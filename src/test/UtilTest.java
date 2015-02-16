package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import main.DatabaseConnector;
import main.Table;
import main.TableDescriptor;
import main.Util;
import main.Util.Type;

import org.junit.Test;

public class UtilTest {

    public static final String LS = System.lineSeparator();
	public static final String STRING_STRING_TABLE = "name ("+DatabaseConnector.STRING_TYPE_NAME+") \tid ("+DatabaseConnector.STRING_TYPE_NAME+")  " + LS + 
													 "------------------------" + LS + 
													 "adrian \t 1266067    " + LS;
	public static final String STRING_INT_TABLE = "name ("+DatabaseConnector.STRING_TYPE_NAME+") \tid ("+DatabaseConnector.INT_TYPE_NAME+")  " + LS + 
			 									  "------------------------" + LS + 
			 									  "adrian \t 1266067    " + LS;

	@Test
	public void testReadWrite() {
		Path path = Paths.get("testFile");
		try {
			Files.deleteIfExists(path);// clean
			Util.writeToFile("testFile", "test Content");
			assertTrue(Files.exists(path));
			assertEquals("test Content" + System.lineSeparator(),
					Util.readFromFile("testFile"));
			path.toFile().deleteOnExit();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Try parsing a table with 2 string columns
	 */
	@Test
	public void parseTableStringString() {
		Table t = Util.parseStringToTable(STRING_STRING_TABLE);
		assertEquals(1, t.size());
		TableDescriptor td = t.getTD();
		assertEquals("name", td.getAliasName(0));
		assertEquals(Type.TEXT, td.getFieldType(0));
		assertEquals("id", td.getAliasName(1));
		assertEquals(Type.TEXT, td.getFieldType(1));
	}

	/**
	 * Try parsing a table with string and int columns
	 */
	@Test
	public void parseTableStringInt() {
		Table t = Util.parseStringToTable(STRING_INT_TABLE);
		assertEquals(1, t.size());
		TableDescriptor td = t.getTD();
		assertEquals(td.type, TableDescriptor.TableType.QUERY_RESULTS);
		assertEquals("name", td.getAliasName(0));
		assertEquals(Type.TEXT, td.getFieldType(0));
		assertEquals("id", td.getAliasName(1));
		assertEquals(Type.INT, td.getFieldType(1));
	}
	
	/**
	 * Try parsing something from the DB query
	 */
	@Test
	public void parseFromDB() {
		DatabaseConnector db = DatabaseConnector.getInstance();
		db.connect(DatabaseConnectorTest.DB, DatabaseConnectorTest.USER, DatabaseConnectorTest.PASSWORD);
		db.executeUpdate("DROP TABLE IF EXISTS num;");
		db.executeUpdate("CREATE table num (name text, id int);");
		db.executeUpdate("INSERT INTO num VALUES ('adrian', 1266067);");
		Table t = Util.parseStringToTable(db.runSQL("SELECT * FROM num;"));
		
		TableDescriptor td = t.getTD();
        assertEquals(td.type, TableDescriptor.TableType.QUERY_RESULTS);
		assertEquals(1, t.size());
		  assertEquals("name", td.getAliasName(0));
		assertEquals(Type.TEXT, td.getFieldType(0));
        assertEquals("id", td.getAliasName(1));
		assertEquals(Type.INT, td.getFieldType(1));
		db.executeUpdate("DROP TABLE num;");
	}
	
	/**
	 * Try parsing a column where the elements in the column are too wide
	 */
	@Test
	public void parseWideColumn() {
		DatabaseConnector db = DatabaseConnector.getInstance();
		db.connect(DatabaseConnectorTest.DB, DatabaseConnectorTest.USER, DatabaseConnectorTest.PASSWORD);
		db.executeUpdate("DROP TABLE IF EXISTS num;");
		db.executeUpdate("CREATE table num (name text, id int);");
		db.executeUpdate("INSERT INTO num VALUES ('superrrrrrrrrlllllooooonnnnnnggggg', 1266067);");
		Table t = Util.parseStringToTable(db.runSQL("SELECT * FROM num;"));
		
		TableDescriptor td = t.getTD();
		assertEquals(1, t.size());
		assertEquals("name", td.getAliasName(0));
		assertEquals(Type.TEXT, td.getFieldType(0));
		assertEquals("id", td.getAliasName(1));
		assertEquals(Type.INT, td.getFieldType(1));
		db.executeUpdate("DROP TABLE num;");
	}
}
