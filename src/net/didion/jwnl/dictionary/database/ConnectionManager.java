package net.didion.jwnl.dictionary.database;

import net.didion.jwnl.JWNLException;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class ConnectionManager {
	private String _driverClass;
	private String _url;
	private String _userName;
	private String _password;
    private boolean _registered;
    private DataSource _source = null;
    private String _jndi;

	public ConnectionManager(String driverClass, String url, String userName, String password) {
		_driverClass = driverClass;
		_url = url;
		_userName = userName;
		_password = password;
	}

	public ConnectionManager(String jndi) {
		_jndi = jndi;
	}


	public Query getQuery(String sql) throws SQLException, JWNLException {
		return new Query(sql, getConnection());
	}

	public Connection getConnection() throws SQLException, JWNLException {

		 if (_jndi != null){
		     try {
	             if (_source != null) {
	                 return _source.getConnection();
	             }
	             Context initContext = new InitialContext();
	             Context envContext  = (Context) initContext.lookup("java:/comp/env");
	             DataSource ds = (DataSource) envContext.lookup(_jndi);
	             if (ds != null) {
	            	 return ds.getConnection();
	             }
	         } catch (NamingException ne) {
	        	 throw new JWNLException("JNDI_NAMING_EXCEPTION", ne);
	         }
		}
		registerDriver();
	    if (_userName == null ) {
	    	return  DriverManager.getConnection(_url);
	    } else {
	    	return  DriverManager.getConnection(
				_url, _userName, (_password != null) ? _password : "");
	    }


	}

	private void registerDriver() throws JWNLException {
		if (!_registered) {
			try {
				Driver driver = (Driver) Class.forName(_driverClass).newInstance();
				DriverManager.registerDriver(driver);
				_registered = true;
			} catch (Exception ex) {
				throw new JWNLException("DICTIONARY_EXCEPTION_024", ex);
			}
		}
	}
}