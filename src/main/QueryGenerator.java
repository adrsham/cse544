package main;

import Zql.ZQuery;

public class QueryGenerator {


    /*private QueryGenerator() {
		//static class
	}*/

    public static String generate(ZQuery originalQuery, Table original, Table modified) {
        Util.findRealTableDescriptor(modified);
        return "original " + original.toString() + ", modified " + modified.toString();
    }
}
