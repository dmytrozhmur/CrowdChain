package ua.nure.blockchainservice.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import ua.nure.blockchainservice.model.Transaction;

import java.security.Signature;
import java.util.Base64;

public class AddNewTransactionController {

    @FXML
    private TextField toAddress;

    @FXML
    private TextField value;

    @FXML
    void createNewTransaction(ActionEvent event) {
        Base64.Decoder decoder = Base64.getDecoder();
        Signature signing = Signature.getInstance("SHA256withDSA");
        Integer ledgerId = BlockchainData.getInstance().getTransactionLedgerFX().get(0).getLedgerId();
        byte[] sendB = decoder.decode(toAddress.getText());
        Transaction transaction = new Transaction(WalletData.getInstance()
                .getWallet(),sendB ,Integer.parseInt(value.getText()), ledgerId, signing);
        BlockchainData.getInstance().addTransaction(transaction,false);
        BlockchainData.getInstance().addTransactionState(transaction);
    }

}
