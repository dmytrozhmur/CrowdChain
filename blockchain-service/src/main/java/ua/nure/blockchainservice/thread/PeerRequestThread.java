package ua.nure.blockchainservice.thread;

import ua.nure.blockchainservice.model.Block;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.LinkedList;

public class PeerRequestThread extends Thread {
    private Socket socket;

    public PeerRequestThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            LinkedList<Block> receivedBC = (LinkedList<Block>) ois.readObject();
            System.out.printf("LedgerId = %d Size = %d\n",
                    receivedBC.getLast().getLedgerId(),
                    receivedBC.getLast().getTransactionLedger().size());
            oos.writeObject(BlockchainData.getInstance().getBlockchainConsensus(receivedBC));
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
