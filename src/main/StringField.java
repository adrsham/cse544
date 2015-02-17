package main;

import main.Util.*;

/**
 * Instance of Field that stores a single String of a fixed length.
 */
public class StringField implements Field {

    private final String value;

    public String getValue() {
        return value;
    }

    /**
     * Constructor.
     * 
     * @param s
     *            The value of this field.
     * @param maxSize
     *            The maximum size of this string
     */
    public StringField(String s) {
        value = s;
    }

    public String toString() {
        return value;
    }

    public int hashCode() {
        return value.hashCode();
    }

    public boolean equals(Object field) {
        if (!(field instanceof StringField))
            return false;
        return ((StringField) field).value.equals(value);
    }

    /**
     * Compare the specified field to the value of this Field. Return semantics
     * are as specified by Field.compare
     * 
     * @throws IllegalCastException
     *             if val is not a StringField
     * @see Field#compare
     */
    public boolean compare(Op op, Field val) {

        StringField iVal = (StringField) val;
        int cmpVal = value.compareTo(iVal.value);

        switch (op) {
        case EQUALS:
            return cmpVal == 0;
        case NOT_EQUALS:
            return cmpVal != 0;
        case GREATER_THAN:
            return cmpVal > 0;

        case GREATER_THAN_OR_EQ:
            return cmpVal >= 0;
        case LESS_THAN:
            return cmpVal < 0;
        case LESS_THAN_OR_EQ:
            return cmpVal <= 0;
        case LIKE:
            return value.indexOf(iVal.value) >= 0;
        }
        return false;
    }

    /**
     * @return the Type for this Field
     */
    public Type getType() {

        return Type.TEXT;
    }
} 
