package ua.nure.crowdchainnode.util;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;
import ua.nure.crowdchainnode.repository.TransactionRepository;

public class BlockchainServerWrapper {
    private static BlockchainServerWrapper instance;
    private HttpServer server;
    private TransactionRepository transactionRepository = TransactionRepository.getInstance();

    static {
        instance = new BlockchainServerWrapper();
    }

    private BlockchainServerWrapper() {
        setUp();
    }

    public static BlockchainServerWrapper getInstance() {
        return instance;
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(1000);
    }

    private void setUp() {
        try {
            server = HttpServer.create(new InetSocketAddress(8080), 0);
            server.createContext("/api/transactions", exchange -> {
                String queryParam = exchange.getRequestURI().getQuery();
                StringBuilder body = new StringBuilder();
                if (queryParam != null && queryParam.startsWith("to")) {
                    System.out.println("Query param: " + queryParam);
                    String receiver = queryParam.replace("to=", "");
                    body.append(transactionRepository.findAmountSumByReceiver(receiver.getBytes()));
                }

                exchange.sendResponseHeaders(200, body.toString().getBytes().length);
                exchange.getResponseBody().write(body.toString().getBytes());
                exchange.getResponseBody().close();
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
