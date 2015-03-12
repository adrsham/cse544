package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import main.DatabaseConnector;
import main.IntField;
import main.Main;
import main.QueryGenerator;
import main.StringField;
import main.Table;
import main.TableDescriptor;
import main.Tuple;
import main.Util;
import main.Util.Type;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import Zql.ZQuery;

public class QueryGeneratorTest {

    /**Test database user name*/
    public static final String USER = "user";
    /**Test database password*/
    public static final String PASSWORD = "password";
    /**Test database name*/
    public static final String DB = "testdb";
    public static final String DROP_TABLE = "DROP TABLE IF EXISTS students;";
    public static final String CREATE = "create table students ( name text, id int, major text);";
    public static final String INSERT1 = "insert into students values ('Adrian Sham', 1266067, 'computer science'); ";
    public static final String INSERT2 = "insert into students values ('Lindsey Nguyen', 1130418, 'computer science');";
    public static final String INSERT3 = "insert into students values ('Bob Sue', 1234567, 'biology');";

    public static final String SQL = "select name, id from students;";
    
    public static DatabaseConnector db;
    public static ZQuery query;
    public static Table original;
    public static Table modified;

    @Before
    public void testBefore() {
        db = DatabaseConnector.getInstance();
        db.connect(DB, USER, PASSWORD);
        assertTrue(db.connectionValid());
        db.executeUpdate(DROP_TABLE);
        db.executeUpdate(CREATE);
        db.executeUpdate(INSERT1);
        db.executeUpdate(INSERT2);
        db.executeUpdate(INSERT3);
        Util.getDatabaseInfo();
        query = Main.getQuery(SQL);
        List<Type> typeList = new ArrayList<Type>();
        typeList.add(Type.TEXT);
        typeList.add(Type.INT);
        List<String> nameList =  new ArrayList<String>();
        nameList.add("name");
        nameList.add("id");
        List<String> aliasList =  new ArrayList<String>();
        aliasList.add(null);
        aliasList.add(null);
        List<String> tableList =  new ArrayList<String>();
        tableList.add("students");
        tableList.add("students");
        original = new Table(new TableDescriptor(typeList, nameList, aliasList, tableList));
        Tuple tup = new Tuple(2);
        tup.setField(0, new StringField("Adrian Sham"));
        tup.setField(1, new IntField(1266067));
        original.add(tup);

        tup = new Tuple(2);
        tup.setField(0, new StringField("Lindsey Nguyen"));
        tup.setField(1, new IntField(1130418));
        original.add(tup);

        tup = new Tuple(2);
        tup.setField(0, new StringField("Bob Sue"));
        tup.setField(1, new IntField(1234567));
        original.add(tup);
    }

    @After
    public void testAfter() {
        db.executeUpdate(DROP_TABLE);
    }

    @Test
    public void testGetSameTableQuery() {   
        List<Type> typeList = new ArrayList<Type>();
        typeList.add(Type.TEXT);
        typeList.add(Type.INT);
        List<String> nameList =  new ArrayList<String>();
        nameList.add("name");
        nameList.add("id");
        List<String> aliasList =  new ArrayList<String>();
        aliasList.add(null);
        aliasList.add(null);
        List<String> tableList =  new ArrayList<String>();
        tableList.add("students");
        tableList.add("students");
        modified = new Table(new TableDescriptor(typeList, nameList, aliasList, tableList));
        Tuple tup = new Tuple(2);
        tup.setField(0, new StringField("Adrian Sham"));
        tup.setField(1, new IntField(1266067));
        modified.add(tup);

        tup = new Tuple(2);
        tup.setField(0, new StringField("Lindsey Nguyen"));
        tup.setField(1, new IntField(1130418));
        modified.add(tup);

        tup = new Tuple(2);
        tup.setField(0, new StringField("Bob Sue"));
        tup.setField(1, new IntField(1234567));
        modified.add(tup);
        String res = QueryGenerator.generate(query, original, modified);
        assertEquals("select students.name, students.id from students", res);
    }

    @Test
    public void testGetMinusColTableQuery() {
        List<Type> typeList = new ArrayList<Type>();
        typeList.add(Type.TEXT);
        List<String> nameList =  new ArrayList<String>();
        nameList.add("name");
        List<String> aliasList =  new ArrayList<String>();
        aliasList.add(null);
        List<String> tableList =  new ArrayList<String>();
        tableList.add("students");
        modified = new Table(new TableDescriptor(typeList, nameList, aliasList, tableList));
        Tuple tup = new Tuple(1);
        tup.setField(0, new StringField("Adrian Sham"));
        modified.add(tup);

        tup = new Tuple(1);
        tup.setField(0, new StringField("Lindsey Nguyen"));
        modified.add(tup);
        
        tup = new Tuple(1);
        tup.setField(0, new StringField("Bob Sue"));
        modified.add(tup);

        String res = QueryGenerator.generate(query, original, modified);
        assertEquals("select students.name from students", res);
    }

    @Test
    public void testGetAddColTableQuery() {
        List<Type> typeList = new ArrayList<Type>();
        typeList.add(Type.TEXT);
        typeList.add(Type.INT);
        typeList.add(Type.TEXT);
        List<String> nameList =  new ArrayList<String>();
        nameList.add("name");
        nameList.add("id");
        nameList.add("major");
        List<String> aliasList =  new ArrayList<String>();
        aliasList.add(null);
        aliasList.add(null);
        aliasList.add(null);
        List<String> tableList =  new ArrayList<String>();
        tableList.add("students");
        tableList.add("students");
        tableList.add("students");
        modified = new Table(new TableDescriptor(typeList, nameList, aliasList, tableList));
        Tuple tup = new Tuple(3);
        tup.setField(0, new StringField("Adrian Sham"));
        tup.setField(1, new IntField(1266067));
        tup.setField(2, new StringField("computer science"));
        modified.add(tup);

        tup = new Tuple(3);
        tup.setField(0, new StringField("Lindsey Nguyen"));
        tup.setField(1, new IntField(1130418));
        tup.setField(2, new StringField("computer science"));
        modified.add(tup);

        tup = new Tuple(3);
        tup.setField(0, new StringField("Bob Sue"));
        tup.setField(1, new IntField(1234567));
        tup.setField(2, new StringField("biology"));
        modified.add(tup);
        String res = QueryGenerator.generate(query, original, modified);
        assertEquals("select students.name, students.id, students.major from students", res);
    }
    
    /**
     * Test number of where expression from 1 to 0
     * Original where name='Adrian Sham'
     * Modified should be empty
     */
    @Test
    public void testWhereRemoveOne() {
        ZQuery originalQuery = Main.getQuery("SELECT * from students where name='Adrian Sham';");
        original = setupBaseTable();
        
        Tuple tup = new Tuple(3);
        tup.setField(0, new StringField("Adrian Sham"));
        tup.setField(1, new IntField(1266067));
        tup.setField(2, new StringField("computer science"));
        original.add(tup);
        
        modified = setupBaseTable();
        tup.setField(0, new StringField("Adrian Sham"));
        tup.setField(1, new IntField(1266067));
        tup.setField(2, new StringField("computer science"));
        modified.add(tup);

        tup = new Tuple(3);
        tup.setField(0, new StringField("Lindsey Nguyen"));
        tup.setField(1, new IntField(1130418));
        tup.setField(2, new StringField("computer science"));
        modified.add(tup);

        tup = new Tuple(3);
        tup.setField(0, new StringField("Bob Sue"));
        tup.setField(1, new IntField(1234567));
        tup.setField(2, new StringField("biology"));
        modified.add(tup);
        
        String res = QueryGenerator.generate(originalQuery, original, modified);
        assertEquals("select students.name, students.id, students.major from students", res);
    }
    
    /**
     * Helper method for setting up a 3 column base table
     * @return Table
     */
    private Table setupBaseTable() {
        List<Type> typeList = new ArrayList<Type>();
        typeList.add(Type.TEXT);
        typeList.add(Type.INT);
        typeList.add(Type.TEXT);
        List<String> nameList =  new ArrayList<String>();
        nameList.add("name");
        nameList.add("id");
        nameList.add("major");
        List<String> aliasList =  new ArrayList<String>();
        aliasList.add(null);
        aliasList.add(null);
        aliasList.add(null);
        List<String> tableList =  new ArrayList<String>();
        tableList.add("students");
        tableList.add("students");
        tableList.add("students");
        return new Table(new TableDescriptor(typeList, nameList, aliasList, tableList));
    }
    
    /**
     * Test number of where expression from 2 to 1
     * 
     * original where name='Adrian Sham' and major='computer science'
     * 
     * modified should get major='computer science'
     */
    @Test
    public void testWhereTwoToOne() {
        ZQuery originalQuery = Main.getQuery("SELECT * from students where name='Adrian Sham' AND major='computer science';");
        original = setupBaseTable();
        Tuple tup = new Tuple(3);
        tup.setField(0, new StringField("Adrian Sham"));
        tup.setField(1, new IntField(1266067));
        tup.setField(2, new StringField("computer science"));
        original.add(tup);
        
        modified = setupBaseTable();
        tup.setField(0, new StringField("Adrian Sham"));
        tup.setField(1, new IntField(1266067));
        tup.setField(2, new StringField("computer science"));
        modified.add(tup);

        tup = new Tuple(3);
        tup.setField(0, new StringField("Lindsey Nguyen"));
        tup.setField(1, new IntField(1130418));
        tup.setField(2, new StringField("computer science"));
        modified.add(tup);

        String res = QueryGenerator.generate(originalQuery, original, modified);
        assertEquals("select students.name, students.id, students.major from students where (major = 'computer science')", res);
    }
    
    /**
     * Test adding where expressions
     * Original : 'empty'
     * Modified: where name='Adrian Sham'
     */
    @Test
    public void testWhereAddOne() {
        ZQuery originalQuery = Main.getQuery("SELECT * from students;");
        original = setupBaseTable();
        Tuple tup = new Tuple(3);
        tup.setField(0, new StringField("Adrian Sham"));
        tup.setField(1, new IntField(1266067));
        tup.setField(2, new StringField("computer science"));
        original.add(tup);
        
        tup = new Tuple(3);
        tup.setField(0, new StringField("Lindsey Nguyen"));
        tup.setField(1, new IntField(1130418));
        tup.setField(2, new StringField("computer science"));
        original.add(tup);

        tup = new Tuple(3);
        tup.setField(0, new StringField("Bob Sue"));
        tup.setField(1, new IntField(1234567));
        tup.setField(2, new StringField("biology"));
        original.add(tup);
        
        modified = setupBaseTable();
        tup.setField(0, new StringField("Adrian Sham"));
        tup.setField(1, new IntField(1266067));
        tup.setField(2, new StringField("computer science"));
        modified.add(tup);

        String res = QueryGenerator.generate(originalQuery, original, modified);
        assertEquals("select students.name, students.id, students.major from students where (name = 'Adrian Sham')", res);
    }
    
    /**
     * Test the case where one is remove and one is added
     */
    @Test
    public void testWhereAddOneRemoveOne() {
        ZQuery originalQuery = Main.getQuery("SELECT * from students where name='Adrian Sham';");
        original = setupBaseTable();
        Tuple tup = new Tuple(3);
        tup.setField(0, new StringField("Adrian Sham"));
        tup.setField(1, new IntField(1266067));
        tup.setField(2, new StringField("computer science"));
        original.add(tup);
        
        modified = setupBaseTable();
        tup.setField(0, new StringField("Adrian Sham"));
        tup.setField(1, new IntField(1266067));
        tup.setField(2, new StringField("computer science"));
        modified.add(tup);
        
        tup = new Tuple(3);
        tup.setField(0, new StringField("Lindsey Nguyen"));
        tup.setField(1, new IntField(1130418));
        tup.setField(2, new StringField("computer science"));
        modified.add(tup);

        String res = QueryGenerator.generate(originalQuery, original, modified);
        assertEquals("select students.name, students.id, students.major from students where (major = 'computer science')", res);
    }
}
