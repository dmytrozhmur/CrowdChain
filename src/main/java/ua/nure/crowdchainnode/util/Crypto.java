package ua.nure.crowdchainnode.util;

import ua.nure.crowdchainnode.model.Block;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import static ua.nure.crowdchainnode.util.Constant.ZERO;

public class Crypto {
    private Signature signing;

    public Crypto() throws NoSuchAlgorithmException {
        this.signing = Signature.getInstance("SHA256withDSA");
    }

    public Crypto(Signature signing) {
        this.signing = signing;
    }

    public byte[] applySHA256(String input, PrivateKey privateKey) throws InvalidKeyException, SignatureException {
        this.signing.initSign(privateKey);
        this.signing.update(input.getBytes());
        return this.signing.sign();
    }

    public Boolean isVerified(Block block) throws InvalidKeyException, SignatureException, NoSuchAlgorithmException, InvalidKeySpecException {
        signing.initVerify(KeyFactory.getInstance("DSA").generatePublic(new X509EncodedKeySpec(block.getMinedBy())));
        signing.update(block.toString().getBytes());
        return signing.verify(block.getCurrHash());
    }

    public String byteArrayToHexString(byte[] hash) {
        StringBuilder hexString = new StringBuilder();

        for (byte el: hash) {
            String hex = Integer.toHexString(0xff & el);
            if (hex.length() == 1) hexString.append(ZERO);
            hexString.append(hex);
        }

        String hexHash = hexString.toString();
        System.out.println("SHA-256 hash: " + hexHash);
        return hexHash;
    }
}
