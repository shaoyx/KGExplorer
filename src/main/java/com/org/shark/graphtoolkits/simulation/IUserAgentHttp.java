package com.org.shark.graphtoolkits.simulation;

import java.util.ArrayList;
import java.util.HashSet;

public interface IUserAgentHttp {
    /**
     * Interactive Operations
     */
    public ArrayList<String> selectNegExamples(HashSet<String> selectedNegExamples, HashSet<String> groundTruth, ArrayList<String> results, int k);
    public ArrayList<String> selectPosExamples(HashSet<String> selectedPosExamples, HashSet<String> groundTruth, ArrayList<String> results, int k);

    /**
     *
     * @param inputExamples
     * @param groundTruth
     * @return the number of iterations to finish the query
     */
    public int interactiveQuery(ArrayList<String> inputExamples, HashSet<String> groundTruth);
    public int simulation();

    void initialization(IKGExplorationSystemHttp kgExplorationSystem, String queryPath, String ratio);
}
