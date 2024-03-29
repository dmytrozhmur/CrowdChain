package ua.nure.blockchainservice.service;

import ua.nure.blockchainservice.model.Wallet;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.sql.*;

public class WalletData {
    private Wallet wallet;
    private static WalletData instance;

    static {
        instance = new WalletData();
    }

    public static WalletData getInstance() {
        return instance;
    }

    public void loadWallet() throws SQLException,
            NoSuchAlgorithmException, InvalidKeySpecException {
        Connection walletConnection = DriverManager
                .getConnection("jdbc:sqlite:src/main/resources/ua/nure/blockchainservice/db/wallet.db");
        Statement walletStatement = walletConnection.createStatement();
        ResultSet resultSet = walletStatement.executeQuery(" SELECT * FROM WALLET ");
        KeyFactory keyFactory = KeyFactory.getInstance("DSA");
        PublicKey pub2 = null;
        PrivateKey prv2 = null;
        while (resultSet.next()) {
            pub2 = keyFactory.generatePublic(new X509EncodedKeySpec(resultSet.getBytes("PUBLIC_KEY")));
            prv2 = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(resultSet.getBytes("PRIVATE_KEY")));
        }
        this.wallet = new Wallet(pub2, prv2);
    }

    public Wallet getWallet() {
        return wallet;
    }
}
