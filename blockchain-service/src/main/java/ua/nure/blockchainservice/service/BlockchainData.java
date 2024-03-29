package ua.nure.blockchainservice.service;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
//import sun.security.provider.DSAPublicKeyImpl;
import ua.nure.blockchainservice.model.Block;
import ua.nure.blockchainservice.model.Transaction;
import ua.nure.blockchainservice.model.Wallet;

import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;

public class BlockchainData {
    private static final int TIMEOUT_INTERVAL = 65;
    private static final int MINING_INTERVAL = 60;
    private static BlockchainData instance;
    private ObservableList<Transaction> newBlockTransactionsFX;
    private ObservableList<Transaction> newBlockTransactions;
    private LinkedList<Block> currentBlockChain = new LinkedList<>();
    private Block latestBlock;
    private boolean exit = false;
    private int miningPoints;
    private Signature signing = Signature.getInstance("SHA256withDSA");
    private Comparator<Transaction> transactionComparator = Comparator.comparing(Transaction::getTimestamp);

    static {
        try {
            instance = new BlockchainData();
        } catch (NoSuchAlgorithmException nsae) {
            nsae.printStackTrace();
        }
    }

    private BlockchainData() throws NoSuchAlgorithmException {
        newBlockTransactions = FXCollections.observableArrayList();
        newBlockTransactionsFX = FXCollections.observableArrayList();
    }

    public static BlockchainData getInstance() {
        return instance;
    }

    public ObservableList<Transaction> getNewBlockTransactionLedgerFX() {
        newBlockTransactionsFX.clear();
        newBlockTransactions.sorted(transactionComparator);
        newBlockTransactionsFX.addAll(newBlockTransactions);
        return FXCollections.observableArrayList(newBlockTransactionsFX);
    }

    public String getWalletBalanceFX() {
        return getBalance(
                this.currentBlockChain,
                newBlockTransactions,
                WalletData.getInstance()
                        .getWallet()
                        .getPublicKey()).toString();
    }

    public void addTransactionState(Transaction transaction) {
        newBlockTransactions.add(transaction);
        newBlockTransactions.sorted(transactionComparator);
    }

    public void addTransaction(Transaction transaction, boolean blockReward)
            throws GeneralSecurityException {
        try {
            if (!blockReward && getBalance(
                    currentBlockChain,
                    newBlockTransactions,
                    KeyFactory.getInstance("DSA").generatePublic(new X509EncodedKeySpec(transaction
                            .getFrom()))) < transaction.getValue()) {
                throw new GeneralSecurityException("Not enough money");
            } else {
                Connection connection = DriverManager
                        .getConnection("jdbc:sqlite:src/main/resources/ua/nure/blockchainservice/db/blockchain.db");
                PreparedStatement statement = connection.prepareStatement(
                            "INSERT INTO TRANSACTIONS(" +
                                "\"FROM\", \"TO\", LEDGER_ID, VALUE, SIGNATURE, CREATED_ON) " +
                                "VALUES(?,?,?,?,?,?) ");
                statement.setBytes(1, transaction.getFrom());
                statement.setBytes(2, transaction.getTo());
                statement.setInt(3, transaction.getLedgerId());
                statement.setInt(4, transaction.getValue());
                statement.setBytes(5, transaction.getSignature());
                statement.setString(6, transaction.getTimestamp());
                statement.executeUpdate();

                statement.close();
                connection.close();
            }
        } catch (SQLException sqle) {
            System.err.printf("Problem with DB: %s\n", sqle.getMessage());
            sqle.printStackTrace();
        }
    }

    public void loadBlockChain() {
        try {
            Connection connection = DriverManager
                    .getConnection("jdbc:sqlite:src/main/resources/ua/nure/blockchainservice/db/blockchain.db");
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(" SELECT * FROM BLOCKCHAIN ");

            while (resultSet.next()) {
                this.currentBlockChain.add(new Block(
                        resultSet.getBytes("PREVIOUS_HASH"),
                        resultSet.getBytes("CURRENT_HASH"),
                        resultSet.getString("CREATED_ON"),
                        resultSet.getBytes("CREATED_BY"),
                        resultSet.getInt("LEDGER_ID"),
                        resultSet.getInt("MINING_POINTS"),
                        resultSet.getDouble("LUCK"),
                        loadTransactionLedger(resultSet.getInt("LEDGER_ID"))
                ));
            }

            this.latestBlock = currentBlockChain.getLast();
            Transaction transaction = new Transaction(
                    new Wallet(),
                    WalletData.getInstance().getWallet().getPublicKey().getEncoded(),
                    100, latestBlock.getLedgerId() + 1, this.signing);
            newBlockTransactions.clear();
//            newBlockTransactions.add(transaction);
            verifyBlockChain(currentBlockChain);

            resultSet.close();
            statement.close();
            connection.close();
        } catch (SQLException | NoSuchAlgorithmException e) {
            System.err.printf("Problem with DB: %s\n", e.getMessage());
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
    }

    public void mineBlock() {
        try {
            finalizeBlock(WalletData.getInstance().getWallet());
            addBlock(this.latestBlock);
        } catch (SQLException | GeneralSecurityException e) {
            System.err.printf("Problem with DB: %s\n", e.getMessage());
            e.printStackTrace();
        }
    }

    public LinkedList<Block> getBlockchainConsensus(LinkedList<Block> receivedBC) {
        try {
            verifyBlockChain(receivedBC);

            if (!Arrays.equals(
                    receivedBC.getLast().getCurrHash(),
                    getCurrentBlockChain().getLast().getCurrHash())) {
                if (checkIfOutdated(receivedBC) != null) {
                    return getCurrentBlockChain();
                } else {
                    if (checkWhichIsCreatedFirst(receivedBC) != null) {
                        return getCurrentBlockChain();
                    } else {
                        if (compareMiningPointsAndLuck(receivedBC) != null) {
                            return getCurrentBlockChain();
                        }
                    }
                }
            } else if (!receivedBC.getLast().getTransactionLedger().equals(
                    getCurrentBlockChain().getLast().getTransactionLedger())) {
                updateTransactionLedgers(receivedBC);
                System.out.println("Transaction ledgers updated");
                return receivedBC;
            } else {
                System.out.println("Blockchains are identical");
            }
        } catch (GeneralSecurityException gse) {
            gse.printStackTrace();
            // return getCurrentBlockChain(); ?
        }
        return receivedBC;
    }

    private void finalizeBlock(Wallet minersWallet)
            throws GeneralSecurityException, SQLException {
        this.latestBlock = new Block(BlockchainData.getInstance().currentBlockChain);
        this.latestBlock.setTransactionLedger(new ArrayList<>(newBlockTransactions));
        this.latestBlock.setTimeStamp(LocalDateTime.now().toString());
        this.latestBlock.setMinedBy(minersWallet.getPublicKey().getEncoded());
        this.latestBlock.setMiningPoints(miningPoints);

        this.signing.initSign(minersWallet.getPrivateKey());
        this.signing.update(latestBlock.toString().getBytes());
        this.latestBlock.setCurrHash(this.signing.sign());

        this.currentBlockChain.add(this.latestBlock);
        miningPoints = 0;

        latestBlock.getTransactionLedger().sort(transactionComparator);
//        addTransaction(latestBlock.getTransactionLedger().get(0), true);
//        Transaction transaction = new Transaction(
//                new Wallet(), minersWallet.getPublicKey().getEncoded(),
//                100, this.latestBlock.getLedgerId() + 1, this.signing);

        newBlockTransactions.clear();
//        newBlockTransactions.add(transaction);
    }

    private void addBlock(Block block) {
        try {
            Connection connection = DriverManager
                    .getConnection("jdbc:sqlite:src/main/resources/ua/nure/blockchainservice/db/blockchain.db");
            PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO BLOCKCHAIN(" +
                            "PREVIOUS_HASH, CURRENT_HASH, LEDGER_ID, CREATED_ON, CREATED_BY, MINING_POINTS, LUCK) " +
                            "VALUES(?,?,?,?,?,?,?) ");
            statement.setBytes(1, block.getPrevHash());
            statement.setBytes(2, block.getCurrHash());
            statement.setInt(3, block.getLedgerId());
            statement.setString(4, block.getTimeStamp());
            statement.setBytes(5, block.getMinedBy());
            statement.setInt(6, block.getMiningPoints());
            statement.setDouble(7, block.getLuck());
            statement.executeUpdate();

            statement.close();
            connection.close();
        } catch (SQLException sqle) {
            System.err.printf("Problem with DB: %s\n", sqle.getMessage());
            sqle.printStackTrace();
        }
    }

    private Integer getBalance(LinkedList<Block> blockChain,
                               ObservableList<Transaction> currentLedger,
                               PublicKey walletAddress) {
        Integer balance = 100;
        for (Block block : blockChain) {
            for (Transaction transaction : block.getTransactionLedger()) {
                if (Arrays.equals(transaction.getFrom(), walletAddress.getEncoded())) {
                    balance -= transaction.getValue();
                }
                if (Arrays.equals(transaction.getTo(), walletAddress.getEncoded())) {
                    balance += transaction.getValue();
                }
            }
        }
        for (Transaction transaction : currentLedger) {
            if (Arrays.equals(transaction.getFrom(), walletAddress.getEncoded())) {
                balance -= transaction.getValue();
            }
        }

        return balance;
    }

    private void verifyBlockChain(LinkedList<Block> currentBlockChain)
            throws GeneralSecurityException {
        for (Block block : currentBlockChain) {
            if (!block.isVerified(this.signing)) {
                throw new GeneralSecurityException("Block validation failed");
            }
            ArrayList<Transaction> transactions = block.getTransactionLedger();
            for (Transaction transaction : transactions) {
                if (!transaction.isVerified(signing)) {
                    throw new GeneralSecurityException("Transaction validation failed");
                }
            }
        }
    }

    private ArrayList<Transaction> loadTransactionLedger(Integer ledgerId)
            throws SQLException {
        ArrayList<Transaction> transactions = new ArrayList<>();
        try {
            Connection connection = DriverManager
                    .getConnection("jdbc:sqlite:src/main/resources/ua/nure/blockchainservice/db/blockchain.db");
            PreparedStatement statement = connection.prepareStatement(" SELECT * FROM TRANSACTIONS WHERE LEDGER_ID = ? ");
            statement.setInt(1, ledgerId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                transactions.add(new Transaction(
                        resultSet.getBytes("FROM"),
                        resultSet.getBytes("TO"),
                        resultSet.getInt("VALUE"),
                        resultSet.getBytes("SIGNATURE"),
                        resultSet.getInt("LEDGER_ID"),
                        resultSet.getString("CREATED_ON")
                ));
            }

            resultSet.close();
            statement.close();
            connection.close();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
        return transactions;
    }

    private void replaceBlockchainInDatabase(LinkedList<Block> receivedBC) {
        try {
            Connection connection = DriverManager
                    .getConnection("jdbc:sqlite:src/main/resources/ua/nure/blockchainservice/db/blockchain.db");
            Statement clearDbStatement = connection.createStatement();
            clearDbStatement.executeUpdate(" DELETE FROM BLOCKCHAIN ");
            clearDbStatement.executeUpdate(" DELETE FROM TRANSACTIONS ");

            clearDbStatement.close();
            connection.close();

            for (Block block : receivedBC) {
                addBlock(block);
//                boolean rewardTransaction = true;
                block.getTransactionLedger().sort(transactionComparator);

                for (Transaction transaction : block.getTransactionLedger()) {
                    System.out.println(transaction);
                    addTransaction(transaction, false);
//                    rewardTransaction = false;
                }
            }
        } catch (SQLException | GeneralSecurityException e) {
            System.err.printf("Problem with DB: %s\n", e.getMessage());
            e.printStackTrace();
        }
    }

    private LinkedList<Block> checkIfOutdated(LinkedList<Block> receivedBC) {
        long lastMinedLocalBlock = LocalDateTime.parse(
                getCurrentBlockChain().getLast().getTimeStamp()
        ).toEpochSecond(ZoneOffset.UTC);
        long lastMinedReceivedBlock = LocalDateTime.parse(
                receivedBC.getLast().getTimeStamp()
        ).toEpochSecond(ZoneOffset.UTC);

        if ((lastMinedLocalBlock + TIMEOUT_INTERVAL) < LocalDateTime.now()
                .toEpochSecond(ZoneOffset.UTC) &&
            (lastMinedReceivedBlock + TIMEOUT_INTERVAL) < LocalDateTime.now()
                .toEpochSecond(ZoneOffset.UTC)) {
            System.out.println("Both are old. Check other peers.");
        } else if ((lastMinedLocalBlock + TIMEOUT_INTERVAL) < LocalDateTime.now()
                        .toEpochSecond(ZoneOffset.UTC) &&
                    (lastMinedReceivedBlock + TIMEOUT_INTERVAL) >= LocalDateTime.now()
                        .toEpochSecond(ZoneOffset.UTC)) {
            setMiningPoints(0);
            replaceBlockchainInDatabase(receivedBC);
            setCurrentBlockChain(new LinkedList<>());
            loadBlockChain();
            System.out.println("Received blockchain won! Local one was old.");
        } else if ((lastMinedLocalBlock + TIMEOUT_INTERVAL) >= LocalDateTime.now()
                        .toEpochSecond(ZoneOffset.UTC) &&
                    (lastMinedReceivedBlock + TIMEOUT_INTERVAL) < LocalDateTime.now()
                        .toEpochSecond(ZoneOffset.UTC)) {
            return getCurrentBlockChain();
        }
        return null;
    }

    private LinkedList<Block> checkWhichIsCreatedFirst(LinkedList<Block> receivedBC) {
        long initReceivedBlockTime = LocalDateTime.parse(
                receivedBC.getFirst().getTimeStamp()
        ).toEpochSecond(ZoneOffset.UTC);
        long initLocalBlockTime = LocalDateTime.parse(
                getCurrentBlockChain().getFirst().getTimeStamp()
        ).toEpochSecond(ZoneOffset.UTC);

        if (initReceivedBlockTime < initLocalBlockTime) {
            setMiningPoints(0);
            replaceBlockchainInDatabase(receivedBC);
            setCurrentBlockChain(new LinkedList<>());
            loadBlockChain();
            System.out.println("PeerClient blockchain won! PeerServer's BC was old");
        } else if (initLocalBlockTime < initReceivedBlockTime) {
            return getCurrentBlockChain();
        }
        return null;
    }

    private LinkedList<Block> compareMiningPointsAndLuck(LinkedList<Block> receivedBC)
        throws GeneralSecurityException {
        if (receivedBC.equals(getCurrentBlockChain())) {
            if (    (receivedBC.getLast().getMiningPoints() > getCurrentBlockChain().getLast().getMiningPoints())
                    ||
                    (receivedBC.getLast().getMiningPoints().equals(getCurrentBlockChain().getLast().getMiningPoints()))
                    &&
                    (receivedBC.getLast().getLuck() > getCurrentBlockChain().getLast().getLuck())) {
                getCurrentBlockChain().getLast().getTransactionLedger().remove(0);

                for (Transaction transaction :
                        getCurrentBlockChain().getLast().getTransactionLedger()) {
                    if (!receivedBC.getLast().getTransactionLedger().contains(transaction)) {
                        receivedBC.getLast().getTransactionLedger().add(transaction);
                    }
                }

                receivedBC.getLast().getTransactionLedger().sort(transactionComparator);
                setMiningPoints(BlockchainData.getInstance().getMiningPoints()
                        + getCurrentBlockChain().getLast().getMiningPoints());
                replaceBlockchainInDatabase(receivedBC);
                setCurrentBlockChain(new LinkedList<>());
                loadBlockChain();
                System.out.println("Received blockchain won!");
            } else {
                receivedBC.getLast().getTransactionLedger().remove(0);

                for (Transaction transaction : receivedBC.getLast().getTransactionLedger()) {
                    if (!getCurrentBlockChain().getLast()
                            .getTransactionLedger().contains(transaction)) {
                        getCurrentBlockChain().getLast().getTransactionLedger().add(transaction);
                        addTransaction(transaction, false);
                    }
                }

                getCurrentBlockChain().getLast().getTransactionLedger().sort(transactionComparator);
                return getCurrentBlockChain();
            }
        }
        return null;
    }

    private void updateTransactionLedgers(LinkedList<Block> receivedBC)
            throws GeneralSecurityException {
        for (Transaction transaction : receivedBC.getLast().getTransactionLedger()) {
            if (!getCurrentBlockChain().getLast()
                    .getTransactionLedger().contains(transaction)) {
                getCurrentBlockChain().getLast().getTransactionLedger().add(transaction);
                System.out.printf(
                        "Current ledger id = %s, transaction id = %s\n",
                        getCurrentBlockChain().getLast().getLedgerId(),
                        transaction.getLedgerId());
                addTransaction(transaction, false);
            }
        }
        getCurrentBlockChain().getLast().getTransactionLedger().sort(transactionComparator);

        for (Transaction transaction :
                getCurrentBlockChain().getLast().getTransactionLedger()) {
            if (!receivedBC.getLast().getTransactionLedger().contains(transaction)) {
                receivedBC.getLast().getTransactionLedger().add(transaction);
            }
        }
        receivedBC.getLast().getTransactionLedger().sort(transactionComparator);
    }

    public LinkedList<Block> getCurrentBlockChain() {
        return currentBlockChain;
    }

    public void setCurrentBlockChain(LinkedList<Block> currentBlockChain) {
        this.currentBlockChain = currentBlockChain;
    }

    public boolean isExit() {
        return exit;
    }

    public void setExit(boolean exit) {
        this.exit = exit;
    }

    public int getMiningPoints() {
        return miningPoints;
    }

    public void setMiningPoints(int miningPoints) {
        this.miningPoints = miningPoints;
    }

    public static int getTimeoutInterval() {
        return TIMEOUT_INTERVAL;
    }

    public static int getMiningInterval() {
        return MINING_INTERVAL;
    }
}
