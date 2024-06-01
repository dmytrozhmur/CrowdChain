package ua.nure.crowdchainnode.thread;

import ua.nure.crowdchainnode.model.Block;
import ua.nure.crowdchainnode.model.Node;
import ua.nure.crowdchainnode.repository.BlockchainRepository;
import ua.nure.crowdchainnode.repository.NodeRepository;
import ua.nure.crowdchainnode.service.ValidationService;
import ua.nure.crowdchainnode.util.RequestHandler;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

public class PeerRequestThread extends Thread {
    private Socket socket;
    private ValidationService validationService = ValidationService.getInstance();
    private BlockchainRepository blockchainRepository = BlockchainRepository.getInstance();
    private NodeRepository nodeRepository = NodeRepository.getInstance();
    private RequestHandler requestHandler = RequestHandler.getInstance();

    public PeerRequestThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

            Block receivedBlock = (Block) ois.readObject();
            System.out.printf("Received LedgerId = %d", receivedBlock.getLedgerId());

            LinkedList<Block> currentBC = blockchainRepository.findAll();
            Optional<Block> currentLastBlock = currentBC.isEmpty()
                    ? Optional.empty()
                    : Optional.ofNullable(currentBC.getLast());
            Optional<Block> currentPrevBlock = currentBC.size() < 2
                    ? Optional.empty()
                    : Optional.ofNullable(currentBC.get(currentBC.size() - 2));

            if (validationService.isProved(receivedBlock, currentLastBlock, currentPrevBlock)) {
                System.out.println("Received block is valid. Saving to blockchain.");
                blockchainRepository.save(receivedBlock);
                byte[] senderPK = receivedBlock.getMinedBy();
                Node node = nodeRepository.findById(senderPK)
                        .orElse(new Node(senderPK, socket.getInetAddress().getHostAddress(), socket.getPort(), 0));
                node.setScore(node.getScore() + 1);
                nodeRepository.save(node);
            } else {
                System.out.println("Received block is invalid. Returning current blockchain.");
                oos.writeObject(currentBC);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
