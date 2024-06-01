package ua.nure.crowdchainnode.model;

import java.io.Serializable;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

public class Block implements Serializable {
    private byte[] prevHash;
    private byte[] currHash;
    private String timeStamp;
    private byte[] minedBy;
    private Integer ledgerId = 1;
    private Double luck = 0.0;

    private ArrayList<Transaction> transactionLedger = new ArrayList<>();

    public Block(byte[] prevHash, byte[] currHash, String timeStamp, byte[] minedBy, Integer ledgerId, Double luck, ArrayList<Transaction> transactionLedger) {
        this.prevHash = prevHash;
        this.currHash = currHash;
        this.timeStamp = timeStamp;
        this.minedBy = minedBy;
        this.ledgerId = ledgerId;
        this.luck = luck;
        this.transactionLedger = transactionLedger;
    }

    public Block(LinkedList<Block> currentBlockChain) {
        Block last = currentBlockChain.getLast();
//        this.prevHash = last.currHash;
        this.ledgerId = last.ledgerId + 1;
        this.luck = Math.random() + 1000000;
    }

    public Block() {
        this.prevHash = new byte[]{0};
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Block block = (Block) o;
        return Arrays.equals(getCurrHash(), block.getCurrHash());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(getCurrHash());
    }

    public byte[] getPrevHash() { return prevHash; }
    public byte[] getCurrHash() { return currHash; }

    public void setPrevHash(byte[] prevHash) { this.prevHash = prevHash; }
    public void setCurrHash(byte[] currHash) { this.currHash = currHash; }

    public ArrayList<Transaction> getTransactionLedger() { return transactionLedger; }
    public void setTransactionLedger(ArrayList<Transaction> transactionLedger) {
        this.transactionLedger = transactionLedger;
    }

    public String getTimeStamp() { return timeStamp; }
    public void setMinedBy(byte[] minedBy) { this.minedBy = minedBy; }

    public void setTimeStamp(String timeStamp) { this.timeStamp = timeStamp; }

    public byte[] getMinedBy() { return minedBy; }
    public Double getLuck() { return luck; }
    public void setLuck(Double luck) { this.luck = luck; }

    public Integer getLedgerId() { return ledgerId; }
    public void setLedgerId(Integer ledgerId) { this.ledgerId = ledgerId; }

    @Override
    public String toString() {
        return "Block{" +
                "prevHash=" + Arrays.toString(prevHash) +
                ", timeStamp='" + timeStamp + '\'' +
                ", minedBy=" + Arrays.toString(minedBy) +
                ", ledgerId=" + ledgerId +
                ", luck=" + luck +
                ", transactions=" + transactionLedger +
                '}';
    }
}
