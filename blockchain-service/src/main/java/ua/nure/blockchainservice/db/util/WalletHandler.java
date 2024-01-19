package ua.nure.blockchainservice.db.util;

import ua.nure.blockchainservice.model.Wallet;

import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class WalletHandler extends DatabaseHandler {

    public WalletHandler() throws SQLException {
        super("jdbc:sqlite:src/main/resources/ua/nure/blockchainservice/db/wallet.db");
    }

    @Override
    public void initDatabase() throws SQLException, NoSuchAlgorithmException {
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS WALLET ( " +
                " PRIVATE_KEY BLOB NOT NULL UNIQUE, " +
                " PUBLIC_KEY BLOB NOT NULL UNIQUE, " +
                " PRIMARY KEY (PRIVATE_KEY, PUBLIC_KEY)" +
                ") ");
        resultSet = statement.executeQuery(" SELECT * FROM WALLET ");
        if (!resultSet.next()) {
            Wallet wallet = new Wallet();
            byte[] pubBlob = wallet.getPublicKey().getEncoded();
            byte[] prvBlob = wallet.getPrivateKey().getEncoded();
            PreparedStatement insertStatement = connection
                    .prepareStatement(" INSERT INTO WALLET(PUBLIC_KEY, PRIVATE_KEY) VALUES (?, ?) ");
            insertStatement.setBytes(1, pubBlob);
            insertStatement.setBytes(2, prvBlob);
            insertStatement.executeUpdate();
        }
    }
}
