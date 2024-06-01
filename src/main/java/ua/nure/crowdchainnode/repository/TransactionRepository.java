package ua.nure.crowdchainnode.repository;

import java.sql.JDBCType;
import java.sql.ResultSet;
import java.sql.SQLType;
import java.util.*;

import ua.nure.crowdchainnode.model.Transaction;
import ua.nure.crowdchainnode.util.DatabaseHandler;
import ua.nure.crowdchainnode.util.tuples.Pair;

import static ua.nure.crowdchainnode.util.Constant.BLOCKCHAIN_CONNECTION_URL;

public class TransactionRepository implements Repository<Transaction, Integer> {
    private static final TransactionRepository instance;

    static {
        instance = new TransactionRepository();
    }

    private TransactionRepository() {}

    public static TransactionRepository getInstance() {
        return instance;
    }

    public static void init() {
        try (DatabaseHandler dbHandler = new DatabaseHandler(BLOCKCHAIN_CONNECTION_URL)) {
            dbHandler.runScript("CREATE TABLE IF NOT EXISTS TRANSACTIONS ( " +
                    " ID INTEGER NOT NULL UNIQUE, " +
                    " \"FROM\" BLOB, " +
                    " \"TO\" BLOB, " +
                    " LEDGER_ID INTEGER, " +
                    " AMOUNT INTEGER, " +
                    " SIGNATURE BLOB UNIQUE, " +
                    " CREATED_ON TEXT, " +
                    " TYPE TEXT, " +
                    " PRIMARY KEY(ID AUTOINCREMENT) " +
                    ")");
        } catch (Exception e) {
            System.err.println("Problem with transactions DB:" + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void save(Transaction entity) {
        try (DatabaseHandler dbHandler = new DatabaseHandler(BLOCKCHAIN_CONNECTION_URL)) {
            dbHandler.runScript("INSERT INTO TRANSACTIONS (\"FROM\", \"TO\", LEDGER_ID, AMOUNT, SIGNATURE, CREATED_ON, TYPE) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)", new LinkedList<>() {{
                        add(Pair.with(entity.getFrom(), JDBCType.BLOB));
                        add(Pair.with(entity.getTo(), JDBCType.BLOB));
                        add(Pair.with(entity.getLedgerId(), JDBCType.INTEGER));
                        add(Pair.with(entity.getAmount(), JDBCType.DOUBLE));
                        add(Pair.with(entity.getSignature(), JDBCType.BLOB));
                        add(Pair.with(entity.getTimestamp(), JDBCType.VARCHAR));
                        add(Pair.with(entity.getType().name(), JDBCType.VARCHAR));
                    }});
        } catch (Exception e) {
            System.err.println("Problem while saving transaction: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void saveAll(Collection<Transaction> entities) {
        Collection<Transaction> nonExistingTransactions = new LinkedList<>(entities);
        try (DatabaseHandler dbHandler = new DatabaseHandler(BLOCKCHAIN_CONNECTION_URL)) {
            ResultSet resultSet = dbHandler.runScript("SELECT * FROM TRANSACTIONS");
            while (resultSet.next()) {
                try {
                    Transaction transaction = new Transaction(
                            resultSet.getBytes("FROM"),
                            resultSet.getBytes("TO"),
                            resultSet.getDouble("AMOUNT"),
                            resultSet.getBytes("SIGNATURE"),
                            resultSet.getInt("LEDGER_ID"),
                            resultSet.getString("CREATED_ON"),
                            Transaction.Type.valueOf(resultSet.getString("TYPE"))
                    );
                    nonExistingTransactions.remove(transaction);
                } catch (Exception e) {
                    System.err.println("Problem while retrieving all transactions: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            LinkedList<Pair<Object, SQLType>> args = new LinkedList<>();
            nonExistingTransactions.forEach(transaction -> {
                args.add(Pair.with(transaction.getFrom(), JDBCType.BLOB));
                args.add(Pair.with(transaction.getTo(), JDBCType.BLOB));
                args.add(Pair.with(transaction.getLedgerId(), JDBCType.INTEGER));
                args.add(Pair.with(transaction.getAmount(), JDBCType.DOUBLE));
                args.add(Pair.with(transaction.getSignature(), JDBCType.BLOB));
                args.add(Pair.with(transaction.getTimestamp(), JDBCType.VARCHAR));
                args.add(Pair.with(transaction.getType().name(), JDBCType.VARCHAR));
            });
            StringBuilder sql = new StringBuilder("INSERT INTO TRANSACTIONS (\"FROM\", \"TO\", LEDGER_ID, AMOUNT, SIGNATURE, CREATED_ON, TYPE) VALUES");
            sql.append(String.join(",", Collections.nCopies(nonExistingTransactions.size(), " (?, ?, ?, ?, ?, ?, ?)")));
            sql.append(";");
            dbHandler.runScript(sql.toString(), args);
        } catch (Exception e) {
            System.err.println("Problem while saving all transactions: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public Optional<Transaction> findById(Integer integer) {
        try (DatabaseHandler dbHandler = new DatabaseHandler(BLOCKCHAIN_CONNECTION_URL)) {
            ResultSet resultSet = dbHandler.runScript("SELECT * FROM TRANSACTIONS WHERE ID = ?", new LinkedList<>() {{
                add(Pair.with(integer, JDBCType.INTEGER));
            }});
            if (resultSet.next()) {
                return Optional.of(new Transaction(
                        resultSet.getBytes("FROM"),
                        resultSet.getBytes("TO"),
                        resultSet.getDouble("AMOUNT"),
                        resultSet.getBytes("SIGNATURE"),
                        resultSet.getInt("LEDGER_ID"),
                        resultSet.getString("CREATED_ON"),
                        Transaction.Type.valueOf(resultSet.getString("TYPE"))
                ));
            }
        } catch (Exception e) {
            System.err.println("Problem while finding transaction by id: " + e.getMessage());
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public ArrayList<Transaction> findAll() {
        try (DatabaseHandler dbHandler = new DatabaseHandler(BLOCKCHAIN_CONNECTION_URL)) {
            ResultSet resultSet = dbHandler.runScript("SELECT * FROM TRANSACTIONS");
            return new ArrayList<>() {{
                while (resultSet.next()) {
                    add(new Transaction(
                            resultSet.getBytes("FROM"),
                            resultSet.getBytes("TO"),
                            resultSet.getDouble("AMOUNT"),
                            resultSet.getBytes("SIGNATURE"),
                            resultSet.getInt("LEDGER_ID"),
                            resultSet.getString("CREATED_ON"),
                            Transaction.Type.valueOf(resultSet.getString("TYPE"))
                    ));
                }
            }};
        } catch (Exception e) {
            System.err.println("Problem while finding all transactions: " + e.getMessage());
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    @Override
    public void removeAll() {
        try (DatabaseHandler dbHandler = new DatabaseHandler(BLOCKCHAIN_CONNECTION_URL)) {
            dbHandler.runScript("DELETE FROM TRANSACTIONS");
        } catch (Exception e) {
            System.err.println("Problem while removing all transactions: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public ArrayList<Transaction> findAllByLedgerId(int ledgerId) {
        try (DatabaseHandler dbHandler = new DatabaseHandler(BLOCKCHAIN_CONNECTION_URL)) {
            ResultSet resultSet = dbHandler.runScript("SELECT * FROM TRANSACTIONS WHERE LEDGER_ID = ?", new LinkedList<>() {{
                add(Pair.with(ledgerId, JDBCType.INTEGER));
            }});
            return new ArrayList<>() {{
                while (resultSet.next()) {
                    add(new Transaction(
                            resultSet.getBytes("FROM"),
                            resultSet.getBytes("TO"),
                            resultSet.getDouble("AMOUNT"),
                            resultSet.getBytes("SIGNATURE"),
                            resultSet.getInt("LEDGER_ID"),
                            resultSet.getString("CREATED_ON"),
                            Transaction.Type.valueOf(resultSet.getString("TYPE"))
                    ));
                }
            }};
        } catch (Exception e) {
            System.err.println("Problem while finding all transactions by ledger id: " + e.getMessage());
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public Double findAmountSumByReceiver(byte[] to) {
        try (DatabaseHandler dbHandler = new DatabaseHandler(BLOCKCHAIN_CONNECTION_URL)) {
            ResultSet resultSet = dbHandler.runScript("SELECT SUM(AMOUNT) FROM TRANSACTIONS WHERE \"TO\" = ?", new LinkedList<>() {{
                add(Pair.with(to, JDBCType.BLOB));
            }});
            return resultSet.getDouble(1);
        } catch (Exception e) {
            System.err.println("Problem while finding all transactions by ledger id: " + e.getMessage());
            e.printStackTrace();
        }
        return 0D;
    }
}
