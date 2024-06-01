package ua.nure.crowdchainnode;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ua.nure.crowdchainnode.repository.BlockchainRepository;
import ua.nure.crowdchainnode.repository.NodeRepository;
import ua.nure.crowdchainnode.repository.TransactionRepository;
import ua.nure.crowdchainnode.repository.WalletRepository;
import ua.nure.crowdchainnode.thread.MiningThread;
import ua.nure.crowdchainnode.thread.PeerServer;
import ua.nure.crowdchainnode.util.BlockchainServerWrapper;
import ua.nure.crowdchainnode.util.RequestHandler;

import java.io.IOException;

public class CrowdchainNodeApplication extends Application {
    @Override
    public void init() {
        initDatabase();
        startThreads();
        //initRequestHandler();
    }

    private void initRequestHandler() {
        while (NodeRepository.getInstance().findAll().isEmpty()) {
            RequestHandler.init();
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/ua/nure/crowdchainnode/transactions-view.fxml"));
            Scene scene = new Scene(root, 800, 600);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Crowdchain Node");
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initDatabase() {
        BlockchainRepository.init();
        TransactionRepository.init();
        NodeRepository.init();
        WalletRepository.init();
    }

    private void startThreads() {
        BlockchainServerWrapper.getInstance().start();
        new PeerServer(6000).start();
        new MiningThread().start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}