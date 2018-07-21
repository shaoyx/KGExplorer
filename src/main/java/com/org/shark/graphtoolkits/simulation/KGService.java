package com.org.shark.graphtoolkits.simulation;

import com.org.shark.graphtoolkits.algorithm.aspectmodel.CompoundAspect;
import com.org.shark.graphtoolkits.graph.RDFGraphWithAspect;

import java.util.ArrayList;
import java.util.HashSet;

public class KGService implements IKGService {

    RDFGraphWithAspect rdfGraph = null;

    @Override
    public void initialization(String kgPath) {
        rdfGraph = new RDFGraphWithAspect();
        rdfGraph.loadGraphFromTriples(kgPath);
        rdfGraph.buildIndex();
        rdfGraph.saveGraph(kgPath+"/../index/graph.save");
        rdfGraph.saveIndex(kgPath+"/../index/graph.index");
    }

    @Override
    public ArrayList<Integer> getEntityIds(ArrayList<String> entityList) {
        return rdfGraph.getEntityIds(entityList);
    }

    @Override
    public RDFGraphWithAspect getKG() {
        return this.rdfGraph;
    }

    @Override
    public ArrayList<Integer> query(CompoundAspect currentState) {
        if(currentState == null)
            return new ArrayList<Integer>();
        return rdfGraph.query(currentState);
    }

    @Override
    public HashSet<CompoundAspect> computeMaximalAspect(HashSet<Integer> totPosExample, ArrayList<Integer> negExamples) {
        return rdfGraph.computeMaximalAspect(totPosExample, negExamples);
    }

    @Override
    public HashSet<Integer> getAspectSet(ArrayList<Integer> posExamples) {
        return rdfGraph.getAspectSet(posExamples);
    }

    @Override
    public ArrayList<CompoundAspect> getCompoundAspectList(ArrayList<Integer> posExamples) {
        return rdfGraph.getCompoundAspectList(posExamples);
    }
}
