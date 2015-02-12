package test;

import static org.junit.Assert.assertTrue;
import main.DatabaseConnector;

import org.junit.Test;

public class DatabaseConnectorTest {
	/**Test database user name*/
	public static final String USER = "user";
	/**Test database password*/
	public static final String PASSWORD = "password";
	/**Test database name*/
	public static final String DB = "testdb";
	
	public static final String CREATE = "create table students ( name text, id text);";
	public static final String INSERT = "insert into students values ('adrian', '1266067');";
	
	@Test
	public void connectTest() {
		DatabaseConnector db = DatabaseConnector.getInstance();
		db.connect(DB, USER, PASSWORD);
		assertTrue(db.connectionValid());
		db.executeUpdate(CREATE);
		db.executeUpdate(INSERT);
		//just make sure that the query executed, and we get some sort of result
		assertTrue(db.runSQL("SELECT * FROM students;").contains("adrian"));
		System.out.println(db.runSQL("SELECT * FROM students;"));
		assertTrue(db.runSQL("SELECT * FROM students;").contains("1266067"));
		db.executeUpdate("DROP TABLE students;");
	}
	
	@Test
	public void intConnectionTest() {
		DatabaseConnector db = DatabaseConnector.getInstance();
		db.connect(DB, USER, PASSWORD);
		assertTrue(db.connectionValid());
		db.executeUpdate("CREATE TABLE num (name text, id int);");
		db.executeUpdate("INSERT INTO num VALUES ('adrian', 1266067);");
		//just make sure that the query executed, and we got some sort of result
		assertTrue(db.runSQL("SELECT * FROM num;").contains("adrian"));
		assertTrue(db.runSQL("SELECT * FROM num;").contains("1266067"));
		db.runSQL("SELECT * FROM num;");
		db.executeUpdate("DROP TABLE num;");
	}
}
