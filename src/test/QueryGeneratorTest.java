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
        tup = new Tuple(3);
        tup.setField(0, new StringField("Adrian Sham"));
        tup.setField(1, new IntField(1266067));
        tup.setField(2, new StringField("computer science"));
        modified.add(tup);

        String res = QueryGenerator.generate(originalQuery, original, modified);
        boolean answer1 = "select students.name, students.id, students.major from students where (name = 'Adrian Sham')".equals(res);
        boolean answer2 = "select students.name, students.id, students.major from students where (id = 1266067)".equals(res);
        boolean answer3 = "select students.name, students.id, students.major from students where (id >= 1266067)".equals(res);
        assertTrue(res, answer1^answer2^answer3);
    }
    
    /**
     * Test adding where expressions
     * Original : 'empty'
     * Modified: where major='computer science'
     */
    @Test
    public void testWhereAddOne2() {
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
        tup = new Tuple(3);
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
    
    /**
     * Test case where we are looking for 2 expressions
     * WHERE major='computer science' AND gender="F"
     */
    @Test
    public void testWhereAddTwoStatements() {
        db.executeUpdate(DROP_TABLE);
        db.executeUpdate("CREATE table students ( name text, gender text, major text);");
        db.executeUpdate("INSERT into students values ('Adrian Sham', 'M', 'computer science');");
        db.executeUpdate("INSERT into students values ('Lindsey Nguyen', 'F', 'computer science');");
        db.executeUpdate("INSERT into students values ('Lily Sue', 'F', 'computer science');");
        db.executeUpdate("INSERT into students values ('Mary Sue', 'F', 'biology');");
        ZQuery originalQuery = Main.getQuery("SELECT * from students;");
        original = setupBaseTable2();
        Tuple tup = new Tuple(3);
        tup.setField(0, new StringField("Adrian Sham"));
        tup.setField(1, new StringField("M"));
        tup.setField(2, new StringField("computer science"));
        original.add(tup);
        
        tup = new Tuple(3);
        tup.setField(0, new StringField("Lindsey Nguyen"));
        tup.setField(1, new StringField("F"));
        tup.setField(2, new StringField("computer science"));
        original.add(tup);
        
        tup = new Tuple(3);
        tup.setField(0, new StringField("Lily Sue"));
        tup.setField(1, new StringField("F"));
        tup.setField(2, new StringField("computer science"));
        original.add(tup);
        
        tup = new Tuple(3);
        tup.setField(0, new StringField("Mary Sue"));
        tup.setField(1, new StringField("F"));
        tup.setField(2, new StringField("biology"));
        original.add(tup);
        
        modified = setupBaseTable2();
        tup = new Tuple(3);
        tup.setField(0, new StringField("Lindsey Nguyen"));
        tup.setField(1, new StringField("F"));
        tup.setField(2, new StringField("computer science"));
        modified.add(tup);
        
        tup = new Tuple(3);
        tup.setField(0, new StringField("Lily Sue"));
        tup.setField(1, new StringField("F"));
        tup.setField(2, new StringField("computer science"));
        modified.add(tup);
        
        String res = QueryGenerator.generate(originalQuery, original, modified);
        boolean ans1 = "select students.name, students.gender, students.major from students where ((gender = 'F') AND (major = 'computer science'))".equals(res);
        boolean ans2 = "select students.name, students.gender, students.major from students where ((major = 'computer science') AND (gender = 'F'))".equals(res);
        boolean ans3 = "select students.name, students.gender, students.major from students where ((name = 'Lindsey Nguyen') OR (name = 'Lily Sue'))".equals(res);
        boolean ans4 = "select students.name, students.gender, students.major from students where ((name = 'Lily Sue') OR (name = 'Lindsey Nguyen'))".equals(res);
        assertTrue(res, ans1^ans2^ans3^ans4);
    }
    
    /**
     * Do a test where OR statement makes sense
     * I.e. name = adrian or name = lindsey
     */
    @Test
    public void testOR() {
        db.executeUpdate(DROP_TABLE);
        db.executeUpdate("CREATE table students ( name text, age int);");
        db.executeUpdate("INSERT into students values ('Adrian Sham', 25);");
        db.executeUpdate("INSERT into students values ('Lindsey Nguyen', 16);");
        db.executeUpdate("INSERT into students values ('Lily Sue', 15);");
        db.executeUpdate("INSERT into students values ('Mary Sue', 17);");
        db.executeUpdate("INSERT into students values ('Larry Dave', 26);");
        ZQuery originalQuery = Main.getQuery("SELECT * from students;");
        original = setupBaseTable3();
        Tuple tup = new Tuple(2);
        tup.setField(0, new StringField("Adrian Sham"));
        tup.setField(1, new IntField(25));
        original.add(tup);
        
        tup = new Tuple(2);
        tup.setField(0, new StringField("Lindsey Nguyen"));
        tup.setField(1, new IntField(16));
        original.add(tup);
        
        tup = new Tuple(2);
        tup.setField(0, new StringField("Lily Sue"));
        tup.setField(1, new IntField(15));
        original.add(tup);
        
        tup = new Tuple(2);
        tup.setField(0, new StringField("Mary Sue"));
        tup.setField(1, new IntField(17));
        original.add(tup);
        
        tup = new Tuple(2);
        tup.setField(0, new StringField("Larry Dave"));
        tup.setField(1, new IntField(26));
        original.add(tup);

        
        modified = setupBaseTable3();
        tup = new Tuple(2);
        tup.setField(0, new StringField("Adrian Sham"));
        tup.setField(1, new IntField(25));
        modified.add(tup);
        
        tup = new Tuple(2);
        tup.setField(0, new StringField("Lindsey Nguyen"));
        tup.setField(1, new IntField(16));
        modified.add(tup);

        String res = QueryGenerator.generate(originalQuery, original, modified);
        System.out.println(res);
        int count = 0;
        if (res.contains("Adrian Sham")) {
            count++;
        }
        if (res.contains("Lindsey Nguyen")) {
            count++;
        }
        if (res.contains("age = 16")) {
            count++;
        }
        if (res.contains("age = 25")) {
            count++;
        }
        //check to see that the generated query contains exactly 2 of the above strings
        assertEquals(2, count);
    }
    
    /**
     * See if query generator can find >=
     */
    @Test
    public void testWhereNumbers() {
        db.executeUpdate(DROP_TABLE);
        db.executeUpdate("CREATE table students ( name text, age int);");
        db.executeUpdate("INSERT into students values ('Adrian Sham', 25);");
        db.executeUpdate("INSERT into students values ('Lindsey Nguyen', 21);");
        db.executeUpdate("INSERT into students values ('Lily Sue', 15);");
        db.executeUpdate("INSERT into students values ('Mary Sue', 17);");
        ZQuery originalQuery = Main.getQuery("SELECT * from students;");
        original = setupBaseTable3();
        Tuple tup = new Tuple(2);
        tup.setField(0, new StringField("Adrian Sham"));
        tup.setField(1, new IntField(25));
        original.add(tup);
        
        tup = new Tuple(2);
        tup.setField(0, new StringField("Lindsey Nguyen"));
        tup.setField(1, new IntField(21));
        original.add(tup);
        
        tup = new Tuple(2);
        tup.setField(0, new StringField("Lily Sue"));
        tup.setField(1, new IntField(15));
        original.add(tup);
        
        tup = new Tuple(2);
        tup.setField(0, new StringField("Mary Sue"));
        tup.setField(1, new IntField(17));
        original.add(tup);

        
        modified = setupBaseTable3();
        tup = new Tuple(2);
        tup.setField(0, new StringField("Adrian Sham"));
        tup.setField(1, new IntField(25));
        modified.add(tup);
        
        tup = new Tuple(2);
        tup.setField(0, new StringField("Lindsey Nguyen"));
        tup.setField(1, new IntField(21));
        modified.add(tup);

        String res = QueryGenerator.generate(originalQuery, original, modified);
        assertEquals("select students.name, students.age from students where (age >= 21)", res);
    }
    
    private Table setupBaseTable2() {
        List<Type> typeList = new ArrayList<Type>();
        typeList.add(Type.TEXT);
        typeList.add(Type.TEXT);
        typeList.add(Type.TEXT);
        List<String> nameList =  new ArrayList<String>();
        nameList.add("name");
        nameList.add("gender");
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
     * Setup table with name text, age int
     * @return
     */
    private Table setupBaseTable3() {
        List<Type> typeList = new ArrayList<Type>();
        typeList.add(Type.TEXT);
        typeList.add(Type.INT);
        List<String> nameList =  new ArrayList<String>();
        nameList.add("name");
        nameList.add("age");
        List<String> aliasList =  new ArrayList<String>();
        aliasList.add(null);
        aliasList.add(null);
        List<String> tableList =  new ArrayList<String>();
        tableList.add("students");
        tableList.add("students");
        return new Table(new TableDescriptor(typeList, nameList, aliasList, tableList));
    }
}
