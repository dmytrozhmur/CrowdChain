package ua.nure.blockchainservice.model;

//import sun.security.provider.DSAPublicKeyImpl;

import java.io.Serializable;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;

public class Transaction implements Serializable {
    private byte[] from;
    private String fromFX;
    private byte[] to;
    private String toFX;
    private Integer value;
    private String timestamp;
    private byte[] signature;
    private String signatureFX;
    private Integer ledgerId;

    public Transaction(byte[] from, byte[] to, Integer value, byte[] signature, Integer ledgerId,
                       String timeStamp) {
        Base64.Encoder encoder = Base64.getEncoder();
        this.from = from;
        this.fromFX = encoder.encodeToString(from);
        this.to = to;
        this.toFX = encoder.encodeToString(to);
        this.value = value;
        this.signature = signature;
        this.signatureFX = encoder.encodeToString(signature);
        this.ledgerId = ledgerId;
        this.timestamp = timeStamp;
    }

    public Transaction (Wallet fromWallet, byte[] toAddress, Integer value, Integer ledgerId,
                        Signature signing) throws InvalidKeyException, SignatureException {
        Base64.Encoder encoder = Base64.getEncoder();
        this.from = fromWallet.getPublicKey().getEncoded();
        this.fromFX = encoder.encodeToString(this.from);
        this.to = toAddress;
        this.toFX = encoder.encodeToString(toAddress);
        this.value = value;
        this.ledgerId = ledgerId;
        this.timestamp = LocalDateTime.now().toString();
        this.setSignature(fromWallet, signing);
        this.signatureFX = encoder.encodeToString(this.signature);
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
                ", value=" + value +
                ", timeStamp= " + timestamp +
                ", ledgerId=" + ledgerId +
                '}';
    }

    public byte[] getFrom() { return from; }
    public void setFrom(byte[] from) { this.from = from; }

    public byte[] getTo() { return to; }
    public void setTo(byte[] to) { this.to = to; }

    public Integer getValue() { return value; }
    public void setValue(Integer value) { this.value = value; }
    public byte[] getSignature() { return signature; }

    public Integer getLedgerId() { return ledgerId; }
    public void setLedgerId(Integer ledgerId) { this.ledgerId = ledgerId; }

    public String getTimestamp() { return timestamp; }

    public String getFromFX() { return fromFX; }
    public String getToFX() { return toFX; }
    public String getSignatureFX() { return signatureFX; }
}
