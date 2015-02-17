package main;

import java.util.ArrayList;
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
    
    public List<List<Field>> getColOfFields() {
        List<List<Field>> list = new ArrayList<List<Field>>();
        for (int i = 0; i < td.numFields(); i++) {
            // create all the columns
            list.add(new ArrayList<Field>());
        }
        for (int i = 0; i < tuples.size(); i++) {
            Tuple t = tuples.get(i);
            for (int j = 0; j < t.length; j++) {
                list.get(j).add(t.getField(j));
            }
        }
        return list;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Table [td=" + td + "]";
    }
}
