package main;


public class Util {

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
		return null;
	}
	
	/**
	 * Takes the given string and writes it to a file
	 * 
	 * @param fileLocation location of file
	 * @param content content to be written to file
	 */
	public static void writeToFile (String fileLocation, String content) {
		
	}
	
	/**
	 * Returns the string read from a file
	 * 
	 * @param fileLocation location of file
	 * @return string contents of file
	 */
	public static String readFromFile (String fileLocation) {
		return null;
	}
}
