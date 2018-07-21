package com.org.shark.graphtoolkits.simulation;

import com.org.shark.graphtoolkits.algorithm.aspectmodel.CompoundAspect;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;

public class InteractiveUserAgent implements IUserAgent {
    private static final Logger logger = LogManager.getLogger(UserAgent.class.getName());

    private IKGExplorationSystem kgExplorationSystem;

    private ArrayList<Integer> currentResults;
    private CompoundAspect currentState;

    private HashSet<Integer> posExamples = new HashSet<Integer>();
    private HashSet<Integer> negExamples = new HashSet<Integer>();
    private HashSet<Integer> totExamples = new HashSet<>();
    private ArrayList<Integer> newPosExamples = new ArrayList<Integer>();
    private ArrayList<Integer> newNegExamples = new ArrayList<Integer>();
    private int iterNum = 0;

    @Override
    public ArrayList<Integer> selectNegExamples(HashSet<Integer> selectedNegExamples, HashSet<Integer> groundTruth, ArrayList<Integer> results, int k) {
        return null;
    }

    @Override
    public ArrayList<Integer> selectPosExamples(HashSet<Integer> selectedPosExamples, HashSet<Integer> groundTruth, ArrayList<Integer> results, int k) {
        return null;
    }

    @Override
    public int interactiveQuery(ArrayList<Integer> inputExamples, HashSet<Integer> groundTruth) {
        return 0;
    }

    @Override
    public int simulation() {
        /**
         * 1. input initial example <xxxx>, <xxx>
         * 2. query and list results
         * 3. select example and query again.
         * Input samples:
         * q <http://dbpedia.org/resource/On_the_Road>
         * q <http://dbpedia.org/resource/Door_Wide_Open>
         * exec
         * exec +1,+2,-1 # the first one is positive, ...
         * exec +1,+2,-1
         * end
         * quit
         */
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in ));
        String line = "";
        int state = 0; //1:input example; 2: interactive process; 3: end; 4: quit
        try{
            while(true) {
                line=br.readLine();
                String [] recs = line.split(" ");
                state = updateExecState(recs[0], state);
                logger.info(String.format("CMD: %d", state));
                if(state == 1)
                    addExample(recs.length > 1 ? recs[1] : null);
                if(state == 2)
                    execQuery(recs.length > 1 ? recs[1] : null);
                if(state == 3)
                    cleanQuery();
                if(state == 4)
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }

    private void cleanQuery() {
        this.newPosExamples.clear();
        this.newNegExamples.clear();
        this.posExamples.clear();
        this.negExamples.clear();
        this.totExamples.clear();
        kgExplorationSystem.clean();
        iterNum = 0;
    }

    private void execQuery(String parameters) {
        iterNum++;
        if(parameters != null)
           updateExamples(parameters);
        this.currentResults = kgExplorationSystem.explore(posExamples, totExamples, newPosExamples, newNegExamples, null);
        this.currentState = kgExplorationSystem.getState();

        this.newPosExamples.clear();
        this.newNegExamples.clear();

        listResults();
    }

    private void listResults() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Iteration %d:\n", iterNum));
        if(this.currentState != null) {
            sb.append("CURRENT STATE:\n");
            sb.append(kgExplorationSystem.getExplanableCompoundAspect(this.currentState));
        }
        else {
            sb.append("EMPTY STATE!!!\n");
        }

        if(this.currentResults != null) {
            sb.append("RESULT LIST:\n");
            int idx = 0;
            for(Integer eid : this.currentResults) {
                if(idx > 50)
                    break;
                sb.append(String.format("[%d]: %s\n", idx, kgExplorationSystem.getEntityName(eid)));
                idx ++;
            }
        }
        else {
            sb.append("EMPTY RESULTS!!!\n");
        }

        logger.info(String.format("%s", sb.toString()));
    }

    private void updateExamples(String parameters) {
            String [] inExamples = parameters.split(",");
            for(String eg : inExamples) {
                int idx = Math.abs(Integer.parseInt(eg));
                if(eg.startsWith("-")) {
                    this.newNegExamples.add(this.currentResults.get(idx));
                    this.negExamples.add(this.currentResults.get(idx));
                    this.totExamples.add(this.currentResults.get(idx));
                }
                else {
                    this.newPosExamples.add(this.currentResults.get(idx));
                    this.posExamples.add(this.currentResults.get(idx));
                    this.totExamples.add(this.currentResults.get(idx));
                }
            }
    }

    private void addExample(String example) {
        if(example == null) return ;
        Integer entityId = kgExplorationSystem.getEntityId(example);
        if(entityId == null) {
            logger.info(String.format("Invalid Example: %s", example));
        }
        else {
            this.posExamples.add(entityId);
            this.newPosExamples.add(entityId);
            this.totExamples.add(entityId);
        }
    }

    @Override
    public void initialization(IKGExplorationSystem kgExplorationSystem, String queryPath, String ratio) {
        this.kgExplorationSystem = kgExplorationSystem;
    }

    private int updateExecState(String input, int currentState) {
        if(input.equalsIgnoreCase("quit")) return 4;
        if(input.equalsIgnoreCase("q")) return 1;
        if(input.equalsIgnoreCase("exec")) return 2;
        if(input.equalsIgnoreCase("end")) return 3;
        return currentState;
    }
}
