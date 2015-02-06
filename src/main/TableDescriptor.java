package main;

import java.util.*;
import main.Util.*;

/**
 * TableDescriptor describes the schema of a resulting Table object.
 */
public class TableDescriptor {

	/**
	 * A help class to facilitate organizing the information of each field
	 * */
	public static class TDItem {

		/**
		 * The type of the field
		 * */
		public final Type fieldType;

		/**
		 * The name of the field
		 * */
		public final String fieldName;
		
		/**
		 * The name of the field
		 * */
		public final String fieldAlias;
		
		/**
		 * The name of the field
		 * */
		public final String fieldTable;

		public TDItem(Type type, String n, String a, String t) {
			this.fieldName = n;
			this.fieldAlias = a;
			this.fieldTable = t;
			this.fieldType = type;
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
	 * Create a new TupleDesc with typeAr.length fields with fields of the
	 * specified types, with associated named fields.
	 * 
	 * @param typeAr
	 *            array specifying the number of and types of fields in this
	 *            TupleDesc. It must contain at least one entry.
	 * @param fieldAr
	 *            array specifying the names of the fields. Note that names may
	 *            be null.
	 */
	public TableDescriptor(Type[] typeAr, String[] nameAr, String[] aliasAr, String[] tableAr) {
		if (typeAr.length != nameAr.length || typeAr.length != aliasAr.length || typeAr.length != tableAr.length) {
			System.err.println("descriptor must have same amount of types as fileds");
			throw new RuntimeException();
		}
		descriptor = new ArrayList<TDItem>();
		for (int i = 0; i < typeAr.length; i++) {
			descriptor.add(new TDItem(typeAr[i], nameAr[i], aliasAr[i], tableAr[i]));
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
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append(descriptor.get(0));
		for (int i = 1; i < descriptor.size(); i++) {
			TDItem item = descriptor.get(i);
			buf.append( ", " + item);
		}
		return buf.toString();
	}
}
