package main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DatabaseConnector class will be used to connect to the
 * PostgreSQL DB and execute queries. 
 * 
 * Will only return the result string
 * 
 * @author adrian
 *
 */
public class DatabaseConnector {
	private static final Logger LOG = Logger.getLogger(DatabaseConnector.class.getName());
	private static DatabaseConnector db = null;
	private Connection con = null;
	
	private DatabaseConnector() {
		
	}
	
	public static DatabaseConnector getInstance() {
		if (db == null) {
			return new DatabaseConnector();
		} else {
			return db;
		}
	}
	
	/**
	 * Use this function to connect to a database
	 * @param dbName database name
	 * @param userName username
	 * @param password password
	 */
	public void connect(String dbName, String userName, String password) {
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			LOG.log(Level.SEVERE, "Failed to load class", e);
		}
		
		try {
			con = DriverManager.getConnection("jdbc:postgresql://localhost:5432/" + dbName, 
					userName, password);
		} catch (SQLException e) {
			LOG.log(Level.SEVERE, "Failed to connect to db", e);
		}
	}
	
	public boolean connectionValid () {
		return con != null;
	}
	
	/**
	 * Run the provided SQL query and return a string of the results
	 * 
	 * @param query SQL query
	 * @return result string
	 */
	public String runSQL (String query) {
		StringBuilder buf = new StringBuilder();
		try (PreparedStatement q = con.prepareStatement(query)) {
			ResultSet rs = q.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();
			//store column names
			for (int i = 1; i <= columnCount; i++) {
				//print column
				buf.append(String.format("%"+rsmd.getColumnLabel(i).length()+"s (%"+rsmd.getColumnTypeName(i).length()+"s)  ", 
						rsmd.getColumnLabel(i), rsmd.getColumnTypeName(i)));
			}
			buf.append(System.lineSeparator());
			//store line
			int colsWidth = 0;
			for (int i = 1; i <= columnCount; i++) {
				colsWidth += getColumnWidth(rsmd, i);
			}
			for (int i = 0; i < colsWidth; i++) {
				buf.append("-");
			}
			buf.append(System.lineSeparator());
			//store tuples
			
			while (rs.next()) {
				for (int i = 1; i <= columnCount; i++) {
					if (rsmd.getColumnType(i) == Types.INTEGER) {
						buf.append(String.format("%-"+getColumnWidth(rsmd, i)+"s", rs.getInt(i)));
					} else {
						//text
						buf.append(String.format("%-"+getColumnWidth(rsmd, i)+"s", rs.getString(i)));
					}
				}
				buf.append(System.lineSeparator());
			}
			
			
		} catch (SQLException e) {
			LOG.log(Level.SEVERE, "Failed to execute query", e);
		}
		return buf.toString();
	}
	
	private int getColumnWidth(ResultSetMetaData rsmd, int i) throws SQLException {
			return rsmd.getColumnLabel(i).length() + rsmd.getColumnTypeName(i).length() + 5;
	}
}
