package net.sf.extjwnl.dictionary.database;

import net.sf.extjwnl.JWNLRuntimeException;
import net.sf.extjwnl.dictionary.Dictionary;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Connection manager for database-backed dictionaries.
 *
 * @author John Didion (jdidion@didion.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class ConnectionManager {

    private final Dictionary dictionary;
    private final String driverClass;
    private final String url;
    private final String userName;
    private final String password;
    private Connection connection;

    public ConnectionManager(Dictionary dictionary, String driverClass, String url, String userName, String password) {
        this.dictionary = dictionary;
        this.driverClass = driverClass;
        this.url = url;
        this.userName = userName;
        this.password = password;
        registerDriver();
    }

    public Query getQuery(String sql) throws SQLException {
        return new Query(dictionary, sql, getConnection());
    }

    public Connection getConnection() throws SQLException {
        if (null != connection) {
            return connection;
        }
        if (userName == null) {
            connection = DriverManager.getConnection(url);
            connection.setReadOnly(true);
            return connection;
        } else {
            connection = DriverManager.getConnection(url, userName, (password != null) ? password : "");
            connection.setReadOnly(true);
            return connection;
        }
    }

    private void registerDriver() {
        try {
            Driver driver = (Driver) Class.forName(driverClass).newInstance();
            DriverManager.registerDriver(driver);
        } catch (SQLException | ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            throw new JWNLRuntimeException(dictionary.getMessages().resolveMessage("DICTIONARY_EXCEPTION_024", driverClass), e);
        }
    }

    public void close() {
        if (null != connection) {
            try {
                connection.close();
            } catch (SQLException e) {
                //nop
            }
            connection = null;
        }
    }
}