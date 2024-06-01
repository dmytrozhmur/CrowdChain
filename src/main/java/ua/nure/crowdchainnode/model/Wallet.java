package ua.nure.crowdchainnode.model;

import java.io.Serializable;
import java.security.*;

public class Wallet implements Serializable {
    private KeyPair keyPair;
    private Double balance;

    public Wallet() throws NoSuchAlgorithmException {
        this(2048, KeyPairGenerator.getInstance("DSA"));
    }

    public Wallet(Integer keySize, KeyPairGenerator generator) {
        generator.initialize(keySize);
        this.keyPair = generator.generateKeyPair();
        this.balance = 0.0;
    }

    public Wallet(PublicKey publicKey, PrivateKey privateKey, double balance) {
        this.keyPair = new KeyPair(publicKey, privateKey);
        this.balance = balance;
    }

    public KeyPair getKeyPair() {
        return keyPair;
    }

    public PublicKey getPublicKey() {
        return keyPair.getPublic();
    }
    public PrivateKey getPrivateKey() {
        return keyPair.getPrivate();
    }

    public Double getBalance() {
        return balance;
    }
}
