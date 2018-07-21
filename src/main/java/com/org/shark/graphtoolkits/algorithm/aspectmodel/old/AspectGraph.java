package com.org.shark.graphtoolkits.algorithm.aspectmodel.old;

import com.org.shark.graphtoolkits.algorithm.aspectmodel.CompoundAspect;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.*;

public class AspectGraph {
    private HashMap<CompoundAspect, Integer> caspect2Id;
    private HashMap<Integer, HashSet<Integer>> graph;
    public AspectGraph() {
        caspect2Id = new HashMap<CompoundAspect, Integer>();
        graph = new HashMap<Integer, HashSet<Integer>>();
    }

    /**
     *
     * add a new tmpCompundAspect
     *
     * @param tmpCompoundAspect
     */
    public void addCompoundAspect(ArrayList<Integer> tmpCompoundAspect) {
        CompoundAspect ca = new CompoundAspect(tmpCompoundAspect);
        // 1. create Id for compound aspect
        if(!caspect2Id.containsKey(ca)) {
            Integer id = caspect2Id.size();
            caspect2Id.put(ca, id);
            graph.put(id, new HashSet<Integer>());
        }
    }

    /**
     * This function builds the graph with set containment validation.
     */
    public void buildGraph() {
        TreeSet<CompoundAspect> caSet = new TreeSet<CompoundAspect>();
        for(CompoundAspect ca : caspect2Id.keySet()) {
            caSet.add(ca);
        }

        ArrayList<CompoundAspect> visList = new ArrayList<CompoundAspect>();
        for(CompoundAspect ca : caSet) {
            int id = caspect2Id.get(ca);
            ArrayList<CompoundAspect> parants = new ArrayList<CompoundAspect>();
            HashSet<CompoundAspect> removeList = new HashSet<CompoundAspect>();
            for(CompoundAspect candParant : visList) {
                if(candParant.containsAll(ca)) {
                    for(CompoundAspect tmp : parants) {
                        if(tmp.containsAll(candParant)) {
                            removeList.add(tmp);
                        }
                    }
                    parants.add(candParant);
                }
            }
            visList.add(ca);
            for(CompoundAspect parant : parants) {
                if(!removeList.contains(parant)) {
                    graph.get(caspect2Id.get(parant)).add(id);
                }
            }
        }
    }

    public void save(String outfile) {
        try {
            FileOutputStream fout = new FileOutputStream(outfile);
            BufferedWriter fwr = new BufferedWriter(new OutputStreamWriter(fout));
            fwr.write("Dictionary:\n");
            for (CompoundAspect ca : caspect2Id.keySet()) {
                int caId = caspect2Id.get(ca);
                StringBuffer sb = new StringBuffer();
                sb.append(caId);
                sb.append(" : ");
                for(int ele : ca) {
                    sb.append(ele+", ");
                }
                sb.append("\n");
                fwr.write(sb.toString());
            }
            fwr.write("Graph:\n");
            for(int vid : graph.keySet()) {
                HashSet<Integer> nbrs = graph.get(vid);
                StringBuffer sb = new StringBuffer();
                sb.append(vid);
                sb.append(" : ");
                for(int ele : nbrs) {
                    sb.append(ele+", ");
                }
                sb.append("\n");
                fwr.write(sb.toString());
            }
            fwr.flush();
            fwr.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
