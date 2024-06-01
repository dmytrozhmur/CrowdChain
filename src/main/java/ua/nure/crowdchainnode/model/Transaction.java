package ua.nure.crowdchainnode.model;

//import sun.security.provider.DSAPublicKeyImpl;

import java.io.Serializable;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;

public class Transaction implements Serializable {
    private byte[] from;
    private String fromFX;
    private byte[] to;
    private String toFX;
    private Double amount;
    private String timestamp;
    private byte[] signature;
    private String signatureFX;
    private Type type;
    private String typeFX;
    private Integer ledgerId;

    public Transaction(byte[] from, byte[] to, Double amount, byte[] signature, Integer ledgerId,
                       String timeStamp, Type type) {
        Base64.Encoder encoder = Base64.getEncoder();
        this.from = from;
        this.fromFX = new String(from);
        this.to = to;
        this.toFX = new String(to);
        this.amount = amount;
        this.signature = signature;
        this.signatureFX = encoder.encodeToString(signature);
        this.ledgerId = ledgerId;
        this.timestamp = timeStamp;
        this.type = type;
        this.typeFX = type.name().toLowerCase();
    }

    public Boolean isVerified(Signature signing) throws InvalidKeyException, SignatureException, NoSuchAlgorithmException, InvalidKeySpecException {
        signing.initVerify(KeyFactory.getInstance("DSA").generatePublic(new X509EncodedKeySpec(this.from)));
        signing.update(this.toString().getBytes());
        return signing.verify(this.signature);
    }

    private void setSignature(Wallet fromWallet, Signature signing) throws InvalidKeyException, SignatureException {
        signing.initSign(fromWallet.getPrivateKey());
        String sr = this.toString();
        signing.update(sr.getBytes());
        this.signature = signing.sign();
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "from=" + Arrays.toString(from) +
                ", to=" + Arrays.toString(to) +
                ", value=" + amount +
                ", timeStamp= " + timestamp +
                ", ledgerId=" + ledgerId +
                '}';
    }

    public byte[] getFrom() { return from; }
    public void setFrom(byte[] from) { this.from = from; }

    public byte[] getTo() { return to; }
    public void setTo(byte[] to) { this.to = to; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public byte[] getSignature() { return signature; }

    public Integer getLedgerId() { return ledgerId; }
    public void setLedgerId(Integer ledgerId) { this.ledgerId = ledgerId; }

    public String getTimestamp() { return timestamp; }

    public String getFromFX() { return fromFX; }
    public String getToFX() { return toFX; }
    public String getSignatureFX() { return signatureFX; }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
        this.typeFX = this.type.name().toLowerCase();
    }

    public String getTypeFX() {
        return typeFX;
    }

    public enum Type {
        DONATE,
        REFUND,
        INNER
    }
}
