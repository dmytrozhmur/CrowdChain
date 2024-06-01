package ua.nure.crowdchainnode.repository;

import ua.nure.crowdchainnode.model.Node;
import ua.nure.crowdchainnode.util.DatabaseHandler;
import ua.nure.crowdchainnode.util.tuples.Pair;

import java.sql.JDBCType;
import java.sql.ResultSet;
import java.sql.SQLType;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

import static ua.nure.crowdchainnode.util.Constant.BLOCKCHAIN_CONNECTION_URL;

public class NodeRepository implements Repository<Node, byte[]> {
    private static final NodeRepository instance;

    static {
        instance = new NodeRepository();
    }

    private NodeRepository() {}

    public static NodeRepository getInstance() {
        return instance;
    }

    public static void init() {
        try (DatabaseHandler dbHandler = new DatabaseHandler(BLOCKCHAIN_CONNECTION_URL)) {
            dbHandler.runScript("CREATE TABLE IF NOT EXISTS NODES ( " +
                    " PUBLIC_KEY BLOB NOT NULL UNIQUE, " +
                    " HOST TEXT NOT NULL, " +
                    " PORT INTEGER NOT NULL, " +
                    " SCORE INTEGER NOT NULL, " +
                    " PRIMARY KEY (PUBLIC_KEY)" +
                    ")");
        } catch (Exception e) {
            System.err.println("Problem with nodes DB:" + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void save(Node entity) {
        try (DatabaseHandler dbHandler = new DatabaseHandler(BLOCKCHAIN_CONNECTION_URL)) {
            LinkedList<Pair<Object, SQLType>> args = new LinkedList<>();
            extractArgs(entity, args);
            dbHandler.runScript("INSERT INTO NODES (PUBLIC_KEY, HOST, PORT, SCORE) VALUES (?, ?, ?, ?)", args);
        } catch (Exception e) {
            System.err.println("Problem with DB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void saveAll(Collection<Node> entities) {
        Collection<Node> currentNetwork = findAll();
        Collection<Node> nonExistingNodes = new LinkedList<>(entities);
        try (DatabaseHandler dbHandler = new DatabaseHandler(BLOCKCHAIN_CONNECTION_URL)) {
            nonExistingNodes.removeAll(currentNetwork);
            for (Node node : nonExistingNodes) {
                LinkedList<Pair<Object, SQLType>> args = new LinkedList<>();
                extractArgs(node, args);
                dbHandler.runScript("INSERT INTO NODES (PUBLIC_KEY, HOST, PORT, SCORE) VALUES (?, ?, ?, ?)", args);
            }
        } catch (Exception e) {
            System.err.println("Problem with DB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public Optional<Node> findById(byte[] minerId) {
        try (DatabaseHandler dbHandler = new DatabaseHandler(BLOCKCHAIN_CONNECTION_URL)) {
            LinkedList<Pair<Object, SQLType>> args = new LinkedList<>();
            args.add(new Pair<>(minerId, JDBCType.BLOB));
            ResultSet resultSet = dbHandler.runScript("SELECT * FROM NODES WHERE PUBLIC_KEY = ?", args);
            Node node = null;
            if (resultSet.next()) {
                node = new Node(
                        resultSet.getBytes("PUBLIC_KEY"),
                        resultSet.getString("HOST"),
                        resultSet.getInt("PORT"),
                        resultSet.getInt("SCORE")
                );
            }
            return Optional.ofNullable(node);
        } catch (Exception e) {
            System.err.println("Problem with nodes DB: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public Collection<Node> findAll() {
        Collection<Node> nodes = new LinkedList<>();
        try (DatabaseHandler dbHandler = new DatabaseHandler(BLOCKCHAIN_CONNECTION_URL)) {
            ResultSet resultSet = dbHandler.runScript("SELECT * FROM NODES");
            while (resultSet.next()) {
                Node node = new Node(
                        resultSet.getBytes("PUBLIC_KEY"),
                        resultSet.getString("HOST"),
                        resultSet.getInt("PORT"),
                        resultSet.getInt("SCORE")
                );
                nodes.add(node);
            }
        } catch (Exception e) {
            System.err.println("Problem with nodes DB: " + e.getMessage());
            e.printStackTrace();
        }
        return nodes;
    }

    @Override
    public void removeAll() {
        try (DatabaseHandler dbHandler = new DatabaseHandler(BLOCKCHAIN_CONNECTION_URL)) {
            dbHandler.runScript("DELETE FROM NODES");
        } catch (Exception e) {
            System.err.println("Problem with nodes DB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void extractArgs(Node entity, LinkedList<Pair<Object, SQLType>> args) {
        args.add(new Pair<>(entity.getPublicKey(), JDBCType.BLOB));
        args.add(new Pair<>(entity.getHost(), JDBCType.VARCHAR));
        args.add(new Pair<>(entity.getPort(), JDBCType.INTEGER));
        args.add(new Pair<>(entity.getScore(), JDBCType.INTEGER));
    }
}
