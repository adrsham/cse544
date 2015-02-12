package main;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;
import java.util.Vector;

import Zql.ParseException;
import Zql.ZConstant;
import Zql.ZExp;
import Zql.ZExpression;
import Zql.ZFromItem;
import Zql.ZQuery;
import Zql.ZSelectItem;
import Zql.ZStatement;
import Zql.ZqlParser;


public class Main {
	public static final String INSTRUCTIONS = 
			"Usage: java Main <username> <password> <database name>";
	public static final String CONN_FAIL = "Error connecting to db";

	public static final String INTRO =
			"UpdateDB Console ver 1.\nType \"h\" or \"help\" for help.\n";

	private static DatabaseConnector con;

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
		ZQuery query = getQuery(statement);
		processStatement(query);
		System.out.println();

		String results = con.runSQL(statement);
		if (!fileLocation.equals("")) {
			Util.writeToFile(fileLocation, results);
		}
		System.out.println(results);
	}

	public static void printUpdateQuery(Scanner reader, String line) {
		// parse the line. make sure it is the right format
		System.out.print("sql statement: ");
		String statement = Util.correctStatementFormat(reader.nextLine());
		System.out.print("expected result filepath: ");
		String file = reader.nextLine();
		System.out.print("filepath (leave blank to just print to stdout): ");
		String fileLocation = reader.nextLine();
		
		String statementResults = con.runSQL(statement);
		Table original = Util.parseStringToTable(statementResults);
		String fileResults = Util.readFromFile(file);
		Table modified = Util.parseStringToTable(fileResults);

		String resultQuery = QueryGenerator.generate(getQuery(statement), original, modified);
		if (!fileLocation.equals("")) {
			Util.writeToFile(fileLocation, resultQuery);
		}
		System.out.println(resultQuery);
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

	@SuppressWarnings("unchecked")
	public static void processStatement(ZQuery q) {
		try {
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
			for (int i = 0; i < wx.nbOperands(); i++) {
				if (!(wx.getOperand(i) instanceof ZExpression)) {
					System.err.println("Nested queries are currently unsupported.");
					return null;
				}
				ZExpression newWx = (ZExpression) wx.getOperand(i);
				exp += processExpression(newWx);
			}
			return exp;
		} else if (wx.getOperator().equals("OR")) {
			System.err.println("OR expressions currently unsupported.");
			return null;
		} else {
			// this is a binary expression comparing two constants
			Vector<ZExp> ops = wx.getOperands();
			if (ops.size() != 2) {
				System.err.println("Only simple binary expresssions of the form A op B are currently supported.");
				return null;
			}

			boolean isJoin = false;
			boolean op1const = ops.elementAt(0) instanceof ZConstant; // otherwise
			// is a
			// Query
			boolean op2const = ops.elementAt(1) instanceof ZConstant; // otherwise
			// is a
			// Query
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
				System.err.println("Only simple binary expresssions of the form A op B are currently supported, where A or B are fields, or constants.");
				return null;
			} else {
				isJoin = false;
			}

			if (isJoin) { // join node
				System.err.println("Joins are currently unsupported.");
				return null;
				/*
                String tab1field = "", tab2field = "";

                if (!op1const) { // left op is a nested query
                    // generate a virtual table for the left op
                    // this isn't a valid ZQL query
                } else {
                    tab1field = ((ZConstant) ops.elementAt(0)).getValue();

                }

                if (!op2const) { // right op is a nested query
                    try {
                        LogicalPlan sublp = parseQueryLogicalPlan(tid,
                                (ZQuery) ops.elementAt(1));
                        DbIterator pp = sublp.physicalPlan(tid,
                                TableStats.getStatsMap(), explain);
                        lp.addJoin(tab1field, pp, op);
                    } catch (IOException e) {
                        throw new simpledb.ParsingException("Invalid subquery "
                                + ops.elementAt(1));
                    } catch (Zql.ParseException e) {
                        throw new simpledb.ParsingException("Invalid subquery "
                                + ops.elementAt(1));
                    }
                } else {
                    tab2field = ((ZConstant) ops.elementAt(1)).getValue();
                    lp.addJoin(tab1field, tab2field, op);
                }
				 */
			} else { // select node
				String column;
				String compValue;
				ZConstant op1 = (ZConstant) ops.elementAt(0);
				ZConstant op2 = (ZConstant) ops.elementAt(1);
				if (op1.getType() == ZConstant.COLUMNNAME) {
					column = op1.getValue();
					compValue = new String(op2.getValue());
					return column + "(column) " + wx.getOperator() + compValue + "(comp value)";
				} else {
					column = op2.getValue();
					compValue = new String(op1.getValue());

					return compValue + "(comp value)" + wx.getOperator() + column + "(column) ";
				}
			}
		}
	}
}