package main;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Scanner;
import java.util.Vector;

import Zql.ZConstant;
import Zql.ZExp;
import Zql.ZExpression;
import Zql.ZFromItem;
import Zql.ZQuery;
import Zql.ZSelectItem;
import Zql.ZStatement;
import Zql.ZqlParser;


public class LogicalPlanMain {
	public static final String INSTRUCTIONS = 
			"Usage: java Main <username> <password> <database name>";
	public static final String CONN_FAIL = "Error connecting to db";

	public static final String[] SQL_COMMANDS = { "select", "from", "where"/*,
        "group by", "max(", "min(", "avg(", "count" */};
	
	private static DatabaseConnector con;

	public static void main(String[] argv) throws IOException {
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
/*
		// Add really stupid tab completion for simple SQL
		ArgumentCompletor completor = new ArgumentCompletor(
				new SimpleCompletor(SQL_COMMANDS));
		completor.setStrict(false); // match at any position
		reader.addCompletor(completor);*/

		StringBuilder buffer = new StringBuilder();
		boolean quit = false;
		System.out.print("UpdateDB> ");
		String line = reader.nextLine();
		while (!quit && line != null) {
			// Split statements at ';': handles multiple statements on one
			// line, or one statement spread across many lines
			while (line.indexOf(';') >= 0) {
				int split = line.indexOf(';');
				buffer.append(line.substring(0, split + 1));
				String cmd = buffer.toString().trim();
				cmd = cmd.substring(0, cmd.length() - 1).trim() + ";";
				byte[] statementBytes = cmd.getBytes("UTF-8");
				if (cmd.equalsIgnoreCase("quit;") || cmd.equalsIgnoreCase("exit;")) {
					quit = true;
					break;
				}

				processStatement(new ByteArrayInputStream(statementBytes));
				System.out.println();
				System.out.println(con.runSQL(cmd));
				
				// Grab the remainder of the line
				line = line.substring(split + 1);
				buffer = new StringBuilder();
			}
			if (line.length() > 0) {
				buffer.append(line);
				buffer.append("\n");
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

	@SuppressWarnings("unchecked")
    public static void processStatement(InputStream is) {
        try {
            ZqlParser p = new ZqlParser(is);
            ZStatement s = p.readStatement();
            if (!(s instanceof ZQuery)) {
            	System.err.println("Can't parse " + s + "\n -- parser only handles SQL select statements");
            	return;
            }

            ZQuery q = (ZQuery) s;
            
            // SELECT
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