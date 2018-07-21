package com.org.shark.graphtoolkits.simulation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

public class UserAgent implements IUserAgent {
    private static final Logger logger = LogManager.getLogger(UserAgent.class.getName());
    private static Random rand = new Random();

    private IKGExplorationSystem kgExplorationSystem;
    private String queryFilePath;
    private double exampleRatio;

    @Override
    public ArrayList<Integer> selectNegExamples(HashSet<Integer> selectedNegExamples, HashSet<Integer> groundTruth, ArrayList<Integer> results, int k) {
        ArrayList<Integer> candidates = new ArrayList<Integer>();
        for(Integer ele : results) {
            if(!groundTruth.contains(ele) && !selectedNegExamples.contains(ele))
                candidates.add(ele);
        }

        if(candidates.size() <= k) {
            return candidates;
        }

        ArrayList<Integer> res = new ArrayList<Integer>();

        for(int i = 0; i < k; i++) {
            int randIdx = rand.nextInt(candidates.size());
            res.add(candidates.get(randIdx));
            candidates.remove(randIdx);
        }
        return res;
    }

    @Override
    public ArrayList<Integer> selectPosExamples(HashSet<Integer> selectedPosExamples, HashSet<Integer> groundTruth, ArrayList<Integer> results, int k) {
        return new ArrayList<Integer>();
    }

    @Override
    public int interactiveQuery(ArrayList<Integer> inputExamples, HashSet<Integer> groundTruth) {
        int iterNum = 0;
        int queryProgress = 0; // 0: ongoing, 1: finish and success, -1: finish but failed.

        HashSet<Integer> posExamples = new HashSet<Integer>();
        HashSet<Integer> totExamples = new HashSet<Integer>();
        HashSet<Integer> negExamples = new HashSet<Integer>();
        ArrayList<Integer> newPosExamples = new ArrayList<Integer>();
        ArrayList<Integer> newNegExamples = new ArrayList<Integer>();

        for(Integer ele : inputExamples) {
            newPosExamples.add(ele);
            posExamples.add(ele);
        }

        totExamples.addAll(groundTruth);

        do{
           iterNum ++;
           ArrayList<Integer> results = kgExplorationSystem.explore(posExamples, totExamples, newPosExamples, newNegExamples, groundTruth);
           System.out.println(String.format("iterNum %d: res size %d.", iterNum, results.size()));
           newPosExamples = null; //selectPosExamples(posExamples, groundTruth, results, 1); //TODO: current we do not select pos examples.
           newNegExamples = selectNegExamples(negExamples, groundTruth, results, 1);
           if(newNegExamples.size() == 0) {
               logger.info("No Negative example is selected!");
           }
           negExamples.addAll(newNegExamples);
           totExamples.addAll(newNegExamples);

           queryProgress = kgExplorationSystem.getProgress();
        }while(queryProgress == 0 && iterNum <= 1000);

        kgExplorationSystem.clean();

        return iterNum > 1000 ? (iterNum * -1) : (iterNum * queryProgress);
    }

    @Override
    public int simulation() {
        int queryId = 0;
        int invalid = 0;
        try {
            FileInputStream fin = new FileInputStream(this.queryFilePath);
            BufferedReader fbr = new BufferedReader(new InputStreamReader(fin));
            String line;
            int rawId = -1;
            while ((line = fbr.readLine()) != null) {
                if(line.startsWith("#")) {
                    rawId = Integer.parseInt(line.substring(1, line.length()));
                    continue;
                }
                queryId ++;

                ArrayList<String> entitySet = parseLine(line);
                ArrayList<Integer> groundTruth = kgExplorationSystem.getEntityIds(entitySet);

                int partIdx = (int)((entitySet.size()+1) * this.exampleRatio);
                ArrayList<Integer> examples = new ArrayList<Integer>();

                // Check input validation and construct example lists
                boolean isInputValid = true;
                for(int idx = 0; idx < groundTruth.size(); idx++) {
                    Integer val = groundTruth.get(idx);
                    if(val == null) isInputValid = false;
                    if(idx < partIdx)
                        examples.add(val);
                }

                int iterNum = Integer.MIN_VALUE;
                if(isInputValid) {
                    System.out.println(String.format("Examples: %s", examples.toString()));
                    System.out.println(String.format("GroundTruth: %s", groundTruth.toString()));
                    iterNum = interactiveQuery(examples, new HashSet<Integer>(groundTruth));
                }

                if(iterNum < 0) {
                    logger.info(String.format("Invalid query %d costs %d iteration", rawId, iterNum));
                    invalid ++;
                }
                else {
                    logger.info(String.format("query %d costs %d iterations", rawId, iterNum));
                }
            }
            fbr.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
        return queryId - invalid;
    }

    @Override
    public void initialization(IKGExplorationSystem kgExplorationSystem, String queryFilePath, String ratio) {
        this.kgExplorationSystem = kgExplorationSystem;
        this.queryFilePath = queryFilePath;
        this.exampleRatio = Double.parseDouble(ratio);
    }

    private ArrayList<String> parseLine(String line) {
        String [] raw = line.split("\t");
        ArrayList<String> res = new ArrayList<String> ();
        for(String entity : raw) {
            res.add("<"+entity+">");
        }
        return res;
    }
}
