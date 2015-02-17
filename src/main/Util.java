package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import main.TableDescriptor.TDItem;
import main.TableDescriptor.TQDItem;
import Zql.ZFromItem;
import Zql.ZQuery;
import Zql.ZSelectItem;


public class Util {

	public static final Logger LOG = Logger.getLogger(Util.class.getName());

	public static Map<String, TableDescriptor> dbInfo;

	public static final String GET_TABLE_SQL = "SELECT table_name FROM information_schema.tables WHERE table_schema='public';";
	public static final String GET_COLUMN_INFO_SQL = "SELECT column_name, data_type FROM information_schema.columns WHERE table_name = '%s';";
    public static final String GET_COLUMN_DATA_SQL = "SELECT %s FROM %s;";
	public static final String GET_MAX_LENGTH_TEXT_SQL = "SELECT MAX(LENGTH(%s)) FROM %s;";
	public static final String GET_MAX_INT_SQL = "SELECT MAX(%s) FROM %s;";
	public static final String GET_MIN_INT_SQL = "SELECT MIN(%s) FROM %s;";

	public enum Op {
		EQUALS, GREATER_THAN, LESS_THAN, LESS_THAN_OR_EQ, GREATER_THAN_OR_EQ, LIKE, NOT_EQUALS;

		/**
		 * Interface to access operations by integer value for command-line
		 * convenience.
		 * 
		 * @param i a valid integer Op index
		 */
		public static Op getOp(int i) {
			return values()[i];
		}

		public String toString() {
			if (this == EQUALS)
				return "=";
			if (this == GREATER_THAN)
				return ">";
			if (this == LESS_THAN)
				return "<";
			if (this == LESS_THAN_OR_EQ)
				return "<=";
			if (this == GREATER_THAN_OR_EQ)
				return ">=";
			if (this == LIKE)
				return "LIKE";
			if (this == NOT_EQUALS)
				return "<>";
			throw new IllegalStateException("impossible to reach here");
		}

	}

	public enum Type {
		NONE,
		INT,
		TEXT
	}

	/**
	 * all internal so the queries and such should return back the correct sizes and lengths.
	 * @return
	 */
	public static Map<String, TableDescriptor> getDatabaseInfo() {

		dbInfo = new HashMap<String, TableDescriptor>();
		DatabaseConnector con = DatabaseConnector.getInstance();
		String[] tables = con.runSQL(GET_TABLE_SQL).split(System.lineSeparator());
		for (int i = 2; i < tables.length; i++) {
			String tableName = tables[i].trim();
			// loops through all of the tables
			List<Type> type = new ArrayList<Type>();
			List<String> name = new ArrayList<String>();
			List<Integer> maxLength = new ArrayList<Integer>();
			String[] column = con.runSQL(String.format(GET_COLUMN_INFO_SQL, tables[i].trim())).split(System.lineSeparator());
			for(int j = 2; j < column.length; j++) {
				String[] nameAndType = column[j].trim().split("\t");
				String colName = nameAndType[0].trim(); 
				String colType = nameAndType[1].trim();
				name.add(colName);
				if (colType.equals("text")) {
					type.add(Type.TEXT);
					String[] res = con.runSQL(String.format(GET_MAX_LENGTH_TEXT_SQL, colName, tableName)).split(System.lineSeparator());
					maxLength.add(Integer.parseInt(res[2].trim()));
					//
				} else {
					type.add(Type.INT);
					//getting the max and min value and comparing their lengths to get the max length of an int.
					String[] resMax = con.runSQL(String.format(GET_MAX_INT_SQL, colName, tableName)).split(System.lineSeparator());
					String[] resMin = con.runSQL(String.format(GET_MIN_INT_SQL, colName, tableName)).split(System.lineSeparator());
					maxLength.add(Math.max(resMax[2].trim().length(), resMin[2].trim().length()));
				}
			}
			dbInfo.put(tableName, new TableDescriptor(type, name, maxLength));
		}

		return dbInfo;
	}

	/**
	 * Checks sql format to make sure no white spaces are at the end and also adds semicolon to end
	 * if it is not there.
	 * @param statement
	 * @return corrected statement with no extra spaces and a semicolon at the end.
	 */
	public static String correctStatementFormat(String statement) {
		statement = statement.trim();
		if (!statement.endsWith(";"))
			statement += ";";
		return statement;
	}

	/**
	 * parses string and query into a table object of type query results
	 */
	@SuppressWarnings("unchecked")
	public static Table parseStringAndQueryToTable(ZQuery query, String stringInput) {
		List<String> name = new ArrayList<String>();
		List<String> alias = new ArrayList<String>();
		List<String> table = new ArrayList<String>();
		List<Type> type = new ArrayList<Type>();
		
		String[] line = stringInput.split(System.lineSeparator())[0].split("\t");
		int curCol = 0;		
		Vector<ZSelectItem> selectList = query.getSelect();
		for (int i = 0; i < selectList.size(); i++) {
			ZSelectItem si = selectList.elementAt(i);
			Vector<ZFromItem> from = query.getFrom();
			if (si.getColumn().equals("*")) {
				if (si.getTable() == null) {
					// star everything. must add all col from from list.
					for (int j = 0; j < from.size(); j++) {
						String curTable = from.elementAt(j).getTable();
						Iterator<TDItem> iter = dbInfo.get(curTable).iterator();
						while (iter.hasNext()) {
							TDItem next = iter.next();
							name.add(next.fieldName);
							alias.add(null);
							table.add(curTable);
							type.add(next.fieldType);
							curCol++;
						}
					}
				} else {
					for (int j = 0; j < from.size(); j++) {
						if (si.getTable().equals(from.elementAt(j).getAlias()) || si.getTable().equals(from.elementAt(j).getTable())) {
							String curTable = from.elementAt(j).getTable();
							Iterator<TDItem> iter = dbInfo.get(curTable).iterator();
							while (iter.hasNext()) {
								TDItem next = iter.next();
								name.add(next.fieldName);
								alias.add(null);
								table.add(curTable);
								type.add(next.fieldType);
								curCol++;
							}
							break;
						}
					}
				}
			} else {
				String[] column = line[curCol].split("\\s+");
				name.add(si.getColumn());
				alias.add(si.getAlias());
				if (si.getTable() == null) {
				    // find the table that it belongs to
				    for (int j = 0; j < from.size(); j++) {
				        String currentTable = from.elementAt(j).getTable();
				        TableDescriptor td = dbInfo.get(currentTable);
				        for (int k = 0; k < td.numFields(); k++) {
				            if (si.getColumn().equals(td.getFieldName(k))) {
				                table.add(currentTable);
				                break;
				            }
				        }
				        if (table.size() == name.size()) {
				            // already added the table name
				            break;
				        }
				    }
				} else {
				    for (int j = 0; j < from.size(); j++) {
                        ZFromItem item = from.elementAt(j);
                        if (si.getTable().equals(item.getTable()) || si.getTable().equals(item.getAlias())) {
                            table.add(item.getTable());
                            break;
                        }
				    }
				}
				if (column[1].trim().toLowerCase().equals("("+DatabaseConnector.STRING_TYPE_NAME+")")) {
					type.add(Type.TEXT);
				} else if (column[1].trim().toLowerCase().startsWith("("+DatabaseConnector.INT_TYPE_NAME)) {
					type.add(Type.INT);
				} else {
					throw new RuntimeException("Unsupported type!");
				}
			}
		}
		TableDescriptor td = new TableDescriptor(type, name, alias, table);
		return parseTDAndStringToTable(td, stringInput);
	}

	/**
	 * Parse string and turn it into table class
	 * 
	 * @param stringInput string to be parsed
	 * @return Table object
	 */
	public static Table parseStringToTable (String stringInput) {
		Objects.requireNonNull(stringInput);
		//split on each line
		String[] lines = stringInput.split(System.lineSeparator());

		List<Type> typeAr = new ArrayList<>();
		List<String> nameAr = new ArrayList<>();

		// assume first line is the labels
		String[] line = lines[0].split("\t");
		for (int j = 0; j < line.length; j++) {
			String[] column = line[j].split("\\s+");
			nameAr.add(column[0]);
			
			if (column[1].trim().toLowerCase().equals("("+DatabaseConnector.STRING_TYPE_NAME+")")) {
				typeAr.add(Type.TEXT);
			} else if (column[1].trim().toLowerCase().startsWith("("+DatabaseConnector.INT_TYPE_NAME)) {
				typeAr.add(Type.INT);
			} else {
				throw new RuntimeException("Unsupported type!");
			}
		}

		//create table descriptor
		TableDescriptor td = new TableDescriptor(typeAr, nameAr);
		return parseTDAndStringToTable(td, stringInput);
	}
	
	private static Table parseTDAndStringToTable(TableDescriptor td, String stringInput) {
		Objects.requireNonNull(stringInput);
		//split on each line
		String[] lines = stringInput.split(System.lineSeparator());
		Table table = new Table(td);

		for (int i = 2; i < lines.length; i++) {
			//third onwards are tuples
			Tuple t = new Tuple(td.numFields());
			//String[] line = lines[i].split("\\s+");
			String[] line = lines[i].split("\t");
			for (int j = 0; j < line.length; j++) {
				if (td.getFieldType(j) == Type.INT) {
					t.setField(j, new IntField(Integer.parseInt(line[j].trim())));
				} else if (td.getFieldType(j) == Type.TEXT) {
					t.setField(j, new StringField(line[j].trim()));
				} else {
					throw new RuntimeException("Unsupported type!");
				}
			}
			//insert tuple
			table.add(t);
		}

		return table;
	}

	/**
	 * Takes the given string and writes it to a file
	 * 
	 * @param fileLocation location of file
	 * @param content content to be written to file
	 * @throws NullPointerException if any parameters are null
	 */
	public static void writeToFile (String fileLocation, String content) {
		Objects.requireNonNull(fileLocation);
		Objects.requireNonNull(content);
		Charset charset = Charset.forName("UTF-8");
		Path path = Paths.get(fileLocation);
		try (BufferedWriter writer = Files.newBufferedWriter(path, charset)) {
			writer.write(content);
		} catch (IOException e) {
			LOG.log(Level.SEVERE, "Failed to write to file", e);
		}
	}

	/**
	 * Returns the string read from a file
	 * 
	 * @param fileLocation location of file
	 * @return string contents of file
	 */
	public static String readFromFile (String fileLocation) {
		Objects.requireNonNull(fileLocation);
		Charset charset = Charset.forName("UTF-8");
		Path path = Paths.get(fileLocation);
		StringBuilder buf = new StringBuilder();
		try (BufferedReader reader = Files.newBufferedReader(path, charset)) {
			String line;
			while ((line = reader.readLine()) != null) {
				buf.append(line);
				buf.append(System.lineSeparator());
			}
		} catch (IOException e) {
			LOG.log(Level.SEVERE, "Failed to read from file", e);
		}

		return buf.toString();
	}


    /**
     * Modifies the table so that the table descriptor is correct with name, alias, and table of each column set correctly.
     * @param table
     */
    public static void findRealTableDescriptor(Table table) {
        DatabaseConnector con = DatabaseConnector.getInstance();
        List<List<Field>> fields = table.getColOfFields();
        TableDescriptor td = table.getTD();
        for (int i = 0; i < td.numFields(); i++) {
            boolean found = false;
            // loop through each num field and find out what the column info actually is.
            for (String tableName : dbInfo.keySet()) {
                TableDescriptor curTD = dbInfo.get(tableName);
                for (int j = 0; j < curTD.numFields(); j++) {
                    String colName = curTD.getFieldName(j);
                    Table curTable = parseStringToTable(con.runSQL(String.format(GET_COLUMN_DATA_SQL, colName, tableName)));
                    List<Field> curFields = curTable.getColOfFields().get(0);
                    if (curFields.containsAll(fields.get(i)) && td.getFieldType(i) == curTD.getFieldType(j)) {
                        // the fields of the table passed in was contained inside the full list.
                        TQDItem item = (TQDItem) td.getFieldInfo(i);
                        item.fieldName = colName;
                        if (item.fieldName.equals(item.fieldAlias)) {
                            item.fieldAlias = null;
                        }
                        item.fieldTable = tableName;
                        found = true;
                        break;
                    }
                }
                if (found) {
                    break;
                }
            }
        }
        System.out.println(fields);
    }
}