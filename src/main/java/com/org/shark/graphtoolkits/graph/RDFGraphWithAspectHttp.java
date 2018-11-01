package com.org.shark.graphtoolkits.graph;

import com.org.shark.graphtoolkits.algorithm.aspectmodel.CompoundAspectHttp;
import com.org.shark.graphtoolkits.algorithm.aspectmodel.SingleAspectHttp;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

import java.util.*;
public class RDFGraphWithAspectHttp extends RDFGraphHttp {

    /**
     * Aspect Dictionary
     */
    private Direction aspectDir;

    public RDFGraphWithAspectHttp() {
        super();
        this.aspectDir = Direction.BOTH;
    }

    /**
     * Analysis API
     * @return
     */
    public HashMap<String, Integer> sparsityAnalysis() {
        HashMap<String, Integer> degreeCount = new HashMap<String, Integer>();
        for (String ent : this.getVertexSet()) {
            ArrayList<EdgeHttp> outNbrList = this.getNbrList(ent, Direction.OUT);
            ArrayList<EdgeHttp> inNbrList = this.getNbrList(ent, Direction.IN);
            HashSet<SingleAspectHttp> aspectNbr = new HashSet<SingleAspectHttp>();
            for (EdgeHttp edge : outNbrList) {
                SingleAspectHttp relAspect = new SingleAspectHttp(edge.getLabel());
                SingleAspectHttp poFactAspect = new SingleAspectHttp(SingleAspectHttp.SAType.PO, edge.getEntity(), edge.getLabel());
                aspectNbr.add(relAspect);
                aspectNbr.add(poFactAspect);
            }
            for (EdgeHttp edge : inNbrList) {
                SingleAspectHttp relAspect = new SingleAspectHttp(edge.getLabel());
                SingleAspectHttp spFactAspect = new SingleAspectHttp(SingleAspectHttp.SAType.SP, edge.getEntity(), edge.getLabel());
                aspectNbr.add(relAspect);
                aspectNbr.add(spFactAspect);
            }

            degreeCount.put(ent, aspectNbr.size());

        }

        return degreeCount;
    }

    public int aspectSize() {
        Set<SingleAspectHttp> aspSet = new HashSet<SingleAspectHttp>();

        /* *
        for (String ent : this.getVertexSet()) {
            ArrayList<EdgeHttp> outNbrList = this.getNbrList(ent, Direction.OUT);
            ArrayList<EdgeHttp> inNbrList = this.getNbrList(ent, Direction.IN);
            for (EdgeHttp edge : outNbrList) {
                SingleAspectHttp relAspect = new SingleAspectHttp(edge.getLabel());
                SingleAspectHttp poFactAspect = new SingleAspectHttp(SingleAspectHttp.SAType.PO, edge.getEntity(), edge.getLabel());
                aspSet.add(relAspect);
                aspSet.add(poFactAspect);
            }
            for (EdgeHttp edge : inNbrList) {
                SingleAspectHttp relAspect = new SingleAspectHttp(edge.getLabel());
                SingleAspectHttp spFactAspect = new SingleAspectHttp(SingleAspectHttp.SAType.SP, edge.getEntity(), edge.getLabel());
                aspSet.add(relAspect);
                aspSet.add(spFactAspect);
            }

        }
        return aspSet.size();
        */

        String ques = "SELECT * WHERE {?s ?p ?o .}";
        ResultSet results = sparqlEnd.getFromHttp(ques);
        while (results.hasNext() ) {
            QuerySolution qs = results.next();
            SingleAspectHttp relAspect = new SingleAspectHttp(toFormat(qs.get("p").toString()));
            SingleAspectHttp spFactAspect = new SingleAspectHttp(SingleAspectHttp.SAType.SP, toFormat(qs.get("s").toString()), toFormat(qs.get("p").toString()));
            SingleAspectHttp poFactAspect = new SingleAspectHttp(SingleAspectHttp.SAType.PO, toFormat(qs.get("o").toString()), toFormat(qs.get("p").toString()));
            aspSet.add(relAspect);
            aspSet.add(spFactAspect);
            aspSet.add(poFactAspect);
        }
        return aspSet.size();
        
    }

    public int entitySize() {
        return this.getVertexSet().size();
    }


    /**
     * get the aspects that an entity refers to
     * @param ent
     * @return
     */
    public ArrayList<SingleAspectHttp> getAspects(String ent) {
        HashSet<SingleAspectHttp> res = new HashSet<SingleAspectHttp>();
        if(this.aspectDir == Direction.BOTH || this.aspectDir == Direction.OUT) {
            ArrayList<EdgeHttp> outNbrList = this.getNbrList(ent, Direction.OUT);
            for (EdgeHttp edge : outNbrList) {
                SingleAspectHttp relAspect = new SingleAspectHttp(edge.getLabel());
                SingleAspectHttp factAspect = new SingleAspectHttp(SingleAspectHttp.SAType.PO, edge.getEntity(), edge.getLabel());
                res.add(relAspect);
                res.add(factAspect);
            }
        }
        if(this.aspectDir == Direction.BOTH || this.aspectDir == Direction.IN) {
            ArrayList<EdgeHttp> inNbrList = this.getNbrList(ent, Direction.IN);
            for (EdgeHttp edge : inNbrList) {
                SingleAspectHttp relAspect = new SingleAspectHttp(edge.getLabel());
                SingleAspectHttp factAspect = new SingleAspectHttp(SingleAspectHttp.SAType.SP, edge.getEntity(), edge.getLabel());
                res.add(relAspect);
                res.add(factAspect);
            }
        }
        return new ArrayList<SingleAspectHttp>(res);
    }

    public ArrayList<String> getEntityWithAspect(SingleAspectHttp singleAspect) {
        ArrayList<String> res = new ArrayList<String>();
        Direction dir = Direction.BOTH;
        if(singleAspect.getSAType() == SingleAspectHttp.SAType.PO) {
            dir = Direction.OUT;
        }
        else if(singleAspect.getSAType() == SingleAspectHttp.SAType.SP) {
            dir = Direction.IN;
        }
        /* *
        for(String ent : this.getVertexSet()) {
            ArrayList<EdgeHttp> nbrList = this.getNbrList(ent, dir);
            for(EdgeHttp edge : nbrList) {
                SingleAspectHttp relAspect = new SingleAspectHttp(edge.getLabel());
                SingleAspectHttp factAspect = new SingleAspectHttp(singleAspect.getSAType(), edge.getEntity(), edge.getLabel());
                if(relAspect.equals(singleAspect) || factAspect.equals(singleAspect)) {
                    res.add(ent);
                    break;
                }
            }
        }
        return res;
        */
        String ques = "SELECT * WHERE {?s " + singleAspect.getRel() + " ?o .}";
        if (dir == Direction.IN) {
            ques = "SELECT ?o WHERE {" + singleAspect.getEntity() + " " + singleAspect.getRel() + " ?o .}";
            ResultSet results = sparqlEnd.getFromHttp(ques);
            while (results.hasNext()) {
                QuerySolution qs = results.next();
                res.add(toFormat(qs.get("o").toString()));
            }
            return res;
        } 
        else if (dir == Direction.OUT) {
            ques = "SELECT ?s WHERE { ?s " + singleAspect.getRel() + " " + singleAspect.getEntity() + " .}";
            ResultSet results = sparqlEnd.getFromHttp(ques);
            while (results.hasNext()) {
                QuerySolution qs = results.next();
                res.add(toFormat(qs.get("s").toString()));
            }
            return res;
        }
        
        ResultSet results = sparqlEnd.getFromHttp(ques);
        while (results.hasNext() ) {
            QuerySolution qs = results.next();
            res.add(toFormat(qs.get("s").toString()));
            res.add(toFormat(qs.get("o").toString()));
        }
        return res;

    }

    /**
     * This method is expensive!!!
     * @param ent
     * @return
     */
    public HashSet<SingleAspectHttp> getAspectSet(String ent) {
        HashSet<SingleAspectHttp> res = new HashSet<SingleAspectHttp>();
        if(this.aspectDir == Direction.BOTH || this.aspectDir == Direction.OUT) {
            ArrayList<EdgeHttp> outNbrList = this.getNbrList(ent, Direction.OUT);
            for (EdgeHttp edge : outNbrList) {
                SingleAspectHttp factAspect = new SingleAspectHttp(SingleAspectHttp.SAType.PO, edge.getEntity(), edge.getLabel());
                res.add(factAspect);
            }
        }
        if(this.aspectDir == Direction.BOTH || this.aspectDir == Direction.IN) {
            ArrayList<EdgeHttp> inNbrList = this.getNbrList(ent, Direction.IN);
            for (EdgeHttp edge : inNbrList) {
                SingleAspectHttp factAspect = new SingleAspectHttp(SingleAspectHttp.SAType.SP, edge.getEntity(), edge.getLabel());
                res.add(factAspect);
            }
        }
        return res;
    }

    /**
     * compute the union of aspects related to a set of examples.
     * @param examples
     * @return
     */
    public HashSet<SingleAspectHttp> getAspectSet(ArrayList<String> examples) {
        HashSet<SingleAspectHttp> res = new HashSet<SingleAspectHttp>();
        for(String ent : examples) {
            res.addAll(this.getAspectSet(ent));
        }
        return res;
    }

    /**
     * query entity based on aspect.
     * @param ca
     * @return
     */
    public ArrayList<String> query(CompoundAspectHttp ca) {
        ArrayList<String> entityList = new ArrayList<String>();

        /* *
        for(String ent : this.getVertexSet()) {
            HashSet<SingleAspectHttp> entityAspects = this.getAspectSet(ent);
            if(entityAspects.containsAll(ca)) {
                entityList.add(ent);
            }
        }
        return entityList;
        */

        String ques = "SELECT * WHERE { ";
        String srcName = "?source";
        for (SingleAspectHttp asp : ca) {
            if (asp.getSAType() == SingleAspectHttp.SAType.REL) {
                System.out.println("error in collecting compound aspects!!");
                break;
            }
            else if (asp.getSAType() == SingleAspectHttp.SAType.PO) {
                ques = ques + srcName + " " + asp.getRel() + " " + asp.getEntity() + " . ";
            }
            else if (asp.getSAType() == SingleAspectHttp.SAType.SP) {
                ques = ques + asp.getEntity() + " " + asp.getRel() + " " + srcName + " . ";
            }
        }
        ques += " }";

        ResultSet results = sparqlEnd.getFromHttp(ques);
        while (results.hasNext()) {
            QuerySolution qs = results.next();
            entityList.add(toFormat(qs.get("source").toString()));
        }
        
        return entityList;
    }

    public HashSet<CompoundAspectHttp> computeMaximalAspect(Collection<String> examples, ArrayList<String> negExample) {
        HashSet<CompoundAspectHttp> res = new HashSet<CompoundAspectHttp>();
        for(String ent : examples) {
            HashSet<SingleAspectHttp> nbrSet = this.getAspectSet(ent);

            for(String neg : negExample) {
                CompoundAspectHttp ca = new CompoundAspectHttp();
                if(this.aspectDir == Direction.BOTH || this.aspectDir == Direction.OUT) {
                    ArrayList<EdgeHttp> outNbrList = this.getNbrList(neg, Direction.OUT);
                    for (EdgeHttp edge : outNbrList) {
                        SingleAspectHttp factAspect = new SingleAspectHttp(SingleAspectHttp.SAType.PO, edge.getEntity(), edge.getLabel());
                        if(nbrSet.contains(factAspect)) {
                            ca.add(factAspect);
                        }
                    }
                }
                if(this.aspectDir == Direction.BOTH || this.aspectDir == Direction.IN) {
                    ArrayList<EdgeHttp> inNbrList = this.getNbrList(neg, Direction.IN);
                    for (EdgeHttp edge : inNbrList) {
                        SingleAspectHttp factAspect = new SingleAspectHttp(SingleAspectHttp.SAType.SP, edge.getEntity(), edge.getLabel());
                        
                        if(nbrSet.contains(factAspect)) {
                            ca.add(factAspect);
                        }
                    }
                }
                if(!ca.isEmpty()) {
                    res.add(ca);
                }
            }
        }
        return res;
    }

    /**
     * Methods for print
     */
    public ArrayList<CompoundAspectHttp> getCompoundAspectList(ArrayList<String> examples) {
        ArrayList<CompoundAspectHttp> res = new ArrayList<CompoundAspectHttp>();
        for(String ent : examples) {
            CompoundAspectHttp entityAspects = new CompoundAspectHttp();
            HashSet<SingleAspectHttp> tmpCa = this.getAspectSet(ent);
            entityAspects.addAll(tmpCa); //TODO: how do we maintain the score of compound aspects.
            res.add(entityAspects);
        }
        return res;
    }

    public String aspectStr(SingleAspectHttp singleAspect) {
        String entity = singleAspect.getEntity();
        String rel = singleAspect.getRel();
        String saType = singleAspect.getSAType().toString();
        String res = saType + ": (";
        if(rel != "") {
            res += rel +", ";
        }
        else {
            res += "NULL, ";
        }
        res = res + entity+")";
        return res;
    }

    public String entityName(String ent) {
        return ent;
    }
}
