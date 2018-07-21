package com.org.shark.graphtoolkits.simulation;

import com.org.shark.graphtoolkits.algorithm.aspectmodel.CompoundAspect;
import com.org.shark.graphtoolkits.graph.RDFGraphWithAspect;

import java.util.ArrayList;
import java.util.HashSet;

public interface IKGService {

    public void initialization(String kgPath);
    public ArrayList<Integer> getEntityIds(ArrayList<String> entityList);

    public RDFGraphWithAspect getKG();

    public ArrayList<Integer> query(CompoundAspect currentState);

    public HashSet<CompoundAspect> computeMaximalAspect(HashSet<Integer> totPosExample, ArrayList<Integer> negExamples);

    public HashSet<Integer> getAspectSet(ArrayList<Integer> posExamples);

    public ArrayList<CompoundAspect> getCompoundAspectList(ArrayList<Integer> posExamples);
}
