package test;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import main.DatabaseConnector;
import main.IntField;
import main.Main;
import main.QueryGenerator;
import main.StringField;
import main.Table;
import main.TableDescriptor;
import main.Tuple;
import main.Util.Type;
import Zql.ZQuery;

public class TimingTests {
    private static DatabaseConnector db;

    /** Test database user name */
    public static final String USER = "user";
    /** Test database password */
    public static final String PASSWORD = "password";
    /** Test database name */
    public static final String DB = "testdb";

    public static final String CREATE_TABLE = "create table MOCK_DATA (name text, id INT);";

    public static final String DROP_TABLE = "DROP TABLE IF EXISTS MOCK_DATA;";
    
    private SecureRandom random = new SecureRandom();

    private void testPerformance(int rows, int selected) {
        Table original = setupTD();
        Table modified = setupTD();
        
        
        //setup db
        db = DatabaseConnector.getInstance();
        db.connect(DB, USER, PASSWORD);
        db.executeUpdate(DROP_TABLE);
        db.executeUpdate(CREATE_TABLE);
        
        insertTuples(original, modified, selected, rows);
    }
    
    public void test() {
//        testPerformance(10, 1);
//        testPerformance(100, 1);
//        testPerformance(1000, 1);
//        testPerformance(10000, 1);
//        testPerformance(100000, 1);
        testPerformance(10, 5);
        testPerformance(100, 5);
        testPerformance(1000, 5);
        testPerformance(10000, 5);
        testPerformance(100000, 5);
//        testPerformance(100, 100);
//        testPerformance(1000, 100);
//        testPerformance(10000, 100);
//        testPerformance(100000, 100);
    }

    private void insertTuples(Table original, Table modified, int selected, int rows) {
        ZQuery originalQuery = Main.getQuery("SELECT * from MOCK_DATA;");
//        System.out.println(new BigInteger(100, random).toString(Character.MAX_RADIX));
        HashSet <Integer> selection = new HashSet<>();
        while (selection.size() < selected) {
            selection.add(random.nextInt(rows));
        }
//        System.out.println(selection);
        for (int id = 0; id < rows; id++) {
            //insert a number of rows
            String name = new BigInteger(100, random).toString(Character.MAX_RADIX);
            db.executeUpdate("INSERT INTO MOCK_DATA (name, id) values ('" + name +"', " + id + ");"); 
            Tuple tup = new Tuple(2);
            tup.setField(0, new StringField(name));
            tup.setField(1, new IntField(id));
            original.add(tup);
            
            if (selection.contains(id)) {
                tup = new Tuple(2);
                tup.setField(0, new StringField(name));
                tup.setField(1, new IntField(id));
                modified.add(tup);
            }
        }
        assert(original.size() == rows);
        assert(modified.size() == selected);
//        System.out.println(original);
//        System.out.println(modified);
        long start = System.nanoTime();
        String res = QueryGenerator.generate(originalQuery, original, modified);
        long end = System.nanoTime();
        System.out.println("rows : " + rows + " selected : " + selected);
        System.out.println(res);
        System.out.println(end - start);
    }

    private Table setupTD() {
        List<Type> typeList = new ArrayList<Type>();
        typeList.add(Type.TEXT);
        typeList.add(Type.INT);
        List<String> nameList = new ArrayList<String>();
        nameList.add("name");
        nameList.add("id");
        List<String> aliasList = new ArrayList<String>();
        aliasList.add(null);
        aliasList.add(null);
        List<String> tableList = new ArrayList<String>();
        tableList.add("MOCK_DATA");
        tableList.add("MOCK_DATA");
        return new Table(new TableDescriptor(typeList, nameList, aliasList,
                tableList));
    }
}
