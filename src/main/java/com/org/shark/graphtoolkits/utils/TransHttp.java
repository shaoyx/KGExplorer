package com.org.shark.graphtoolkits.utils;

import org.apache.jena.query.*;

public class TransHttp {
    /* 
     * initially "http://162.105.146.140:3001/sparql"
     */
    private String RDFendpoint;

    public TransHttp() {
        RDFendpoint = "http://162.105.146.140:3001/sparql";
    }

    public void getHttpEnd(String kgPathHttp) {
        RDFendpoint = kgPathHttp;
    }

    public String gerEndAdr(){
        return RDFendpoint;
    }

    public ResultSet getFromHttp (String qe) {
        ParameterizedSparqlString qs = new ParameterizedSparqlString(qe);
        QueryExecution exec = QueryExecutionFactory.sparqlService(RDFendpoint, qs.asQuery());
        
        ResultSet results = ResultSetFactory.copyResults(exec.execSelect() );
        return results;
    }

}