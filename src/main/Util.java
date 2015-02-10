package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Util {
	
	public static final Logger LOG = Logger.getLogger(Util.class.getName());

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
	 * Parse string and turn it into table class
	 * 
	 * @param stringInput string to be parsed
	 * @return Table object
	 */
	public static Table parseStringToTable (String stringInput) {
		Objects.requireNonNull(stringInput);
		//split on each line
		String[] lines = stringInput.split(System.lineSeparator());
		
		ArrayList <Type> typeAr = new ArrayList<>();
		ArrayList <String> nameAr = new ArrayList<>();
		ArrayList <String> aliasAr = new ArrayList<>();
		ArrayList <String> tableAr = new ArrayList<>();
		TableDescriptor td = null;
		Table table = null;
		
		for (int i = 0; i < lines.length; i++) {
			//first line is column title
			if (i == 0) {
				String[] line = lines[0].split("\\s+");
				for (int j = 0; j < line.length; j++) {
					if (j % 2 == 0) {
						//name
						nameAr.add(line[j]);
					} else {
						//type
						if (line[j].trim().equals("("+DatabaseConnector.STRING_TYPE_NAME+")")) {
							typeAr.add(Type.TEXT);
						} else if (line[j].trim().equals("("+DatabaseConnector.INT_TYPE_NAME+")")) {
							typeAr.add(Type.INT);
						} else {
							throw new RuntimeException("Unsupported type!");
						}
					}
				}
				for (int j = 0; j < typeAr.size(); j++) {
					aliasAr.add(null);
					tableAr.add(null);
				}
				//create table descriptor
				Type[] typeArr = new Type[typeAr.size()];
				typeArr = typeAr.toArray(typeArr);
				String[] nameArr = new String[typeAr.size()];
				nameArr = nameAr.toArray(nameArr);
				String[] aliasArr = new String[typeAr.size()];
				aliasArr = aliasAr.toArray(aliasArr);
				String[] tableArr = new String[typeAr.size()];
				tableArr = tableAr.toArray(tableArr);
				td = new TableDescriptor(typeArr, nameArr, aliasArr, tableArr);
				//create table
				table = new Table(td);
				continue;
			}
			//second line is line ("---"), ignore
			if (i == 1) {
				continue;
			}
			//third onwards are tuples
			Tuple t = new Tuple(typeAr.size());
			
			String[] line = lines[i].split("\\s+");
			for (int j = 0; j < line.length; j++) {
				if (typeAr.get(j) == Type.INT) {
					t.setField(j, new IntField(Integer.parseInt(line[j].trim())));
				} else if (typeAr.get(j) == Type.TEXT) {
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
}
