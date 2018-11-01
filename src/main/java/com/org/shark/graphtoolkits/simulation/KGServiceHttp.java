package com.org.shark.graphtoolkits.simulation;

import com.org.shark.graphtoolkits.algorithm.aspectmodel.CompoundAspectHttp;
import com.org.shark.graphtoolkits.algorithm.aspectmodel.SingleAspectHttp;
import com.org.shark.graphtoolkits.graph.RDFGraphWithAspectHttp;

import java.util.ArrayList;
import java.util.HashSet;

public class KGServiceHttp implements IKGServiceHttp {

    RDFGraphWithAspectHttp rdfGraph = null;

    @Override
    public void initialization(String kgPath) {
        rdfGraph = new RDFGraphWithAspectHttp();
        rdfGraph.regHttpGraph(kgPath);
    }

    @Override
    public RDFGraphWithAspectHttp getKG() {
        return this.rdfGraph;
    }

    @Override
    public ArrayList<String> query(CompoundAspectHttp currentState) {
        if(currentState == null)
            return new ArrayList<String>();
        return rdfGraph.query(currentState);
    }

    @Override
    public HashSet<CompoundAspectHttp> computeMaximalAspect(HashSet<String> totPosExample, ArrayList<String> negExamples) {
        return rdfGraph.computeMaximalAspect(totPosExample, negExamples);
    }

    @Override
    public HashSet<SingleAspectHttp> getAspectSet(ArrayList<String> posExamples) {
        return rdfGraph.getAspectSet(posExamples);
    }

    @Override
    public ArrayList<CompoundAspectHttp> getCompoundAspectList(ArrayList<String> posExamples) {
        return rdfGraph.getCompoundAspectList(posExamples);
    }
}
