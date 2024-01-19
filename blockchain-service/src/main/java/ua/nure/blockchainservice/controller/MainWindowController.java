package ua.nure.blockchainservice.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import ua.nure.blockchainservice.BlockChainApplication;
import ua.nure.blockchainservice.model.Transaction;
import ua.nure.blockchainservice.service.BlockchainData;
import ua.nure.blockchainservice.service.WalletData;

import java.io.IOException;
import java.util.Base64;
import java.util.Optional;

public class MainWindowController {

    @FXML
    private BorderPane borderPane;

    @FXML
    private TextField coins;

    @FXML
    private TableColumn<?, ?> from;

    @FXML
    private TableColumn<?, ?> o;

    @FXML
    private TextArea publicKey;

    @FXML
    private TableColumn<?, ?> signature;

    @FXML
    private TableView<Transaction> tableView;

    @FXML
    private TableColumn<?, ?> timestamp;

    @FXML
    private TableColumn<?, ?> value;

    public void initialize() {
        Base64.Encoder encoder = Base64.getEncoder();
        from.setCellValueFactory(
                new PropertyValueFactory<>("fromFX"));
        o.setCellValueFactory(
                new PropertyValueFactory<>("toFX"));
        value.setCellValueFactory(
                new PropertyValueFactory<>("value"));
        signature.setCellValueFactory(
                new PropertyValueFactory<>("signatureFX"));
        timestamp.setCellValueFactory(
                new PropertyValueFactory<>("timestamp"));
        coins.setText(BlockchainData.getInstance().getWalletBalanceFX());
        publicKey.setText(encoder.encodeToString(WalletData.getInstance().getWallet().getPublicKey().getEncoded()));
        tableView.setItems(BlockchainData.getInstance().getNewBlockTransactionLedgerFX());
        tableView.getSelectionModel().select(0);
    }

    @FXML
    void handleExit(ActionEvent event) {
        BlockchainData.getInstance().setExit(true);
        Platform.exit();
    }

    @FXML
    void refresh(ActionEvent event) {
        tableView.setItems(BlockchainData.getInstance().getNewBlockTransactionLedgerFX());
        tableView.getSelectionModel().select(0);
        coins.setText(BlockchainData.getInstance().getWalletBalanceFX());
    }

    @FXML
    void toNewTransactionController(ActionEvent event) {
        Dialog<ButtonType> newTransactionController = new Dialog<>();
        newTransactionController.initOwner(borderPane.getScene().getWindow());
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(BlockChainApplication.class.getResource("AddNewTransactionWindow.fxml"));
        try {
            newTransactionController.getDialogPane().setContent(fxmlLoader.load());
        } catch (IOException e) {
            System.err.println("Can't load dialog");
            e.printStackTrace();
            return;
        }
        newTransactionController.getDialogPane().getButtonTypes().add(ButtonType.FINISH);
        Optional<ButtonType> result = newTransactionController.showAndWait();
        if (result.isPresent()) {
            tableView.setItems(BlockchainData.getInstance().getNewBlockTransactionLedgerFX());
            coins.setText(BlockchainData.getInstance().getWalletBalanceFX());
        }
    }

}
