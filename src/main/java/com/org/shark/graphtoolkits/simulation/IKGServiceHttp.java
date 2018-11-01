package com.org.shark.graphtoolkits.simulation;

import com.org.shark.graphtoolkits.algorithm.aspectmodel.CompoundAspectHttp;
import com.org.shark.graphtoolkits.algorithm.aspectmodel.SingleAspectHttp;
import com.org.shark.graphtoolkits.graph.RDFGraphWithAspectHttp;

import java.util.ArrayList;
import java.util.HashSet;

public interface IKGServiceHttp {

    public void initialization(String kgPath);

    /* * id-related funcs only used when non-Http mode is on
    */ 

    public RDFGraphWithAspectHttp getKG();

    public ArrayList<String> query(CompoundAspectHttp currentState);

    public HashSet<CompoundAspectHttp> computeMaximalAspect(HashSet<String> totPosExample, ArrayList<String> negExamples);

    public HashSet<SingleAspectHttp> getAspectSet(ArrayList<String> posExamples);

    public ArrayList<CompoundAspectHttp> getCompoundAspectList(ArrayList<String> posExamples);
}
