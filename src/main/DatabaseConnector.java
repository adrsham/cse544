package main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;
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
	
	/*Integer type name*/
	public static final String INT_TYPE_NAME = "int";
	/*Text type name*/
	public static final String STRING_TYPE_NAME = "text";
	
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
			db = this;
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
				buf.append(rsmd.getColumnLabel(i) + " (" + rsmd.getColumnTypeName(i) + ")\t");
/*				buf.append(String.format("%"+rsmd.getColumnLabel(i).length()+"s (%"+rsmd.getColumnTypeName(i).length()+"s)   ", 
						rsmd.getColumnLabel(i), rsmd.getColumnTypeName(i)));*/
			}
			buf.append(System.lineSeparator());
			//store line
			
			for (int i = 0; i < 10; i++) {
				buf.append("-");
			}
			buf.append(System.lineSeparator());
			//store tuples
			
			while (rs.next()) {
				for (int i = 1; i <= columnCount; i++) {
					if (rsmd.getColumnType(i) == Types.INTEGER) {
						//buf.append(String.format("%-"+getColumnWidth(rsmd, i)+"s ", rs.getInt(i)));
						buf.append(rs.getInt(i) + "\t");
					} else {
						//text
						//buf.append(String.format("%-"+getColumnWidth(rsmd, i)+"s ", rs.getString(i)));
						buf.append(rs.getString(i) + "\t");
					}
				}
				buf.append(System.lineSeparator());
			}
			
			
		} catch (SQLException e) {
			LOG.log(Level.SEVERE, "Failed to execute query", e);
		}
		return buf.toString();
	}
	
	/**
	 * Slightly modified version of runSQL that takes dbInfo to get the column widths
	 * 
	 * @param query SQL query
	 * @param dbInfo database information
	 * @return result string
	 */
	public String runSQL (String query, Map<String, TableDescriptor> dbInfo) {
		StringBuilder buf = new StringBuilder();
		try (PreparedStatement q = con.prepareStatement(query)) {
			ResultSet rs = q.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();
			//store column names
			for (int i = 1; i <= columnCount; i++) {
				//print column
				buf.append(String.format("%-"+dbInfo.get(rsmd.getTableName(i)).getFieldLength(rsmd.getColumnName(i))+"s\t", 
						rsmd.getColumnLabel(i) + " (" + rsmd.getColumnTypeName(i) + ")"));
			}
			buf.append(System.lineSeparator());
			//store line
			int colsWidth = 0;
			for (int i = 1; i <= columnCount; i++) {
				colsWidth += getColumnWidth(rsmd, i, dbInfo);
			}
			for (int i = 0; i < colsWidth + 7*(columnCount-1); i++) {
				buf.append("-");
			}
			buf.append(System.lineSeparator());
			//store tuples
			
			while (rs.next()) {
				for (int i = 1; i <= columnCount; i++) {
					if (rsmd.getColumnType(i) == Types.INTEGER) {
						buf.append(String.format("%-" +dbInfo.get(rsmd.getTableName(i)).getFieldLength(rsmd.getColumnName(i))+"d\t", rs.getInt(i)));
					} else {
						//text
						buf.append(String.format("%-" +dbInfo.get(rsmd.getTableName(i)).getFieldLength(rsmd.getColumnName(i))+"s\t", rs.getString(i)));
					}
				}
				buf.append(System.lineSeparator());
			}
		} catch (SQLException e) {
			LOG.log(Level.SEVERE, "Failed to execute query", e);
		}
		return buf.toString();
	}
	
	/**
	 * Use this function for executing queries that have no result, such
	 * as insert or update
	 * 
	 * @param query SQL query
	 * @return num of affected rows
	 */
	public int executeUpdate(String query) {
		try (PreparedStatement q = con.prepareStatement(query)) {
			return q.executeUpdate();
		} catch (SQLException e) {
			LOG.log(Level.SEVERE, "Failed to execute query", e);
			return -1;
		}
		
	}
	
	/**
	 * Calculate the width of the column
	 * 
	 * @param rsmd
	 * @param i
	 * @param dbInfo
	 * @return value returned will be the max of the title and field lengths
	 * @throws SQLException
	 */
	private int getColumnWidth(ResultSetMetaData rsmd, int i, Map<String, TableDescriptor> dbInfo) throws SQLException {
			return Math.max(dbInfo.get(rsmd.getTableName(i)).getFieldLength(rsmd.getColumnName(i)), rsmd.getColumnLabel(i).length()+7);
	}
}
