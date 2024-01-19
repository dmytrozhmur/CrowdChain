package ua.nure.blockchainservice.controller;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import ua.nure.blockchainservice.model.Transaction;
import ua.nure.blockchainservice.service.BlockchainData;
import ua.nure.blockchainservice.service.WalletData;

import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Base64;
import java.util.Optional;

public class AddNewTransactionController {

    @FXML
    private TextField toAddress;

    @FXML
    private TextField value;

    @FXML
    void createNewTransaction(ActionEvent event) throws GeneralSecurityException {
        Base64.Decoder decoder = Base64.getDecoder();
        Signature signing = Signature.getInstance("SHA256withDSA");
        ObservableList<Transaction> newLedger = BlockchainData.getInstance().getNewBlockTransactionLedgerFX();
        Integer ledgerId = newLedger.isEmpty() ? 0 : newLedger.get(0).getLedgerId();
        byte[] sendB = decoder.decode(toAddress.getText());
        Transaction transaction = new Transaction(WalletData.getInstance()
                .getWallet(),sendB ,Integer.parseInt(value.getText()), ledgerId, signing);
        BlockchainData.getInstance().addTransaction(transaction,false);
        BlockchainData.getInstance().addTransactionState(transaction);
    }

}
