package main;

import java.util.*;
import main.Util.*;

/**
 * TableDescriptor describes the schema of a resulting Table object.
 */
public class TableDescriptor {

    public static enum TableType {
        DB_TABLE, QUERY_RESULTS;
    }

    /**
     * A help class to facilitate organizing the information of each field
     * */
    public static class TDItem {

        /**
         * The type of the field
         * */
        public Type fieldType;

        /**
         * The actual name of the field
         * */
        public String fieldName;

        /**
         * the max length of this particular field in the table
         */
        public int fieldMaxLength;

        public TDItem(Type type, String name, int maxLength) {
            this.fieldType = type;
            this.fieldName = name;
            this.fieldMaxLength = maxLength;
        }

        public String toString() {
            StringBuffer buf = new StringBuffer();
            buf.append(fieldName);
            buf.append("(" + fieldType + ")");
            return buf.toString();
        }
    }

    public static class TQDItem extends TDItem {

        /**
         * The alias name of the field
         * */
        public String fieldAlias;

        /**
         * The name of the table this field was from
         * */
        public String fieldTable;

        public TQDItem(Type type, String name, String aliasName, String tableName) {
            super(type, name, 0);
            this.fieldAlias = aliasName;
            this.fieldTable = tableName;
        }

        public String toString() {
            StringBuffer buf = new StringBuffer();
            if (fieldTable != null) {
                buf.append(fieldTable + ".");
            }
            buf.append(fieldName);
            if (fieldAlias != null) {
                buf.append(" as " + fieldAlias);
            }
            buf.append("(" + fieldType + ")");
            return buf.toString();
        }
    }

    private List<TDItem> descriptor;
    public final TableType type;

    /**
     * @return
     *        An iterator which iterates over all the field TDItems
     *        that are included in this TupleDesc
     * */
    public Iterator<TDItem> iterator() {
        if (descriptor != null) {
            return descriptor.iterator();
        }
        return null;
    }

    /**
     * Create a new TupleDesc with typeList.size() fields with fields of the
     * specified types, with associated named fields.
     * 
     * @param typeList
     *            list specifying the number of and types of fields in this
     *            TupleDesc. It must contain at least one entry.
     * @param nameList
     *            list specifying the names of the fields. Names can not be null
     *            
     * @param maxLengthList
     *            list specifying the max lengths of the fields.
     */
    public TableDescriptor(List<Type> typeList, List<String> nameList, List<Integer> maxLengthList) {
        if (typeList.size() != nameList.size() || typeList.size() != maxLengthList.size()) {
            System.err.printf("Type size %d, name size %d\n", typeList.size(), nameList.size());
            System.err.printf("LengthList size %d, type size %d\n", maxLengthList.size(), typeList.size());
            System.err.println("descriptor must have same amount of types as fileds");
            throw new RuntimeException();
        }
        type = TableType.DB_TABLE;
        descriptor = new ArrayList<TDItem>();
        for (int i = 0; i < typeList.size(); i++) {
            descriptor.add(new TDItem(typeList.get(i), nameList.get(i), maxLengthList.get(i)));
        }
    }

    /**
     * Create a new TupleDesc with typeList.size() fields with fields of the
     * specified types, with associated named fields. By not passing in a max length, this td
     * is assumed to be a query result
     * 
     * @param typeList
     *            list specifying the number of and types of fields in this
     *            TupleDesc. It must contain at least one entry.
     * @param nameList
     *            list specifying the names of the fields. Names can not be null
     */
    public TableDescriptor(List<Type> typeList, List<String> nameList) {
        // type query result
        if (typeList.size() != nameList.size()) {
            System.err.printf("Type size %d, name size %d\n", typeList.size(), nameList.size());
            System.err.println("descriptor must have same amount of types as fileds");
            throw new RuntimeException();
        }
        type = TableType.QUERY_RESULTS;
        descriptor = new ArrayList<TDItem>();
        for (int i = 0; i < typeList.size(); i++) {
            descriptor.add(new TQDItem(typeList.get(i), null, nameList.get(i), null));
        }
    }

    /**
     * Create a new TupleDesc with typeList.size() fields with fields of the
     * specified types, with associated named fields. By not passing in a max length, this td
     * is assumed to be a query result
     * 
     * @param typeList
     *            list specifying the number of and types of fields in this
     *            TupleDesc. It must contain at least one entry.
     * @param nameList
     *            list specifying the names of the fields.
     * @param aliasList
     *            list specifying the alias of the fields.
     * @param tableList
     *            list specifying the table of the fields.
     */
    public TableDescriptor(List<Type> typeList, List<String> nameList, List<String> aliasList, List<String> tableList) {
        // type query result
        if (typeList.size() != nameList.size() || typeList.size() != aliasList.size() || typeList.size() != tableList.size()) {
            System.err.println("descriptor must have same amount of types as fileds");
            System.err.println(typeList.size() + " " + nameList.size() + " " + aliasList.size() + " " + tableList.size());
            throw new RuntimeException();
        }
        descriptor = new ArrayList<TDItem>();
        type = TableType.QUERY_RESULTS;
        for (int i = 0; i < typeList.size(); i++) {
            descriptor.add(new TQDItem(typeList.get(i), nameList.get(i), aliasList.get(i), tableList.get(i)));
        }
    }

    /**
     * @return the number of fields in this TupleDesc
     */
    public int numFields() {
        return descriptor.size();
    }

    /**
     * Gets the (possibly null) field name of the ith field of this TupleDesc.
     * 
     * @param i
     *            index of the field name to return. It must be a valid index.
     * @return the name of the ith field
     * @throws NoSuchElementException
     *             if i is not a valid field reference.
     */
    public String getFieldName(int i) throws NoSuchElementException {
        if (i < 0 || i > descriptor.size() - 1) {
            throw new NoSuchElementException();
        }
        return descriptor.get(i).fieldName;
    }

    /**
     * Gets the (possibly null) field alias of the ith field of this TupleDesc.
     * 
     * @param i
     *            index of the alias name to return. It must be a valid index.
     * @return the name of the ith field
     * @throws NoSuchElementException
     *             if i is not a valid field reference.
     */
    public String getAliasName(int i) throws NoSuchElementException {
        if (type == TableType.DB_TABLE) {
            return null;
        }
        if (i < 0 || i > descriptor.size() - 1) {
            throw new NoSuchElementException();
        }
        return ((TQDItem)descriptor.get(i)).fieldAlias;
    }

    /**
     * Gets the (possibly null) field table of the ith field of this TupleDesc.
     * 
     * @param i
     *            index of the table name to return. It must be a valid index.
     * @return the name of the ith field
     * @throws NoSuchElementException
     *             if i is not a valid field reference.
     */
    public String getTableName(int i) throws NoSuchElementException {
        if (type == TableType.DB_TABLE) {
            return null;
        }
        if (i < 0 || i > descriptor.size() - 1) {
            throw new NoSuchElementException();
        }
        return ((TQDItem)descriptor.get(i)).fieldTable;
    }

    /**
     * Gets the type of the ith field of this TupleDesc.
     * 
     * @param i
     *            The index of the field to get the type of. It must be a valid
     *            index.
     * @return the type of the ith field
     * @throws NoSuchElementException
     *             if i is not a valid field reference.
     */
    public Type getFieldType(int i) throws NoSuchElementException {
        if (i < 0 || i > descriptor.size() - 1) {
            throw new NoSuchElementException();
        }
        return descriptor.get(i).fieldType;
    }

    public TDItem getFieldInfo(int i) throws NoSuchElementException {
        if (i < 0 || i > descriptor.size() - 1) {
            throw new NoSuchElementException();
        }
        return descriptor.get(i);
    }

    /**
     * Find the index of the field with a given name.
     * 
     * @param name
     *            name of the field.
     * @return the index of the field that is first to have the given name.
     * @throws NoSuchElementException
     *             if no field with a matching name is found.
     */
    public int fieldNameToIndex(String name) throws NoSuchElementException {
        if (name == null) {
            System.err.println("name of field to find can not be null");
            throw new NoSuchElementException();
        }
        for (int i = 0; i < descriptor.size(); i++) {
            if (name.equals(descriptor.get(i).fieldName)) {
                return i;
            }
        }
        throw new NoSuchElementException();
    }

    /**
     * Compares the specified object with this TupleDesc for equality. Two
     * TupleDescs are considered equal if they are the same size and if the n-th
     * type in this TupleDesc is equal to the n-th type in td.
     * 
     * @param o
     *            the Object to be compared for equality with this TupleDesc.
     * @return true if the object is equal to this TupleDesc.
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TableDescriptor)) {
            return false;
        }
        TableDescriptor other = (TableDescriptor) o;
        for (int i = 0; i < descriptor.size(); i++) {
            Type type1 = descriptor.get(i).fieldType;
            Type type2 = other.descriptor.get(i).fieldType;
            if (!type1.equals(type2)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        for (int i = 0; i < descriptor.size(); i++) {
            hash += descriptor.get(i).fieldType.hashCode();
        }
        return hash;
    }

    /**
     * Returns a String describing this descriptor. It should be of the form
     * "fieldType[0](fieldName[0]), ..., fieldType[M](fieldName[M])", although
     * the exact format does not matter.
     * 
     * @return String describing this descriptor.
     */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(descriptor.get(0));
        for (int i = 1; i < descriptor.size(); i++) {
            TDItem item = descriptor.get(i);
            buf.append( ", " + item);
        }
        return buf.toString();
    }

    /**
     * Get the max length of the field, using fieldNameToIndex
     * @param name name of the field (original name)
     * @return max length of field
     */
    public int getFieldLength(String name) {
        return descriptor.get(fieldNameToIndex(name)).fieldMaxLength;
    }
}
