package com.org.shark.graphtoolkits.simulation;

import java.util.ArrayList;
import java.util.HashSet;

public interface IUserAgent {
    /**
     * Interactive Operations
     */
    public ArrayList<Integer> selectNegExamples(HashSet<Integer> selectedNegExamples, HashSet<Integer> groundTruth, ArrayList<Integer> results, int k);
    public ArrayList<Integer> selectPosExamples(HashSet<Integer> selectedPosExamples, HashSet<Integer> groundTruth, ArrayList<Integer> results, int k);

    /**
     *
     * @param inputExamples
     * @param groundTruth
     * @return the number of iterations to finish the query
     */
    public int interactiveQuery(ArrayList<Integer> inputExamples, HashSet<Integer> groundTruth);
    public int simulation();

    void initialization(IKGExplorationSystem kgExplorationSystem, String queryPath, String ratio);
}
