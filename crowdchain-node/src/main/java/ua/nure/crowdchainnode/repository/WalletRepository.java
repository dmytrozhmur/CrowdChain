package ua.nure.crowdchainnode.repository;

import ua.nure.crowdchainnode.model.Wallet;
import ua.nure.crowdchainnode.util.DatabaseHandler;
import ua.nure.crowdchainnode.util.tuples.Pair;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.sql.JDBCType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLType;
import java.util.LinkedList;
import java.util.List;

import static ua.nure.crowdchainnode.util.Constant.WALLET_CONNECTION_URL;

public class WalletRepository {
    private static WalletRepository instance = new WalletRepository();
    private Wallet wallet;

    private WalletRepository() {
    }

    public static WalletRepository getInstance() {
        return instance;
    }

    public static void init() {
        try (DatabaseHandler handler = new DatabaseHandler(WALLET_CONNECTION_URL)) {
            handler.runScript("CREATE TABLE IF NOT EXISTS WALLET ( " +
                    " PRIVATE_KEY BLOB NOT NULL UNIQUE, " +
                    " PUBLIC_KEY BLOB NOT NULL UNIQUE, " +
                    " BALANCE DOUBLE NOT NULL DEFAULT 0, " +
                    " PRIMARY KEY (PRIVATE_KEY, PUBLIC_KEY)" +
                    ") ");
            ResultSet resultSet = handler.runScript(" SELECT * FROM WALLET ");
            if (!resultSet.next()) {
                Wallet wallet = new Wallet();
                LinkedList<Pair<Object, SQLType>> args = new LinkedList<>();
                byte[] pubBlob = wallet.getPublicKey().getEncoded();
                byte[] prvBlob = wallet.getPrivateKey().getEncoded();
                args.add(new Pair<>(pubBlob, JDBCType.BLOB));
                args.add(new Pair<>(prvBlob, JDBCType.BLOB));
                handler.runScript(" INSERT INTO WALLET(PUBLIC_KEY, PRIVATE_KEY) VALUES (?, ?) ", args);
            }
        } catch (Exception exc) {
            System.err.println("Problem with wallet DB: " + exc.getMessage());
            exc.printStackTrace();
        }
    }

    public void loadWallet() {
        try (DatabaseHandler handler = new DatabaseHandler(WALLET_CONNECTION_URL)) {
            ResultSet resultSet = handler.runScript(" SELECT * FROM WALLET ");
            KeyFactory keyFactory = KeyFactory.getInstance("DSA");
            PublicKey pub2 = null;
            PrivateKey prv2 = null;
            double balance = 0.0;
            if (resultSet.next()) {
                pub2 = keyFactory.generatePublic(new X509EncodedKeySpec(resultSet.getBytes("PUBLIC_KEY")));
                prv2 = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(resultSet.getBytes("PRIVATE_KEY")));
                balance = resultSet.getDouble("BALANCE");
            }
            this.wallet = new Wallet(pub2, prv2, balance);
        } catch (Exception exc) {
            System.err.println("Problem with wallet DB: " + exc.getMessage());
            exc.printStackTrace();
        }
    }

    public void updateBalance(double balance) {
        try (DatabaseHandler handler = new DatabaseHandler(WALLET_CONNECTION_URL)) {
            LinkedList<Pair<Object, SQLType>> args = new LinkedList<>();
            args.add(new Pair<>(balance, JDBCType.DOUBLE));
            handler.runScript("UPDATE WALLET SET BALANCE = ? ", args);
        } catch (SQLException e) {
            System.err.println("Problem with wallet DB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void removeAll() {
        try (DatabaseHandler handler = new DatabaseHandler(WALLET_CONNECTION_URL)) {
            handler.runScript("DELETE FROM WALLET");
        } catch (SQLException e) {
            System.err.println("Problem with wallet DB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Wallet getWallet() {
        if (wallet == null || wallet.getPublicKey() == null || wallet.getPrivateKey() == null) loadWallet();
        return wallet;
    }
}
