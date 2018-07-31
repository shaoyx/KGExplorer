package com.org.shark.graphtoolkits.graph;

import com.org.shark.graphtoolkits.algorithm.aspectmodel.CompoundAspect;
import com.org.shark.graphtoolkits.algorithm.aspectmodel.SingleAspect;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

public class RDFGraphWithAspect extends RDFGraph {

    /**
     * Aspect Dictionary
     */
    private HashMap<SingleAspect, Integer> aspect2Id;
    private HashMap<Integer, SingleAspect> id2aspect;
    private Direction aspectDir;


    public RDFGraphWithAspect() {
        super();
        this.aspect2Id = new HashMap<SingleAspect, Integer>();
        this.id2aspect = new HashMap<Integer, SingleAspect>();
        this.aspectDir = Direction.BOTH;
    }

    /**
     * build aspect index for RDF,
     */
    public void buildIndex() {
        for(Integer eid : this.getVertexSet()) {
            ArrayList<Edge> nbrList = this.getNbrList(eid, Direction.OUT);
            for (Edge edge : nbrList) {
                SingleAspect relAspect = new SingleAspect(edge.getLabel());
                SingleAspect poFactAspect = new SingleAspect(SingleAspect.SAType.PO, edge.getId(), edge.getLabel());
                SingleAspect spFactAspect = new SingleAspect(SingleAspect.SAType.SP, edge.getId(), edge.getLabel());
                indexAspect(relAspect);
                indexAspect(poFactAspect);
                indexAspect(spFactAspect);
            }
        }
    }

    public int indexAspect(SingleAspect aspect) {
        if(!this.aspect2Id.containsKey(aspect)) {
            Integer nid = this.aspect2Id.size();
            this.aspect2Id.put(aspect, nid);
            this.id2aspect.put(nid, aspect);
        }
        return this.aspect2Id.get(aspect);
    }

    /**
     * Analysis API
     * @return
     */
    public HashMap<Integer, Integer> sparsityAnalysis() {
        HashMap<Integer, Integer> degreeCount = new HashMap<Integer, Integer>();
        for(Integer eid : this.getVertexSet()) {
            ArrayList<Edge> outNbrList = this.getNbrList(eid, Direction.OUT);
            ArrayList<Edge> inNbrList = this.getNbrList(eid, Direction.IN);
            HashSet<Integer> aspectNbr = new HashSet<Integer>();
            for (Edge edge : outNbrList) {
                SingleAspect relAspect = new SingleAspect(edge.getLabel());
                SingleAspect poFactAspect = new SingleAspect(SingleAspect.SAType.PO, edge.getId(), edge.getLabel());
                int relId = indexAspect(relAspect);
                int poId = indexAspect(poFactAspect);
                aspectNbr.add(relId);
                aspectNbr.add(poId);
            }

            for (Edge edge : inNbrList) {
                SingleAspect relAspect = new SingleAspect(edge.getLabel());
                SingleAspect spFactAspect = new SingleAspect(SingleAspect.SAType.SP, edge.getId(), edge.getLabel());
                int relId = indexAspect(relAspect);
                int spId = indexAspect(spFactAspect);
                aspectNbr.add(relId);
                aspectNbr.add(spId);
            }

            degreeCount.put(eid, aspectNbr.size());
        }
        return degreeCount;
    }

    /**
     *
     * @param filepath
     */
    public void saveIndex(String filepath) {
        try {
            FileOutputStream fout = new FileOutputStream(filepath);
            BufferedWriter fwr = new BufferedWriter(new OutputStreamWriter(fout));
            for (int vid : id2aspect.keySet()) {
                SingleAspect singleAspect = id2aspect.get(vid);
                StringBuffer sb = new StringBuffer();
                sb.append(vid);
                sb.append(" ");
                sb.append(singleAspect.toString());
                sb.append("\n");
                fwr.write(sb.toString());
            }
            fwr.flush();
            fwr.close();

            fout = new FileOutputStream(filepath+".sparsity");
            fwr = new BufferedWriter(new OutputStreamWriter(fout));
            HashMap<Integer, Integer> degreeCount = sparsityAnalysis();
            fwr.write("tot "+this.id2aspect.size()+"\n");
            for(int eid : degreeCount.keySet()) {
                int val = degreeCount.get(eid);
                fwr.write(eid +" "+val+"\n");
            }
            fwr.flush();
            fwr.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int aspectSize() {
        return this.id2aspect.size();
    }

    public int entitySize() {
        return this.getVertexSet().size();
    }


    /**
     *
     * @param eid
     * @return
     */
    public ArrayList<Integer> getAspects(Integer eid) {
        HashSet<Integer> res = new HashSet<Integer>();
        if(this.aspectDir == Direction.BOTH || this.aspectDir == Direction.OUT) {
            ArrayList<Edge> outNbrList = this.getNbrList(eid, Direction.OUT);
            for (Edge edge : outNbrList) {
                SingleAspect relAspect = new SingleAspect(edge.getLabel());
                SingleAspect factAspect = new SingleAspect(SingleAspect.SAType.PO, edge.getId(), edge.getLabel());
                res.add(aspect2Id.get(relAspect));
                res.add(aspect2Id.get(factAspect));
            }
        }
        if(this.aspectDir == Direction.BOTH || this.aspectDir == Direction.IN) {
            ArrayList<Edge> inNbrList = this.getNbrList(eid, Direction.IN);
            for (Edge edge : inNbrList) {
                SingleAspect relAspect = new SingleAspect(edge.getLabel());
                SingleAspect factAspect = new SingleAspect(SingleAspect.SAType.SP, edge.getId(), edge.getLabel());
                res.add(aspect2Id.get(relAspect));
                res.add(aspect2Id.get(factAspect));
            }
        }
        return new ArrayList<Integer>(res);
    }

    public ArrayList<Integer> getEntityWithAspect(Integer aspectId) {
        ArrayList<Integer> res = new ArrayList<Integer>();
        SingleAspect singleAspect = this.id2aspect.get(aspectId);
        Direction dir = Direction.BOTH;
        if(singleAspect.getSAType() == SingleAspect.SAType.PO) {
            dir = Direction.OUT;
        }
        else if(singleAspect.getSAType() == SingleAspect.SAType.SP) {
            dir = Direction.IN;
        }
        for(Integer eid : this.getVertexSet()) {
            ArrayList<Edge> nbrList = this.getNbrList(eid, dir);
            for(Edge edge : nbrList) {
                SingleAspect relAspect = new SingleAspect(edge.getLabel());
                SingleAspect factAspect = new SingleAspect(singleAspect.getSAType(), edge.getId(), edge.getLabel());
                if(relAspect.equals(singleAspect) || factAspect.equals(singleAspect)) {
                    res.add(eid);
                    break;
                }
            }
        }
        return res;
    }

    /**
     * This method is expensive!!!
     * @param eid
     * @return
     */
    public HashSet<Integer> getAspectSet(Integer eid) {
        HashSet<Integer> res = new HashSet<Integer>();
        if(this.aspectDir == Direction.BOTH || this.aspectDir == Direction.OUT) {
            ArrayList<Edge> outNbrList = this.getNbrList(eid, Direction.OUT);
            for (Edge edge : outNbrList) {
                SingleAspect factAspect = new SingleAspect(SingleAspect.SAType.PO, edge.getId(), edge.getLabel());
                res.add(aspect2Id.get(factAspect));
            }
        }
        if(this.aspectDir == Direction.BOTH || this.aspectDir == Direction.IN) {
            ArrayList<Edge> inNbrList = this.getNbrList(eid, Direction.IN);
            for (Edge edge : inNbrList) {
                SingleAspect factAspect = new SingleAspect(SingleAspect.SAType.SP, edge.getId(), edge.getLabel());
                res.add(aspect2Id.get(factAspect));
            }
        }
        return res;
    }

    /**
     * compute the union of aspects related to a set of examples.
     * @param examples
     * @return
     */
    public HashSet<Integer> getAspectSet(ArrayList<Integer> examples) {
        HashSet<Integer> res = new HashSet<Integer>();
        for(int eid : examples) {
            res.addAll(this.getAspectSet(eid));
        }
        return res;
    }

    /**
     * query entity based on aspect.
     * @param ca
     * @return
     */
    public ArrayList<Integer> query(CompoundAspect ca) {
        ArrayList<Integer> entityList = new ArrayList<Integer>();
        for(int eid : this.getVertexSet()) {
            HashSet<Integer> entityAspects = this.getAspectSet(eid);
            if(entityAspects.containsAll(ca)) {
                entityList.add(eid);
            }
        }
        return entityList;
    }

    public HashSet<CompoundAspect> computeMaximalAspect(Collection<Integer> examples, ArrayList<Integer> negExample) {
        HashSet<CompoundAspect> res = new HashSet<CompoundAspect>();
        for(int eid : examples) {
            HashSet<Integer> nbrSet = this.getAspectSet(eid);

            for(int neg : negExample) {
                CompoundAspect ca = new CompoundAspect();
                if(this.aspectDir == Direction.BOTH || this.aspectDir == Direction.OUT) {
                    ArrayList<Edge> outNbrList = this.getNbrList(neg, Direction.OUT);
                    for (Edge edge : outNbrList) {
                        SingleAspect factAspect = new SingleAspect(SingleAspect.SAType.PO, edge.getId(), edge.getLabel());
                        int aspectId = aspect2Id.get(factAspect);
                        if(nbrSet.contains(aspectId)) {
                            ca.add(aspectId);
                        }
                    }
                }
                if(this.aspectDir == Direction.BOTH || this.aspectDir == Direction.IN) {
                    ArrayList<Edge> inNbrList = this.getNbrList(neg, Direction.IN);
                    for (Edge edge : inNbrList) {
                        SingleAspect factAspect = new SingleAspect(SingleAspect.SAType.SP, edge.getId(), edge.getLabel());
                        int aspectId = aspect2Id.get(factAspect);
                        if(nbrSet.contains(aspectId)) {
                            ca.add(aspectId);
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
    public ArrayList<CompoundAspect> getCompoundAspectList(ArrayList<Integer> examples) {
        ArrayList<CompoundAspect> res = new ArrayList<CompoundAspect>();
        for(int eid : examples) {
            CompoundAspect entityAspects = new CompoundAspect();
            HashSet<Integer> tmpCa = this.getAspectSet(eid);
            entityAspects.addAll(tmpCa); //TODO: how do we maintain the score of compound aspects.
            res.add(entityAspects);
        }
        return res;
    }

    public String aspectStr(Integer aspectId) {
        SingleAspect singleAspect = id2aspect.get(aspectId);
        int entityId = singleAspect.getEntityId();
        int relId = singleAspect.getRelId();
        String saType = singleAspect.getSAType().toString();
        String res = saType + ": (";
        if(relId != -1) {
            res += this.getEdgeLabel(relId) +", ";
        }
        else {
            res += "-1, ";
        }
        res = res + this.getNodeLabel(entityId)+")";
        return res;
    }

    public String entityName(Integer eid) {
        return this.getNodeLabel(eid);
    }
}
