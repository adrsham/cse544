package main;

import java.util.LinkedList;
import java.util.List;


public class Table {
    private TableDescriptor td;
    private List<Tuple> tuples;
    
    public Table(TableDescriptor td) {
    	this.td = td;
    	// linked list if we plan to add/remove a lot. not sure what we will be doing
    	this.tuples = new LinkedList<Tuple>();
    }
    
    public boolean add(Tuple t) {
    	// check to see if this has the same descriptor
    	if (td.numFields() != t.length) {
    		return false;
    	}
    	for (int i = 0; i < t.length; i++) {
    		if(t.getField(i).getType() != td.getFieldType(i)) {
    			return false;
    		}
    	}
    	tuples.add(t);
    	return true;
    }
    
    public boolean remove(Tuple t) {
    	return tuples.remove(t);
    }
    
    public int size() {
    	return tuples.size();
    }
    
    public TableDescriptor getTD() {
    	return td; // to test if parsing is working
    }
}
