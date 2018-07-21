package com.org.shark.graphtoolkits.simulation;

import com.org.shark.graphtoolkits.algorithm.aspectmodel.CompoundAspect;

import java.util.ArrayList;
import java.util.HashSet;

public interface IKGExplorationSystem {
    public void initialization(IKGService kgService);
    public ArrayList<Integer> getEntityIds(ArrayList<String> entityList);

    public ArrayList<Integer> explore(HashSet<Integer> totPosExample, HashSet<Integer> totExample, ArrayList<Integer> newPosExamples, ArrayList<Integer> newNegExamples, HashSet<Integer> groundTruth);

    public int getProgress();

    public void clean();

    public Integer getEntityId(String entityName);

    public CompoundAspect getState();

    public String getExplanableCompoundAspect(CompoundAspect ca);
    public String getEntityName(Integer eid);
}
