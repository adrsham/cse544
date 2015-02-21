package main;

import java.util.List;
import java.util.Vector;

import main.TableDescriptor.TQDItem;
import Zql.ZFromItem;
import Zql.ZQuery;
import Zql.ZSelectItem;

public class QueryGenerator {


    /*private QueryGenerator() {
		//static class
	}*/

    public static String generate(ZQuery originalQuery, Table original, Table modified) {       
        ZQuery modifiedQuery = new ZQuery();
        modifiedQuery.addSelect(getModifiedSelectList(originalQuery, original, modified));
        modifiedQuery.addFrom(originalQuery.getFrom());

        //findRealTableDescriptor(originalQuery, original, modified);
        return modifiedQuery.toString();
    }


    /**
     * Modifies the table so that the table descriptor is correct with name, alias, and table of each column set correctly.
     * @param table
     */
    @SuppressWarnings("unchecked")
    public static Vector<ZSelectItem> getModifiedSelectList(ZQuery originalQuery, Table original, Table modified) {
        DatabaseConnector con = DatabaseConnector.getInstance();
        Vector<ZSelectItem> select = new Vector<ZSelectItem>();
        List<List<Field>> fieldsOriginal = original.getColOfFields();
        TableDescriptor tdOriginal = original.getTD();
        List<List<Field>> fieldsModified = modified.getColOfFields();
        TableDescriptor tdModified = modified.getTD();
        for (int i = 0; i < fieldsModified.size(); i++) {
            List<Field> modCol = fieldsModified.get(i);
            ZSelectItem cur = null;
            boolean found = false;
            for (int j = 0; j < fieldsOriginal.size(); j++) {
                List<Field> orgCol = fieldsOriginal.get(j);
                if (orgCol.containsAll(modCol)) {
                    // if this is the case, dont need to look at database because that col wil def contain all of modCol
                    TQDItem tdio = (TQDItem) tdOriginal.getFieldInfo(j);
                    TQDItem tdim = (TQDItem) tdModified.getFieldInfo(i);
                    tdim.fieldTable = tdio.fieldTable;
                    tdim.fieldName = tdio.fieldName;
                    cur = new ZSelectItem(tdim.fieldTable + "." + tdim.fieldName);
                    if (tdio.fieldName.equals(tdim.fieldAlias)) {
                        tdim.fieldAlias = null;
                    } else if (tdim.fieldAlias != null) {
                        cur.setAlias(tdim.fieldAlias);
                    }
                    found = true;
                    break;
                } else if (modCol.containsAll(orgCol)) {
                    // need to check full col to make sure mod is contained in it.
                    TQDItem tdio = (TQDItem) tdOriginal.getFieldInfo(j);
                    Table curTable = Util.parseStringToTable(con.runSQL(String.format(Util.GET_COLUMN_DATA_SQL, tdio.fieldName, tdio.fieldTable)));
                    List<Field> curFields = curTable.getColOfFields().get(0);
                    if (curFields.containsAll(modCol)) {
                        TQDItem tdim = (TQDItem) tdModified.getFieldInfo(i);
                        tdim.fieldTable = tdio.fieldTable;
                        tdim.fieldName = tdio.fieldName;
                        cur = new ZSelectItem(tdim.fieldTable + "." + tdim.fieldName);
                        if (tdio.fieldName.equals(tdim.fieldAlias)) {
                            tdim.fieldAlias = null;
                        } else if (tdim.fieldAlias != null) {
                            cur.setAlias(tdim.fieldAlias);
                        }
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                Vector<ZFromItem> from = originalQuery.getFrom();
                for (int k = 0; k < from.size(); k++) {
                    String tableName = from.elementAt(k).getTable();
                    TableDescriptor curTD = Util.dbInfo.get(tableName);
                    for (int j = 0; j < curTD.numFields(); j++) {
                        String colName = curTD.getFieldName(j);
                        Table curTable = Util.parseStringToTable(con.runSQL(String.format(Util.GET_COLUMN_DATA_SQL, colName, tableName)));
                        List<Field> curFields = curTable.getColOfFields().get(0);
                        if (curFields.containsAll(fieldsModified.get(i))) {
                            // the fields of the table passed in was contained inside the full list.
                            TQDItem tdim = (TQDItem) tdModified.getFieldInfo(i);
                            tdim.fieldTable = tableName;
                            tdim.fieldName = colName;
                            cur = new ZSelectItem(tdim.fieldTable + "." + tdim.fieldName);
                            if (tdim.fieldName.equals(tdim.fieldAlias)) {
                                tdim.fieldAlias = null;
                            } else if (tdim.fieldAlias != null) {
                                cur.setAlias(tdim.fieldAlias);
                            }
                            found = true;
                            break;
                        }
                    }
                    if (found) {
                        break;
                    }
                }
            }
            select.add(cur);
        }
        return select;
        /*
        List<List<Field>> fields = table.getColOfFields();
        TableDescriptor td = table.getTD();
        for (int i = 0; i < td.numFields(); i++) {
            boolean found = false;
            // loop through each num field and find out what the column info actually is.
            for (String tableName : Util.dbInfo.keySet()) {
                TableDescriptor curTD = Util.dbInfo.get(tableName);
                for (int j = 0; j < curTD.numFields(); j++) {
                    String colName = curTD.getFieldName(j);
                    Table curTable = Util.parseStringToTable(con.runSQL(String.format(Util.GET_COLUMN_DATA_SQL, colName, tableName)));
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
         */
    }
}
