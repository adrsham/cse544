package main;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import main.TableDescriptor.TQDItem;
import Zql.ZConstant;
import Zql.ZExp;
import Zql.ZExpression;
import Zql.ZFromItem;
import Zql.ZQuery;
import Zql.ZSelectItem;

public class QueryGenerator {
    private static DatabaseConnector con;

    public static String generate(ZQuery originalQuery, Table original, Table modified) {
        con = DatabaseConnector.getInstance();
        ZQuery modifiedQuery = new ZQuery();
        modifiedQuery.addSelect(getModifiedSelectList(originalQuery, original, modified));
        modifiedQuery.addFrom(originalQuery.getFrom());
        getModifiedWhereExpressions(originalQuery, modifiedQuery, modified);
        return modifiedQuery.toString();
    }
    
    public static void getModifiedWhereExpressions(ZQuery originalQuery, ZQuery curModifiedQuery, Table modified) {
        if (originalQuery.getWhere() == null ) {
            return;
        } else if (!(originalQuery.getWhere() instanceof ZExpression)) {
            System.err.println("Nested queries are currently unsupported.");
            System.err.println(originalQuery.getWhere());
            return;
        }
        
        ZExpression whereClause = (ZExpression) originalQuery.getWhere();
        // TODO currently going to assume that there is no alias going on. must fix this.
        curModifiedQuery.addWhere(whereClause);
        String res = con.runSQL(curModifiedQuery.toString());
        //System.out.println(res);
        // table with same where as original sql but same projections as modified table from file.
        Table curOriginal = Util.parseStringToTable(res);
        if (curOriginal.equals(modified)) {
            // correct, just return here
            return;
        }
        Set<ZExpression> exp = getWhereClauses(whereClause);
        if (curOriginal.contains(modified)) {
            // the original is less restricting then the new table provided. that means new where clauses need to be added
            // how do we do this?
            
        } else if (modified.contains(curOriginal)) {
            if (!recursiveRemoveWhere(new HashSet<ZExpression[]>(), exp, curModifiedQuery, modified)) {
                System.err.println("System was not able to find matching SQL statement.");
            }
        } else {
            // still trying to figure this part out
        }
        return;
    }
    
    private static boolean recursiveRemoveWhere(Set<ZExpression[]> tried, Set<ZExpression> cur, ZQuery curModQuery, Table modified) {
        if (tried.contains(cur)) {
            return false;
        } else {
            ZExpression[] expArray = new ZExpression[cur.size()];
            expArray = cur.toArray(expArray);
            tried.add(expArray);
            for (int i = 0; i < expArray.length; i++) {
                cur.remove(expArray[i]);
                // check to see if it is right or if it needs more removing.
                curModQuery.addWhere(convertSetToOneExp(cur));
                String res = con.runSQL(curModQuery.toString());
                Table curMod = Util.parseStringToTable(res);
                if (modified.equals(curMod)) {
                    return true;
                } else if (modified.contains(curMod) && recursiveRemoveWhere(tried, cur, curModQuery, modified)) {
                    // current check is still contained in modified, need to get rid of more where clauses
                    return true;
                }
                cur.add(expArray[i]);
            }
            return false;
        }
    }
    
    private static ZExp convertSetToOneExp(Set<ZExpression> set) {
        if (set.size() == 0) {
            return null;
        } else if (set.size() == 1) {
            return set.iterator().next();
        } else {
            ZExpression newExp = new ZExpression("AND");
            for (ZExpression exp : set) {
                newExp.addOperand(exp);
            }
            return newExp;
        }
    }
    
    @SuppressWarnings("unchecked")
    private static Set<ZExpression> getWhereClauses(ZExpression wc) {
        Set<ZExpression> res = new HashSet<ZExpression>();
        if (wc.getOperator().equals("AND")) {
            for (int i = 0; i < wc.nbOperands(); i++) {
                if (!(wc.getOperand(i) instanceof ZExpression)) {
                    System.err.println("Nested queries are currently unsupported.");
                    return null;
                }
                ZExpression cur = (ZExpression) wc.getOperand(i);
                Vector<ZExp> ops = cur.getOperands();
                if (ops.size() != 2) {
                    System.err.println("Only simple binary expresssions of the form A op B are currently supported.");
                    return null;
                }
                if (!(ops.elementAt(0) instanceof ZConstant && ops.elementAt(1) instanceof ZConstant)) {
                    System.err.println("Joins are currently not supported.");
                    return null;
                }
                ZConstant elm0 = (ZConstant) ops.elementAt(0);
                ZConstant elm1 = (ZConstant) ops.elementAt(1);
                if (elm0.getType() == ZConstant.COLUMNNAME && elm1.getType() != ZConstant.COLUMNNAME) {
                    res.add(new ZExpression(cur.getOperator(), elm0, elm1));
                } else if (elm1.getType() == ZConstant.COLUMNNAME && elm0.getType() != ZConstant.COLUMNNAME) {
                    res.add(new ZExpression(cur.getOperator(), elm1, elm0));
                } else {
                    System.err.println("Joins are currently not supported.");
                    return null;
                }
            }
        } else if (wc.getOperator().equals("OR")) {
            System.err.println("OR in where clauses are currently unsupported.");
            return null;
        } else {
            Vector<ZExp> ops = wc.getOperands();
            if (ops.size() != 2) {
                System.err.println("Only simple binary expresssions of the form A op B are currently supported.");
                return null;
            }
            if (!(ops.elementAt(0) instanceof ZConstant && ops.elementAt(1) instanceof ZConstant)) {
                System.err.println("Joins are currently not supported.");
                return null;
            }
            ZConstant elm0 = (ZConstant) ops.elementAt(0);
            ZConstant elm1 = (ZConstant) ops.elementAt(1);
            if (elm0.getType() == ZConstant.COLUMNNAME && elm1.getType() != ZConstant.COLUMNNAME) {
                res.add(new ZExpression(wc.getOperator(), elm0, elm1));
            } else if (elm1.getType() == ZConstant.COLUMNNAME && elm0.getType() != ZConstant.COLUMNNAME) {
                res.add(new ZExpression(wc.getOperator(), elm1, elm0));
            } else {
                System.err.println("Joins are currently not supported.");
                return null;
            }
        }
        return res;
    }
/*
    public static ZExp getModifiedWhereList(ZQuery originalQuery, Table original, Table modified) {

        if (wx.getOperator().equals("AND")) {
            String exp = "";
            System.out.println("number of operands:" + wx.nbOperands());
            for (int i = 0; i < wx.nbOperands(); i++) {
                if (!(wx.getOperand(i) instanceof ZExpression)) {
                    System.err.println("Nested queries are currently unsupported.");
                    return null;
                }
                ZExpression newWx = (ZExpression) wx.getOperand(i);
                Vector<ZExp> ops = newWx.getOperands();
                if (ops.size() != 2) {
                    System.err.println("Only simple binary expresssions of the form A op B are currently supported.");
                    return null;
                }

                boolean isJoin = false;
                boolean op1const = ops.elementAt(0) instanceof ZConstant; // otherwise
                // is a Query
                boolean op2const = ops.elementAt(1) instanceof ZConstant; // otherwise
                // is a Query
                if (op1const && op2const) {
                    isJoin = ((ZConstant) ops.elementAt(0)).getType() == ZConstant.COLUMNNAME
                            && ((ZConstant) ops.elementAt(1)).getType() == ZConstant.COLUMNNAME;
                } else if (ops.elementAt(0) instanceof ZQuery
                        || ops.elementAt(1) instanceof ZQuery) {
                    isJoin = true;
                    // currently not supported
                    System.err.println("Subqueries are currently unsupported.");
                    return null;
                } else if (ops.elementAt(0) instanceof ZExpression
                        || ops.elementAt(1) instanceof ZExpression) {
                    System.err.println("Only simple binary expresssions of the form A op B are currently " +
                            "supported, where A or B are fields, or constants.");
                    return null;
                } else {
                    isJoin = false;
                }

                if (!isJoin) { // select node
                    String column;
                    String compValue;
                    ZConstant op1 = (ZConstant) ops.elementAt(0);
                    ZConstant op2 = (ZConstant) ops.elementAt(1);
                    if (op1.getType() == ZConstant.COLUMNNAME) {
                        column = op1.getValue();
                        compValue = new String(op2.getValue());
                        exp += column + "(column) " + newWx.getOperator() + " " + compValue + "(comp value)";
                    } else {
                        column = op2.getValue();
                        compValue = new String(op1.getValue());
                        exp += compValue + "(comp value)" + newWx.getOperator() + " " + column + "(column) ";
                    }
                }
                exp += "\n\t";
            }
            return exp;
        }
        return null;
    }
*/

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
