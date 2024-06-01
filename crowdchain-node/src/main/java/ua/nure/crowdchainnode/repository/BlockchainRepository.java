package ua.nure.crowdchainnode.repository;

import ua.nure.crowdchainnode.model.Block;

import java.sql.JDBCType;
import java.sql.ResultSet;
import java.sql.SQLType;
import java.util.*;

import ua.nure.crowdchainnode.util.DatabaseHandler;
import ua.nure.crowdchainnode.util.tuples.Pair;

import static ua.nure.crowdchainnode.util.Constant.BLOCKCHAIN_CONNECTION_URL;

public class BlockchainRepository implements Repository<Block, Integer> {
    private static final BlockchainRepository instance;
    private TransactionRepository transactionRepository = TransactionRepository.getInstance();;

    static {
        instance = new BlockchainRepository();
    }

    private BlockchainRepository() {}

    public static BlockchainRepository getInstance() {
        return instance;
    }

    public static void init() {
        try (DatabaseHandler dbHandler = new DatabaseHandler(BLOCKCHAIN_CONNECTION_URL)) {
            dbHandler.runScript("CREATE TABLE IF NOT EXISTS BLOCKCHAIN ( " +
                    " PREVIOUS_HASH BLOB UNIQUE, " +
                    " CURRENT_HASH BLOB UNIQUE, " +
                    " LEDGER_ID INTEGER NOT NULL UNIQUE, " +
                    " CREATED_ON  TEXT, " +
                    " CREATED_BY  BLOB, " +
                    " LUCK  NUMERIC, " +
                    " PRIMARY KEY( LEDGER_ID AUTOINCREMENT) " +
                    ")");
        } catch (Exception e) {
            System.err.println("Problem with blockchain DB:" + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void save(Block entity) {
        try (DatabaseHandler dbHandler = new DatabaseHandler(BLOCKCHAIN_CONNECTION_URL)) {
            LinkedList<Pair<Object, SQLType>> args = new LinkedList<>();
            extractArgs(entity, args);
            dbHandler.runScript("INSERT INTO BLOCKCHAIN (PREVIOUS_HASH, CURRENT_HASH, CREATED_ON, CREATED_BY, LEDGER_ID, LUCK) VALUES (?, ?, ?, ?, ?)", args);
        } catch (Exception e) {
            System.err.println("Problem with DB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void saveAll(Collection<Block> entities) {
        Collection<Block> currentBlockChain = findAll();
        Collection<Block> nonExistingBlocks = new LinkedList<>(entities);
        try (DatabaseHandler dbHandler = new DatabaseHandler(BLOCKCHAIN_CONNECTION_URL)) {
            LinkedList<Pair<Object, SQLType>> args = new LinkedList<>();
            nonExistingBlocks.removeAll(currentBlockChain);
            StringBuilder sql = new StringBuilder("INSERT INTO BLOCKCHAIN (PREVIOUS_HASH, CURRENT_HASH, CREATED_ON, CREATED_BY, LEDGER_ID, LUCK) VALUES");
            sql.append(String.join(",", Collections.nCopies(nonExistingBlocks.size(), " (?, ?, ?, ?, ?, ?)")));
            sql.append(";");
            nonExistingBlocks.forEach(block -> extractArgs(block, args));
            dbHandler.runScript(sql.toString(), args);
        } catch (Exception e) {
            System.err.println("Problem with DB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void extractArgs(Block block, LinkedList<Pair<Object, SQLType>> args) {
        args.add(Pair.with(block.getPrevHash(), JDBCType.BLOB));
        args.add(Pair.with(block.getCurrHash(), JDBCType.BLOB));
        args.add(Pair.with(block.getTimeStamp(), JDBCType.VARCHAR));
        args.add(Pair.with(block.getMinedBy(), JDBCType.BLOB));
        args.add(Pair.with(block.getLedgerId(), JDBCType.INTEGER));
        args.add(Pair.with(block.getLuck(), JDBCType.NUMERIC));
    }

    @Override
    public Optional<Block> findById(Integer ledgerId) {
        try (DatabaseHandler dbHandler = new DatabaseHandler(BLOCKCHAIN_CONNECTION_URL)) {
            ResultSet resultSet = dbHandler.runScript("SELECT * FROM BLOCKCHAIN WHERE LEDGER_ID = ?", new LinkedList<>() {{
                add(Pair.with(ledgerId, JDBCType.INTEGER));
            }});
            if (resultSet.next()) {
                return Optional.of(new Block(
                        resultSet.getBytes("PREVIOUS_HASH"),
                        resultSet.getBytes("CURRENT_HASH"),
                        resultSet.getString("CREATED_ON"),
                        resultSet.getBytes("CREATED_BY"),
                        resultSet.getInt("LEDGER_ID"),
                        resultSet.getDouble("LUCK"),
                        transactionRepository.findAllByLedgerId(resultSet.getInt(ledgerId))));
            }
        } catch (Exception e) {
            System.err.println("Problem with DB: " + e.getMessage());
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public LinkedList<Block> findAll() {
        LinkedList<Block> blocks = new LinkedList<>();
        try (DatabaseHandler dbHandler = new DatabaseHandler(BLOCKCHAIN_CONNECTION_URL)) {
            ResultSet resultSet = dbHandler.runScript("SELECT * FROM BLOCKCHAIN");
            while (resultSet.next()) {
                blocks.add(new Block(
                        resultSet.getBytes("PREVIOUS_HASH"),
                        resultSet.getBytes("CURRENT_HASH"),
                        resultSet.getString("CREATED_ON"),
                        resultSet.getBytes("CREATED_BY"),
                        resultSet.getInt("LEDGER_ID"),
                        resultSet.getDouble("LUCK"),
                        transactionRepository.findAllByLedgerId(resultSet.getInt("LEDGER_ID"))));
            }
        } catch (Exception e) {
            System.err.println("Problem with DB: " + e.getMessage());
            e.printStackTrace();
        }
        return blocks;
    }

    @Override
    public void removeAll() {
        try (DatabaseHandler dbHandler = new DatabaseHandler(BLOCKCHAIN_CONNECTION_URL)) {
            dbHandler.runScript("DELETE FROM BLOCKCHAIN");
        } catch (Exception e) {
            System.err.println("Problem with DB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public ArrayList<Block> findAllByMinerId(byte[] minerId) {
        ArrayList<Block> blocks = new ArrayList<>();
        try (DatabaseHandler dbHandler = new DatabaseHandler(BLOCKCHAIN_CONNECTION_URL)) {
            ResultSet resultSet = dbHandler.runScript("SELECT * FROM BLOCKCHAIN WHERE CREATED_BY = ?", new LinkedList<>() {{
                add(Pair.with(minerId, JDBCType.BLOB));
            }});
            while (resultSet.next()) {
                blocks.add(new Block(
                        resultSet.getBytes("PREVIOUS_HASH"),
                        resultSet.getBytes("CURRENT_HASH"),
                        resultSet.getString("CREATED_ON"),
                        resultSet.getBytes("CREATED_BY"),
                        resultSet.getInt("LEDGER_ID"),
                        resultSet.getDouble("LUCK"),
                        transactionRepository.findAllByLedgerId(resultSet.getInt("LEDGER_ID"))));
            }
        } catch (Exception e) {
            System.err.println("Problem with DB: " + e.getMessage());
            e.printStackTrace();
        }
        return blocks;
    }
}
