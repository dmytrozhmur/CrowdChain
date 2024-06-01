package ua.nure.crowdchainnode.util;

import java.sql.*;
import java.util.LinkedList;
import java.util.Optional;

import org.sqlite.core.CoreStatement;
import org.sqlite.jdbc4.JDBC4ResultSet;

import ua.nure.crowdchainnode.util.tuples.Pair;

import static java.sql.JDBCType.*;

public class DatabaseHandler implements AutoCloseable {
    protected Connection connection;
    protected Statement statement;
    protected PreparedStatement preparedStatement;
    protected ResultSet resultSet;

    public DatabaseHandler(String connectionUrl) throws SQLException {
        this.connection = DriverManager.getConnection(connectionUrl);
    }

    @Override
    public void close() throws SQLException {
        if (resultSet != null) resultSet.close();
        Optional.ofNullable(statement).orElse(preparedStatement).close();
        connection.close();
    }

    public ResultSet runScript(String sql) throws SQLException {
        statement = connection.createStatement();

        if (sql.trim().startsWith("SELECT")) {
            resultSet = statement.executeQuery(sql);
        } else {
            statement.executeUpdate(sql);
        }

        return resultSet;
    }
    public ResultSet runScript(String sql, LinkedList<Pair<Object, SQLType>> args) throws SQLException {
        preparedStatement = connection.prepareStatement(sql);

        int counter = 1;
        for (Pair<Object, SQLType> entry : args) {
            switch (entry.getValue1()) {
                case BLOB:
                    preparedStatement.setBytes(counter++, (byte[]) entry.getValue0());
                    break;
                case INTEGER:
                    preparedStatement.setInt(counter++, (int) entry.getValue0());
                    break;
                case DOUBLE:
                    preparedStatement.setDouble(counter++, (double) entry.getValue0());
                    break;
                case VARCHAR:
                    preparedStatement.setString(counter++, (String) entry.getValue0());
                    break;
                default:
                    throw new SQLException("Unknown SQLType");
            }
        }

        if (sql.trim().startsWith("SELECT")) {
            resultSet = preparedStatement.executeQuery();
        } else {
            preparedStatement.executeUpdate();
        }

        return resultSet;
    }
}
