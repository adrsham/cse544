package test;

import main.DatabaseConnector;

import org.junit.Test;

public class DatabaseConnectorTest {
	
	public static final String CREATE = "create table students ( name text, id text);";
	public static final String INSERT = "insert into students values ('adrian', '1266067');";
	
	@Test
	public void connectTest() {
		DatabaseConnector db = DatabaseConnector.getInstance();
		db.connect("testdb", "adrian", "test");
		System.out.println(db.runSQL("SELECT * FROM students;"));
	}
}
