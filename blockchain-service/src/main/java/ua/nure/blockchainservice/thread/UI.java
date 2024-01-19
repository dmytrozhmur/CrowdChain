package ua.nure.blockchainservice.thread;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ua.nure.blockchainservice.BlockChainApplication;

import java.io.IOException;

public class UI extends Application {


    @Override
    public void start(Stage stage) throws Exception {
        Parent root = null;
        try {
            root = FXMLLoader.load(BlockChainApplication.class.getResource("MainWindow.fxml"));
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        stage.setTitle("CrowdChain Transactions");
        stage.setScene(new Scene(root, 900, 700));
        stage.show();
    }
}
