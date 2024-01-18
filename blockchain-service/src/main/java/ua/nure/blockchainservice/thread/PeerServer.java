package ua.nure.blockchainservice.thread;

import java.io.IOException;
import java.net.ServerSocket;

public class PeerServer extends Thread {
    private ServerSocket serverSocket;

    public PeerServer(Integer socketPort) throws IOException {
        this.serverSocket = new ServerSocket(socketPort);
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
