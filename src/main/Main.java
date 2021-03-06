package main;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Scanner;
import java.util.Vector;

import Zql.ParseException;
import Zql.ZConstant;
import Zql.ZExp;
import Zql.ZExpression;
import Zql.ZQuery;
import Zql.ZStatement;
import Zql.ZqlParser;


public class Main {
    public static final String INSTRUCTIONS = 
            "Usage: java Main <username> <password> <database name>";
    public static final String CONN_FAIL = "Error connecting to db";

    public static final String INTRO =
            "UpdateDB Console ver 1.\nType \"h\" or \"help\" for help.\n";

    private static DatabaseConnector con;
    private static  Map<String, TableDescriptor> dbInfo;

    public static void main(String[] argv) {
        if (argv.length != 3) {
            System.err.println(INSTRUCTIONS);
            return;
        }

        initializeDBConnection(argv);
        if (!con.connectionValid()) {
            System.err.println(CONN_FAIL);
            return;
        }
        dbInfo = Util.getDatabaseInfo();

        Scanner reader = new Scanner(System.in);
        System.out.println(INTRO);
        boolean quit = false;
        System.out.print("UpdateDB> ");
        String line = reader.nextLine().toLowerCase();
        while (!quit && line != null) {
            if (line.startsWith("q") || line.startsWith("e")) {
                quit = true;
                break;
            } else if (line.startsWith("h")) {
                printHelp();
            } else if (line.startsWith("s")) {
                // run statement and print out results
                printStatementResults(reader, line);
            } else if (line.startsWith("u")) {
                // run statement, compare, get back new statement
                printUpdateQuery(reader, line);
            }

            System.out.print("UpdateDB> ");
            line = reader.nextLine();
        }
        reader.close();
    }

    private static void initializeDBConnection(String[] argv) {
        con = DatabaseConnector.getInstance();
        con.connect(argv[2], argv[0], argv[1]);
    }

    public static void printHelp() {
        System.out.println("You are using the commandline interface of UpdateDB ver 1");
        System.out.println("Type h|help         : view menu");
        System.out.println("     s              : run query and get results");
        System.out.println("     u              : get updated query");
        System.out.println("     q|e|quit|exit  : quit application");
    }

    public static void printStatementResults(Scanner reader, String line) {
        // parse the line. make sure it is the right format
        System.out.print("sql statement: ");
        String statement = Util.correctStatementFormat(reader.nextLine());
        System.out.print("filepath (leave blank to just print to stdout): ");
        String fileLocation = reader.nextLine();
        long start = System.currentTimeMillis();
        ZQuery query = getQuery(statement);

        String results = con.runSQL(statement, query, dbInfo);
        long time = System.currentTimeMillis() - start;
        if (!fileLocation.equals("")) {
            Util.writeToFile(fileLocation, results);
        }
        System.out.println(results);
        System.out.printf("----------------\n%.4f seconds\n\n",
                ((double) time / 1000.0));
    }

    public static void printUpdateQuery(Scanner reader, String line) {
        // parse the line. make sure it is the right format
        System.out.print("sql statement: ");
        String statement = Util.correctStatementFormat(reader.nextLine());
        System.out.print("expected result filepath: ");
        String file = reader.nextLine();
        System.out.print("filepath (leave blank to just print to stdout): ");
        String fileLocation = reader.nextLine();
        long start = System.currentTimeMillis();
        String statementResults = con.runSQL(statement);
        ZQuery query = getQuery(statement);
        processStatement(query);
        Table original = Util.parseStringAndQueryToTable(query, statementResults);
        String fileResults = Util.readFromFile(file);
        Table modified = Util.parseStringToTable(fileResults);
        String resultQuery = QueryGenerator.generate(getQuery(statement), original, modified);
        long time = System.currentTimeMillis() - start;
        if (!fileLocation.equals("")) {
            Util.writeToFile(fileLocation, resultQuery);
        }
        System.out.println(resultQuery);
        System.out.printf("----------------\n%.4f seconds\n\n",
                ((double) time / 1000.0));
    }

    public static ZQuery getQuery(String statement) {
        try {
            byte[] statementBytes = statement.getBytes("UTF-8");
            ZqlParser p = new ZqlParser(new ByteArrayInputStream(statementBytes));

            ZStatement s = p.readStatement();
            if (!(s instanceof ZQuery)) {
                System.err.println("Can't parse " + s + "\n -- parser only handles SQL select statements");
                return null;
            }
            return (ZQuery) s;
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            System.err.println(e.getMessage());
        }
        return null;
    }

    //@SuppressWarnings("unchecked")
    public static void processStatement(ZQuery q) {
        try {
            /*
            System.out.println("SELECT");
            Vector<ZSelectItem> selectList = q.getSelect();
            for (int i = 0; i < selectList.size(); i++) {
                ZSelectItem si = selectList.elementAt(i);
                if (si.getAggregate() == null
                        && (si.isExpression() && !(si.getExpression() instanceof ZConstant))) {
                    System.err.println("Expressions in SELECT list are not supported.");
                    return;
                } else if (si.getAggregate() != null) {
                    System.err.println("Aggregates in SELECT list currently not supported.");
                    return;
                } else {
                    // in a select example a.b as c, alias = c, column = b, table = a
                    System.out.println("\t" + si.getAlias() + " " + si.getColumn() + " " + si.getTable());
                }
            }

            // FROM
            System.out.println("FROM");
            Vector<ZFromItem> from = q.getFrom();
            for (int i = 0; i < from.size(); i++) {
                // need to handle subqueries maybe?
                ZFromItem fi = from.elementAt(i);
                System.out.println("\t" + fi.getAlias() + " " + fi.getTable());
            }
             */
            // WHERE
            System.out.println("WHERE");
            ZExp w = q.getWhere();
            if (w != null) {
                if (!(w instanceof ZExpression)) {
                    System.err.println("Nested queries are currently unsupported.");
                    return;
                }
                ZExpression wx = (ZExpression) w;
                String exp = processExpression(wx);
                if (exp == null) {
                    return;
                }
                System.out.println("\t" + exp);

            }
        } catch (Exception e) {
            System.out.println("there is an error with processing the statement");
        }
    }

    /**
     * Where section parser that reads expressions. currently only supports constants. no joins.
     * @param wx
     */
    @SuppressWarnings("unchecked")
    public static String processExpression(ZExpression wx) {
        if (wx.getOperator().equals("AND")) {
            String exp = "";
            System.out.println("number of operands:" + wx.nbOperands());
            for (int i = 0; i < wx.nbOperands(); i++) {
                if (!(wx.getOperand(i) instanceof ZExpression)) {
                    System.err.println("Nested queries are currently unsupported.");
                    return null;
                }
                ZExpression newWx = (ZExpression) wx.getOperand(i);
                Vector<ZExp> ops = newWx.getOperands();
                if (ops.size() != 2) {
                    System.err.println("Only simple binary expresssions of the form A op B are currently supported.");
                    return null;
                }

                boolean isJoin = false;
                boolean op1const = ops.elementAt(0) instanceof ZConstant; // otherwise
                // is a Query
                boolean op2const = ops.elementAt(1) instanceof ZConstant; // otherwise
                // is a Query
                if (op1const && op2const) {
                    isJoin = ((ZConstant) ops.elementAt(0)).getType() == ZConstant.COLUMNNAME
                            && ((ZConstant) ops.elementAt(1)).getType() == ZConstant.COLUMNNAME;
                } else if (ops.elementAt(0) instanceof ZQuery
                        || ops.elementAt(1) instanceof ZQuery) {
                    isJoin = true;
                    // currently not supported
                    System.err.println("Subqueries are currently unsupported.");
                    return null;
                } else if (ops.elementAt(0) instanceof ZExpression
                        || ops.elementAt(1) instanceof ZExpression) {
                    System.err.println("Only simple binary expresssions of the form A op B are currently " +
                            "supported, where A or B are fields, or constants.");
                    return null;
                } else {
                    isJoin = false;
                }

                if (!isJoin) { // select node
                    String column;
                    String compValue;
                    ZConstant op1 = (ZConstant) ops.elementAt(0);
                    ZConstant op2 = (ZConstant) ops.elementAt(1);
                    if (op1.getType() == ZConstant.COLUMNNAME) {
                        column = op1.getValue();
                        compValue = new String(op2.getValue());
                        exp += column + "(column) " + newWx.getOperator() + " " + compValue + "(comp value)";
                    } else {
                        column = op2.getValue();
                        compValue = new String(op1.getValue());
                        exp += compValue + "(comp value)" + newWx.getOperator() + " " + column + "(column) ";
                    }
                }
                exp += "\n\t";
            }
            return exp;
        }
        return null;
    }
}