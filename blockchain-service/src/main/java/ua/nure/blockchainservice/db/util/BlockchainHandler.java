package ua.nure.blockchainservice.db.util;

import ua.nure.blockchainservice.model.Block;
import ua.nure.blockchainservice.model.Transaction;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class BlockchainHandler extends DatabaseHandler {
    public BlockchainHandler() throws SQLException {
        super("jdbc:sqlite:../db/blockchain.db");
    }

    @Override
    public void initDatabase() throws SQLException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        createBlockchainTable();
        resultSet = statement.executeQuery(" SELECT * FROM BLOCKCHAIN ");
        Transaction initBlockRewardTransaction = getInitBlockRewardTransaction();

        createTransactionTable();
        if (initBlockRewardTransaction != null) {
            BlockchainData.getInstance().addTransaction(initBlockRewardTransaction, true);
            BlockchainData.getInstance().addTransactionState(initBlockRewardTransaction);
        }
    }

    private void createTransactionTable() throws SQLException {
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS TRANSACTIONS ( " +
                " ID INTEGER NOT NULL UNIQUE, " +
                " \"FROM\" BLOB, " +
                " \"TO\" BLOB, " +
                " LEDGER_ID INTEGER, " +
                " VALUE INTEGER, " +
                " SIGNATURE BLOB UNIQUE, " +
                " CREATED_ON TEXT, " +
                " PRIMARY KEY(ID AUTOINCREMENT) " +
                ")");
    }

    private Transaction getInitBlockRewardTransaction() throws SQLException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        if (!resultSet.next()) {
            insertFirstBlock();
            Signature transSignature = Signature.getInstance("SHA256withDSA");
            return new Transaction(
                    WalletData.getInstance().getWallet(),
                    WalletData.getInstance().getWallet().getPublicKey().getEncoded(),
                    100, 1, transSignature);
        }
        return null;
    }

    private void insertFirstBlock() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, SQLException {
        Block firstBlock = new Block();
        firstBlock.setMinedBy(WalletData.getInstance().getWallet().getPublicKey().getEncoded());
        firstBlock.setTimeStamp(LocalDateTime.now().toString());
        //helper class.
        Signature signing = Signature.getInstance("SHA256withDSA");
        signing.initSign(WalletData.getInstance().getWallet().getPrivateKey());
        signing.update(firstBlock.toString().getBytes());
        firstBlock.setCurrHash(signing.sign());
        PreparedStatement pstmt = connection
                .prepareStatement("INSERT INTO BLOCKCHAIN(PREVIOUS_HASH, CURRENT_HASH , LEDGER_ID," +
                        " CREATED_ON, CREATED_BY,MINING_POINTS,LUCK ) " +
                        " VALUES (?,?,?,?,?,?,?) ");
        pstmt.setBytes(1, firstBlock.getPrevHash());
        pstmt.setBytes(2, firstBlock.getCurrHash());
        pstmt.setInt(3, firstBlock.getLedgerId());
        pstmt.setString(4, firstBlock.getTimeStamp());
        pstmt.setBytes(5, WalletData.getInstance().getWallet().getPublicKey().getEncoded());
        pstmt.setInt(6, firstBlock.getMiningPoints());
        pstmt.setDouble(7, firstBlock.getLuck());
        pstmt.executeUpdate();
    }

    private void createBlockchainTable() throws SQLException {
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS BLOCKCHAIN ( " +
                " ID INTEGER NOT NULL UNIQUE, " +
                " PREVIOUS_HASH BLOB UNIQUE, " +
                " CURRENT_HASH BLOB UNIQUE, " +
                " LEDGER_ID INTEGER NOT NULL UNIQUE, " +
                " CREATED_ON  TEXT, " +
                " CREATED_BY  BLOB, " +
                " MINING_POINTS  TEXT, " +
                " LUCK  NUMERIC, " +
                " PRIMARY KEY( ID AUTOINCREMENT) " +
                ")"
        );
    }
}
