package ua.nure.crowdchainnode.util;

public class Constant {
    public static final Integer REQUEST_TIMEOUT = 3_600_000;
    public static final Integer SOCKET_TIMEOUT = 5000;
    public static final String BLOCKCHAIN_CONNECTION_URL = "jdbc:sqlite:src/main/resources/ua/nure/crowdchainnode/db/blockchain.db";
    public static final String WALLET_CONNECTION_URL = "jdbc:sqlite:src/main/resources/ua/nure/crowdchainnode/db/wallet.db";
    public static final String NODE_CONNECTION_URL = "jdbc:sqlite:src/main/resources/ua/nure/crowdchainnode/db/node.db";
    public static final String ZERO = "0";
    public static final Long MAX_MINING_DIFFICULTY = 3600L;
    public static final Long MIN_MINING_DIFFICULTY = 1200L;
    public static final Integer START_ZEROS_QUANTITY = 4;
    public static final String SERVER_URL = "http://localhost:8085";

    public static final String LOCAL_ADDRESS = "127.0.0.1";
    public static final Integer LOCAL_PORT = 6000;
}
