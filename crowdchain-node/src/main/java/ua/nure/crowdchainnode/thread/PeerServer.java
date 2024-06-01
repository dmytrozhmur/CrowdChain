package ua.nure.crowdchainnode.thread;

import java.io.IOException;
import java.net.ServerSocket;

public class PeerServer extends Thread {
    private ServerSocket serverSocket;

    public PeerServer(Integer socketPort) {
        try {
            this.serverSocket = new ServerSocket(socketPort);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                new PeerRequestThread(serverSocket.accept()).start();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
}
