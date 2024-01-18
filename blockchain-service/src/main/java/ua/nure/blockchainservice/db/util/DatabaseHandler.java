package ua.nure.blockchainservice.db.util;

import java.security.NoSuchAlgorithmException;
import java.sql.*;

public abstract class DatabaseHandler implements AutoCloseable {
    protected Connection connection;
    protected Statement statement;
    protected ResultSet resultSet;

    protected DatabaseHandler(String connectionUrl) throws SQLException {
        this.connection = DriverManager.getConnection(connectionUrl);
        this.statement = connection.createStatement();
    }

    @Override
    public void close() throws SQLException {
        resultSet.close();
        statement.close();
        resultSet.close();
    }

    public abstract void initDatabase() throws Exception;
}
