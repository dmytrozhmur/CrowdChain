package ua.nure.crowdchainnode.model;


import java.util.Objects;

public class Node {
    private byte[] publicKey;
    private String host;
    private Integer port;
    private Integer score;

    public Node() {
    }

    public Node(byte[] publicKey, String host, Integer port, Integer score) {
        this.publicKey = publicKey;
        this.host = host;
        this.port = port;
        this.score = score;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(byte[] publicKey) {
        this.publicKey = publicKey;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return new String(publicKey);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return Objects.equals(host, node.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host);
    }
}
