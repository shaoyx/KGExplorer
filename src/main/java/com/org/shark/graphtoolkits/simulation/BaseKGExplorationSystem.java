package com.org.shark.graphtoolkits.simulation;

import com.org.shark.graphtoolkits.algorithm.aspectmodel.CompoundAspect;
import com.org.shark.graphtoolkits.graph.RDFGraphWithAspect;

import java.util.*;

public abstract class BaseKGExplorationSystem implements IKGExplorationSystem {
    protected IKGService kgService;

    /**
     * The following fields record the states of exploration system.
     */
    protected CompoundAspect currentState = null;
    protected ArrayList<Integer> currentRes = null;
    protected boolean isSuccess = false;

    protected HashMap<Integer, Double> basicAspectSet = new HashMap<Integer, Double>();
    protected HashSet<CompoundAspect> uselessAspectSet = new HashSet<CompoundAspect>();
    protected HashSet<CompoundAspect> posCompoundAspectSet = new HashSet<CompoundAspect>();
    protected HashSet<CompoundAspect> negCompoundAspectSet = new HashSet<CompoundAspect>();

    private Queue<CompoundAspect> candidateCompoundAspectSpace = null;
    protected HashSet<CompoundAspect> visitedCompoundAspectSet = new HashSet<CompoundAspect>();

    protected HashSet<Integer> partialAns = new HashSet<Integer>();

    @Override
    public void initialization(IKGService kgService) {
        this.kgService = kgService;
        candidateCompoundAspectSpace = new LinkedList<CompoundAspect>();
    }

    @Override
    public ArrayList<Integer> getEntityIds(ArrayList<String> entityList) {
        return kgService.getEntityIds(entityList);
    }

    @Override
    public ArrayList<Integer> explore(HashSet<Integer> totPosExample, HashSet<Integer> totExample, ArrayList<Integer> newPosExamples, ArrayList<Integer> newNegExamples, HashSet<Integer> groundTruth) {
        //1. update states with input examples.
        updateStatesWithNegExamples(totPosExample, newNegExamples);
        updateStatesWithPosExamples(newPosExamples);
//        if(this.currentRes.size() > totPosExample.size()) //TODO: do we need this condition!!!
        execStateTransition(this.currentState);

        //2. find next valid state and valid the results
        do {
            this.currentState = nextValidState();
            this.currentRes = kgService.query(this.currentState);
            this.isSuccess = checkResults(this.currentRes, groundTruth); //TODO: this should be off for interative mode.
        }while(this.currentRes != null && !this.isSuccess && !hasInformationGain(this.currentRes, totExample));

        if (this.currentState != null) {
            System.out.println(String.format("!!! compound aspect %s: res size %d.", this.currentState.toString(), this.currentRes.size()));
        }

        return this.currentRes;
    }

    /**
     *
     * If the returned results are a subset of identified positive examples,
     * then their is no information gains.
     *
     * @param currentRes
     * @param totExamples
     * @return
     */
    private boolean hasInformationGain(ArrayList<Integer> currentRes, HashSet<Integer> totExamples) {
        if(currentRes.size() > totExamples.size())
            return true;
        for(Integer rId : currentRes) {
            if(!totExamples.contains(rId))
                return true;
        }
        if(this.currentState != null) {
            this.uselessAspectSet.add(this.currentState);
        }
        return false;
    }

    @Override
    public int getProgress() {
        if(isSuccess) return 1;
        if(this.currentState == null && candidateCompoundAspectSpace.size() == 0) return -1;
        return 0;
    }

    @Override
    public void clean() {
        currentState = null;
        currentRes = null;
        isSuccess = false;

        basicAspectSet = new HashMap<Integer, Double>();
        posCompoundAspectSet = new HashSet<CompoundAspect>();
        negCompoundAspectSet = new HashSet<CompoundAspect>();

        candidateCompoundAspectSpace = new LinkedList<CompoundAspect>();
        uselessAspectSet = new HashSet<>();
        visitedCompoundAspectSet = new HashSet<CompoundAspect>();

        partialAns = new HashSet<Integer>();
    }

    @Override
    public Integer getEntityId(String entityName) {
        ArrayList<String> entityList = new ArrayList<String>();
        entityList.add(entityName);
        ArrayList<Integer> idList = kgService.getEntityIds(entityList);
        return idList.get(0);
    }

    @Override
    public CompoundAspect getState() {
        return this.currentState;
    }

    @Override
    public String getExplanableCompoundAspect(CompoundAspect ca) {
        RDFGraphWithAspect graph = kgService.getKG();
        StringBuilder sb = new StringBuilder();
        for(Integer aspectId : ca) {
            sb.append(graph.aspectStr(aspectId)+"\n");
        }
        return sb.toString();
    }

    @Override
    public String getEntityName(Integer eid) {
        RDFGraphWithAspect graph = kgService.getKG();
        return graph.entityName(eid);
    }

    public Set<Integer> getBasicAspectSet() {
        return this.basicAspectSet.keySet();
    }

    public void incBasicAspectWeight(int aid, double w) {
        double newVal = this.basicAspectSet.get(aid);
        this.basicAspectSet.put(aid, newVal + w);
    }

    public void updateStatesWithNegExamples(HashSet<Integer> totPosExample, ArrayList<Integer> negExamples) {
        if(null == negExamples || negExamples.size() == 0) return ;
        HashSet<CompoundAspect> maxAspect = kgService.computeMaximalAspect(totPosExample, negExamples);
        negCompoundAspectSet.addAll(maxAspect);
    }

    public void updateStatesWithPosExamples(ArrayList<Integer> posExamples) {
        if(null == posExamples || posExamples.size() == 0) return ;

        //1. create basic aspect set
        HashSet<Integer> newBasicAspectSet = kgService.getAspectSet(posExamples);
        for(Integer ele : newBasicAspectSet) {
            CompoundAspect tmpCa = new CompoundAspect();
            tmpCa.add(ele);
            if(basicAspectSet.containsKey(ele) || visitedCompoundAspectSet.contains(tmpCa))
                continue;
            basicAspectSet.put(ele, 0.0);
            candidateCompoundAspectSpace.add(tmpCa);
            visitedCompoundAspectSet.add(tmpCa);
        }
        System.out.println(String.format("initial set: %s", basicAspectSet.toString()));
        //2. create positive compound aspect set
        ArrayList<CompoundAspect> newPositiveCompoundAspectList = kgService.getCompoundAspectList(posExamples);
        posCompoundAspectSet.addAll(newPositiveCompoundAspectList);
    }

    public void execStateTransition(CompoundAspect state) {
        if(state == null) return;
        for (int ele : this.getBasicAspectSet()) {
            CompoundAspect nextCa = new CompoundAspect();
            nextCa.add(ele);
            nextCa.addAll(state);
            if (visitedCompoundAspectSet.contains(nextCa)
                    || isSubsetOf(nextCa, negCompoundAspectSet)
                    || !isSubsetOf(nextCa, posCompoundAspectSet)
                    || containsUselessAspectSet(nextCa)) {
                continue;
            }
            candidateCompoundAspectSpace.add(nextCa);
            visitedCompoundAspectSet.add(nextCa);
        }
    }

    protected boolean containsUselessAspectSet(CompoundAspect nextCa) {
        boolean res = false;
        for(CompoundAspect tmp : this.uselessAspectSet) {
            if(nextCa.containsAll(tmp)) {
                res = true;
                break;
            }
        }
        return res;
    }

    public CompoundAspect nextValidState() {
        CompoundAspect res = null;

        while(!this.candidateCompoundAspectSpace.isEmpty()) {
            CompoundAspect tmp = this.candidateCompoundAspectSpace.poll();
            if(isSubsetOf(tmp, negCompoundAspectSet) || containsUselessAspectSet(tmp)) {
                execStateTransition(tmp);
                continue;
            }
            res = tmp;
            break;
        }
        return res;
    }

    public boolean checkResults(ArrayList<Integer> res, HashSet<Integer> groundTruth) {
        if(groundTruth == null) return false;
        if(res.size() == groundTruth.size()){
            boolean isValid = true;
            for(Integer ele : res) {
                if(!groundTruth.contains(ele)) {
                    isValid = false;
                    break;
                }
            }
            return isValid;
        }
        else if(groundTruth.size() > res.size() && groundTruth.containsAll(res)) {
            partialAns.addAll(res); //union answer
            if(partialAns.equals(groundTruth)) {
                return true;
            }
        }
        return false;
    }

    public boolean isSubsetOf(CompoundAspect ca, Collection<CompoundAspect> negativeSet) {
        boolean res = false;
        for(CompoundAspect negAspect : negativeSet) {
            if(negAspect.containsAll(ca)) {
                res = true;
                break;
            }
        }
        return res;
    }

}
