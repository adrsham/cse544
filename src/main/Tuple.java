package main;

import java.util.Arrays;
import java.util.Iterator;

/**
 * Tuple maintains information about the contents of a tuple
 */
public class Tuple {

    private Field[] tuple;
    final int length;

    /**
     * Create a new tuple with the specified schema (type).
     * 
     * @param numField
     *            Number of fields in this tuple
     */
    public Tuple(int numFields) {
        this.tuple = new Field[numFields];
        this.length = numFields;
    }

    /**
     * Change the value of the ith field of this tuple.
     * 
     * @param i
     *            index of the field to change. It must be a valid index.
     * @param f
     *            new value for the field.
     */
    public void setField(int i, Field f) {
        if (i >= 0 && i < tuple.length) {
            tuple[i] = f;
        }
    }

    /**
     * @return the value of the ith field, or null if it has not been set.
     * 
     * @param i
     *            field index to return. Must be a valid index.
     */
    public Field getField(int i) {
        if (i >= 0 && i < tuple.length) {
            return tuple[i];
        }
        return null;
    }

    /**
     * Returns the contents of this Tuple as a string. Note that to pass the
     * system tests, the format needs to be as follows:
     * 
     * column1\tcolumn2\tcolumn3\t...\tcolumnN\n
     * 
     * where \t is any whitespace, except newline, and \n is a newline
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < tuple.length - 1; i++) {
            buf.append(tuple[i] + "\t");
        }
        buf.append(tuple[tuple.length - 1] + "\n");
        return buf.toString();
    }

    /**
     * @return An iterator which iterates over all the fields of this tuple
     * */
    public Iterator<Field> fields() {
        return Arrays.asList(tuple).iterator();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + length;
        result = prime * result + Arrays.hashCode(tuple);
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        else if (obj == null)
            return false;
        else if (!(obj instanceof Tuple))
            return false;

        Tuple other = (Tuple) obj;
        if (length != other.length)
            return false;
        else if (!Arrays.equals(tuple, other.tuple))
            return false;
        return true;
    }
}
