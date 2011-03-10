package net.sf.extjwnl.dictionary.database;

import net.sf.extjwnl.JWNLRuntimeException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Helper class to manage queries.
 *
 * @author John Didion <jdidion@didion.net>
 */
public class Query {
    private Connection connection;
    private PreparedStatement statement;
    private ResultSet results;
    private String sql;

    public Query(String sql, Connection conn) {
        connection = conn;
        this.sql = sql;
    }

    public ResultSet execute() throws SQLException {
        if (isExecuted()) {
            throw new JWNLRuntimeException("DICTIONARY_EXCEPTION_025");
        }
        return (results = (getStatement().execute()) ? getStatement().getResultSet() : null);
    }

    public boolean isExecuted() {
        return (results != null);
    }

    public Connection getConnection() {
        return connection;
    }

    public PreparedStatement getStatement() throws SQLException {
        if (statement == null) {
            statement = connection.prepareStatement(sql);
        }
        return statement;
    }

    public ResultSet getResults() {
        return results;
    }

    public void close() {
        if (results != null) {
            try {
                results.close();
                results = null;
            } catch (SQLException ex) {
                //nop
            }
        }
        if (statement != null) {
            try {
                statement.close();
                statement = null;
            } catch (SQLException ex) {
                //nop
            }
        }
        if (connection != null) {
            try {
                connection.close();
                connection = null;
            } catch (SQLException ex) {
                //nop
            }
        }
    }
}