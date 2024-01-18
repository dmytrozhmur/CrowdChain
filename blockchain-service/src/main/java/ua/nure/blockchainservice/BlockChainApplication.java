package ua.nure.blockchainservice;

import javafx.application.Application;
import javafx.stage.Stage;
import ua.nure.blockchainservice.db.util.BlockchainHandler;
import ua.nure.blockchainservice.db.util.WalletHandler;
import ua.nure.blockchainservice.thread.MiningThread;
import ua.nure.blockchainservice.thread.PeerClient;
import ua.nure.blockchainservice.thread.PeerServer;
import ua.nure.blockchainservice.thread.UI;

import java.security.*;
import java.sql.*;

public class BlockChainApplication extends Application {
    @Override
    public void init() {
        try(WalletHandler walletHandler = new WalletHandler();
            BlockchainHandler blockChainHandler = new BlockchainHandler()) {
            walletHandler.initDatabase();
            WalletData.getInstance().loadWallet();
            blockChainHandler.initDatabase();
        } catch (SQLException | NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
            System.err.println("DB failed: " + e.getMessage());
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        BlockchainData.getInstance().loadBlockChain();
    }

    @Override
    public void start(Stage stage) throws Exception {
        new UI().start(stage);
        new PeerClient().start();
        new PeerServer(6000).start();
        new MiningThread().start();
    }

    public static void main(String[] args) {
        launch();
    }
}