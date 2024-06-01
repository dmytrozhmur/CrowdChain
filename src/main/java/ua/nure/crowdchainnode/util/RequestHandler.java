package ua.nure.crowdchainnode.util;

import ua.nure.crowdchainnode.model.Block;
import ua.nure.crowdchainnode.model.Node;
import ua.nure.crowdchainnode.model.Transaction;
import ua.nure.crowdchainnode.repository.BlockchainRepository;
import ua.nure.crowdchainnode.repository.NodeRepository;
import ua.nure.crowdchainnode.repository.WalletRepository;
import ua.nure.crowdchainnode.service.ValidationService;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static ua.nure.crowdchainnode.util.Constant.*;


public class RequestHandler {
    private static RequestHandler instance;
    private NodeRepository nodeRepository = NodeRepository.getInstance();
    private WalletRepository walletRepository = WalletRepository.getInstance();

    static {
        instance = new RequestHandler();
    }

    private RequestHandler() {
        if (nodeRepository.findAll().isEmpty()) {
            fetchNodes();
            notifyNodes(new Block());
        }
    }

    public static RequestHandler getInstance() {
        return instance;
    }

    public void notifyNodes(Block minedBlock) {
        Queue<Node> nodes = new ConcurrentLinkedQueue<>(nodeRepository.findAll());
        for (Node node : nodes) {
            try (Socket socket = new Socket(node.getHost(), node.getPort())) {
                System.out.println("Sending blockchain object on port: " + node.getPort());
                socket.setSoTimeout(5000);
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

                oos.writeObject(minedBlock);

                LinkedList<Block> returnedBC = (LinkedList<Block>) ois.readObject();
                LinkedList<Block> currentBC = BlockchainRepository.getInstance().findAll();
                System.out.printf("RETURNED BC LedgerId = %d Size = %d\n",
                        returnedBC.getLast().getLedgerId(),
                        returnedBC.getLast().getTransactionLedger().size());

                if (!returnedBC.isEmpty() && !currentBC.equals(returnedBC)) {
                    new Thread(() -> ValidationService.getInstance()
                            .compareChains(currentBC, returnedBC)).start();
                }
            } catch (SocketTimeoutException ste) {
                System.out.println("The socket timed out");
            } catch (IOException ioe) {
                System.out.printf("Client Error: %s -- Error on port: %d\n",
                        ioe.getMessage(), node);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void notifyServer(Block minedBlock) {
        try {
            URL url = new URI(SERVER_URL + "/register").toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            ObjectOutputStream oos = new ObjectOutputStream(connection.getOutputStream());

            oos.writeObject(minedBlock);

            oos.close();
            connection.disconnect();
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Transaction> fetchTransactionsPool() {
        try {
            URL url = new URI(SERVER_URL + "/register").toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            ObjectInputStream ois = new ObjectInputStream(connection.getInputStream());

            ArrayList<Transaction> transactions = (ArrayList<Transaction>) ois.readObject();

            ois.close();
            connection.disconnect();
            return transactions;
        } catch (URISyntaxException | IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private void fetchNodes() {
        try {
            URL url = new URI(SERVER_URL + "/register").toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            ObjectOutputStream oos = new ObjectOutputStream(connection.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(connection.getInputStream());

            oos.writeObject(new Node(
                    walletRepository.getWallet().getPublicKey().getEncoded(),
                    InetAddress.getLocalHost().getHostAddress(),
                    LOCAL_PORT, Integer.parseInt(ZERO)));
            ArrayList<Node> nodes = (ArrayList<Node>) ois.readObject();
            nodeRepository.saveAll(nodes);

            oos.close();
            ois.close();
            connection.disconnect();
        } catch (URISyntaxException | IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
