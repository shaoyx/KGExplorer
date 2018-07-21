package com.org.shark.graphtoolkits.graph;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class RDFGraph {
    private static final Logger logger = LogManager.getLogger(RDFGraph.class.getName());

    public enum Direction {
        IN,
        OUT,
        BOTH
    }

    // Dictionary
    private HashMap<String, Integer> nodeLabel2Id;
    private HashMap<String, Integer> edgeLabel2Id;
    private HashMap<Integer, String> id2NodeLabel;
    private HashMap<Integer, String> id2EdgeLabel;

    //Graph Topology
    private HashMap<Integer, ArrayList<Edge>> outEdgeList;
    private HashMap<Integer, ArrayList<Edge>> inEdgeList;
    private int edgeSize;
    private int vertexSize;

    public RDFGraph() {
        nodeLabel2Id = new HashMap<String, Integer>();
        edgeLabel2Id = new HashMap<String, Integer>();
        id2NodeLabel = new HashMap<Integer, String>();
        id2EdgeLabel = new HashMap<Integer, String>();
        outEdgeList = new HashMap<Integer, ArrayList<Edge>>();
        inEdgeList = new HashMap<Integer, ArrayList<Edge>>();
    }
    /**
     * Field Accessor
     * */
    public ArrayList<Edge> getNbrList(Integer eid, Direction dir) {
        if(dir == Direction.IN) {
            return this.inEdgeList.get(eid);
        }
        else if(dir == Direction.OUT) {
            return this.outEdgeList.get(eid);
        }
        ArrayList<Edge> res = new ArrayList<Edge>();
        res.addAll(this.inEdgeList.get(eid));
        res.addAll(this.outEdgeList.get(eid));
        return res;
    }

    public Set<Integer> getVertexSet() {
        return this.outEdgeList.keySet();
    }

    /***
     * Dictionary related methods
     */
    public ArrayList<Integer> getEntityIds(ArrayList<String> entitySet) {
        ArrayList<Integer> res = new ArrayList<Integer>();
        for(String entityName : entitySet) {
            res.add(nodeLabel2Id.get(entityName));
        }
        return res;
    }

    public String getEdgeLabel(int relId) {
        return this.id2EdgeLabel.get(relId);
    }

    public String getNodeLabel(int eid) {
        return this.id2NodeLabel.get(eid);
    }

    /**
     * Loader
     * @param filepath
     */
    public void loadGraphFromTriples(String filepath) {
        try {
            final File folder = new File(filepath);
            ArrayList<String> files = this.listFilesForFolder(folder);
            for(String file : files) {
                logger.info("Processing file "+file);
                FileInputStream fin = new FileInputStream(file);
                BufferedReader fbr = new BufferedReader(new InputStreamReader(fin));
                String line;

                while ((line = fbr.readLine()) != null) {
                    if (line.startsWith("#")) continue;
                    int firstSeperator = line.indexOf(" ");
                    int secondSeperator = line.indexOf(" ", firstSeperator + 1);
                    int lastSeperator = line.lastIndexOf(" ");

                    String sub = line.substring(0, firstSeperator);
                    String pre = line.substring(firstSeperator + 1, secondSeperator);
                    String obj = line.substring(secondSeperator + 1, lastSeperator);

                    /*
                     * Only entity is cared!!
                     */
                    if(!isInterestTriple(sub, pre, obj)) {
                        continue;
                    }

                    Integer subId = getNodeDict(sub);
                    Integer preId = getEdgeDict(pre);
                    Integer objId = getNodeDict(obj);

                    addEdge(subId, preId, objId);
                    edgeSize++;
                }
                vertexSize = this.nodeLabel2Id.size();
                System.out.println("Vertex=" + vertexSize + " Edge=" + edgeSize);
                fbr.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveGraph(String outpath) {
        try {
            FileOutputStream fout = new FileOutputStream(outpath);
            BufferedWriter fwr = new BufferedWriter(new OutputStreamWriter(fout));
            fwr.write("#Entity Mapping\n");
            for (int vid : id2NodeLabel.keySet()) {
                String label = id2NodeLabel.get(vid);
                StringBuffer sb = new StringBuffer();
                sb.append(vid);
                sb.append(" ");
                sb.append(label);
                sb.append("\n");
                fwr.write(sb.toString());
            }
            fwr.write("#Edge Label Mapping\n");
            for(int eid : id2EdgeLabel.keySet()) {
                String label = id2EdgeLabel.get(eid);
                StringBuffer sb = new StringBuffer();
                sb.append(eid);
                sb.append(" ");
                sb.append(label);
                sb.append("\n");
                fwr.write(sb.toString());
            }
            fwr.flush();
            fwr.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Filter out triple with literal
     * @param sub
     * @param pre
     * @param obj
     * @return
     */
    private boolean isInterestTriple(String sub, String pre, String obj) {
        return obj.startsWith("<");
    }

    private ArrayList<String> listFilesForFolder(final File folder) {
        ArrayList<String> res = new ArrayList<String>();
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                res.addAll(listFilesForFolder(fileEntry));
            } else {
                res.add(fileEntry.getAbsolutePath());
            }
        }
        return res;
    }

    private Integer getNodeDict(String label) {
        if(!this.nodeLabel2Id.containsKey(label)){
            Integer nid = this.nodeLabel2Id.size();
            this.nodeLabel2Id.put(label, nid);
            this.id2NodeLabel.put(nid, label);
            this.outEdgeList.put(nid, new ArrayList<Edge>());
            this.inEdgeList.put(nid, new ArrayList<Edge>());
        }
        return this.nodeLabel2Id.get(label);
    }

    private Integer getEdgeDict(String label) {
        if(!this.edgeLabel2Id.containsKey(label)){
            Integer nid = this.edgeLabel2Id.size();
            this.edgeLabel2Id.put(label, nid);
            this.id2EdgeLabel.put(nid, label);
        }
        return this.edgeLabel2Id.get(label);
    }

    private void addEdge(Integer sub, Integer pre, Integer obj) {
       Edge outEdge = new Edge(obj, pre, 0);
       Edge inEdge = new Edge(sub, pre, 0);
       this.outEdgeList.get(sub).add(outEdge);
       this.inEdgeList.get(obj).add(inEdge);
    }
}
