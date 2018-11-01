package com.org.shark.graphtoolkits.simulation;

import com.org.shark.graphtoolkits.algorithm.aspectmodel.CompoundAspectHttp;

import java.util.ArrayList;
import java.util.HashSet;

public interface IKGExplorationSystemHttp {
    public void initialization(IKGServiceHttp kgService);

    public ArrayList<String> explore(HashSet<String> totPosExample, HashSet<String> totExample, ArrayList<String> newPosExamples, ArrayList<String> newNegExamples, HashSet<String> groundTruth);

    public int getProgress();

    public void clean();

    public CompoundAspectHttp getState();

    public String getExplanableCompoundAspect(CompoundAspectHttp ca);
}
