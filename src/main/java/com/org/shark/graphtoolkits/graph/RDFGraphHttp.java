package com.org.shark.graphtoolkits.graph;

import com.org.shark.graphtoolkits.utils.TransHttp;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class RDFGraphHttp {
    private static final Logger logger = LogManager.getLogger(RDFGraphHttp.class.getName());

    public enum Direction {
        IN,
        OUT,
        BOTH
    }

    public TransHttp sparqlEnd;

    public RDFGraphHttp() {
        sparqlEnd = new TransHttp();
    }

    public void regHttpGraph(String kgHttpPath) {
        sparqlEnd.getHttpEnd(kgHttpPath);
    }

    /**
     * Field Accessor
     * */

    public ArrayList<EdgeHttp> getNbrList(String entst, Direction dir) {
        if(dir == Direction.IN) {
            String ques = "SELECT * WHERE {?s ?p " + entst + " .}";

            ArrayList<EdgeHttp> resArray = new ArrayList<EdgeHttp>();
            ResultSet results = sparqlEnd.getFromHttp(ques);

            while (results.hasNext() ) {
                QuerySolution qs = results.next(); 
                resArray.add(new EdgeHttp(toFormat(qs.get("s").toString()), toFormat(qs.get("p").toString()), 0));
            }

            return resArray;
        }
        else if(dir == Direction.OUT) {
            String ques = "SELECT * WHERE { " + entst + " ?p ?o .}";

            ArrayList<EdgeHttp> resArray = new ArrayList<EdgeHttp>();
            ResultSet results = sparqlEnd.getFromHttp(ques);

            while (results.hasNext() ) {
                QuerySolution qs = results.next(); 
                resArray.add(new EdgeHttp(toFormat(qs.get("o").toString()), toFormat(qs.get("p").toString()), 0));
            }

            return resArray;
        }
        ArrayList<EdgeHttp> resArray = new ArrayList<EdgeHttp>();
        String ques = "SELECT * WHERE { {?s ?p " + entst + " .} UNION { " + entst + " ?p ?o . } }";

        ResultSet results = sparqlEnd.getFromHttp(ques);

        while (results.hasNext() ) {
            QuerySolution qs = results.next(); 
            resArray.add(new EdgeHttp(toFormat(qs.get("s").toString()), toFormat(qs.get("p").toString()), 0));
            resArray.add(new EdgeHttp(toFormat(qs.get("o").toString()), toFormat(qs.get("p").toString()), 0));
        }
        return resArray;
    }

    public Set<String> getVertexSet() {
        String ques = "SELECT * WHERE {?s ?p ?o .}";
        Set<String> resSet = new HashSet<String>();

        ResultSet results = sparqlEnd.getFromHttp(ques);
        while (results.hasNext() ) {
            QuerySolution qs = results.next(); 
            resSet.add(toFormat(qs.get("s").toString()));
            resSet.add(toFormat(qs.get("o").toString()));
        }

        return resSet;
    }

    public String toFormat(String str) {
        return "<" + str + ">";
    }

}
