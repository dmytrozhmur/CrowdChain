package ua.nure.blockchainservice.thread;

import ua.nure.blockchainservice.model.Block;
import ua.nure.blockchainservice.service.BlockchainData;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PeerClient extends Thread {
    private Queue<Integer> queue = new ConcurrentLinkedQueue<>();

    public PeerClient() {
        this.queue.add(6001);
        this.queue.add(6002);
    }

    @Override
    public void run() {
        while (true) {
            try (Socket socket = new Socket("127.0.0.1", queue.peek())) {
                System.out.println("Sending blockchain object on port: " + queue.peek());
                socket.setSoTimeout(5000);
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

                LinkedList<Block> blockChain = BlockchainData.getInstance()
                        .getCurrentBlockChain();
                oos.writeObject(blockChain);

                LinkedList<Block> returnedBlockchain = (LinkedList<Block>) ois.readObject();
                System.out.printf("RETURNED BC LedgerId = %d Size = %d\n",
                        returnedBlockchain.getLast().getLedgerId(),
                        returnedBlockchain.getLast().getTransactionLedger().size());
                BlockchainData.getInstance().getBlockchainConsensus(returnedBlockchain);
            } catch (SocketTimeoutException ste) {
                System.out.println("The socket timed out");
            } catch (IOException ioe) {
                System.out.printf("Client Error: %s -- Error on port: %d\n",
                        ioe.getMessage(), queue.peek());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                queue.add(queue.poll());
            }
        }
    }
}
