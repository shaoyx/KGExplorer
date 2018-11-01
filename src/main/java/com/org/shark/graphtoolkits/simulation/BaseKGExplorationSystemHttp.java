package com.org.shark.graphtoolkits.simulation;

import com.org.shark.graphtoolkits.algorithm.aspectmodel.CompoundAspectHttp;
import com.org.shark.graphtoolkits.algorithm.aspectmodel.SingleAspectHttp;
import com.org.shark.graphtoolkits.graph.RDFGraphWithAspectHttp;

import java.util.*;

public abstract class BaseKGExplorationSystemHttp implements IKGExplorationSystemHttp {
    protected IKGServiceHttp kgService;

    /**
     * The following fields record the states of exploration system.
     */
    protected CompoundAspectHttp currentState = null;
    protected ArrayList<String> currentRes = null;
    protected boolean isSuccess = false;

    protected HashMap<SingleAspectHttp, Double> basicAspectSet = new HashMap<SingleAspectHttp, Double>();
    protected HashSet<CompoundAspectHttp> uselessAspectSet = new HashSet<CompoundAspectHttp>();
    protected HashSet<CompoundAspectHttp> posCompoundAspectSet = new HashSet<CompoundAspectHttp>();
    protected HashSet<CompoundAspectHttp> negCompoundAspectSet = new HashSet<CompoundAspectHttp>();

    private Queue<CompoundAspectHttp> candidateCompoundAspectSpace = null;
    protected HashSet<CompoundAspectHttp> visitedCompoundAspectSet = new HashSet<CompoundAspectHttp>();

    protected HashSet<String> partialAns = new HashSet<String>();

    @Override
    public void initialization(IKGServiceHttp kgService) {
        this.kgService = kgService;
        candidateCompoundAspectSpace = new LinkedList<CompoundAspectHttp>();
    }

/*
    @Override
    public ArrayList<Integer> getEntityIds(ArrayList<String> entityList) {
        return kgService.getEntityIds(entityList);
    }
*/
    @Override
    public ArrayList<String> explore(HashSet<String> totPosExample, HashSet<String> totExample, ArrayList<String> newPosExamples, ArrayList<String> newNegExamples, HashSet<String> groundTruth) {
        //1. update states with input examples.
        updateStatesWithNegExamples(totPosExample, newNegExamples);
        updateStatesWithPosExamples(newPosExamples);
//        if(this.currentRes.size() > totPosExample.size()) //TODO: do we need this condition!!!
        execStateTransition(this.currentState);

        //2. find next valid state and valid the results
        //TODO: may cause many loops so that it does not finish.
        int interCnt = 0;
        do {
            this.currentState = nextValidState();
            this.currentRes = kgService.query(this.currentState);
            this.isSuccess = checkResults(this.currentRes, groundTruth); //TODO: this should be off for interative mode.
            interCnt++;
            if(interCnt % 100 == 0) {
                System.out.println("InterCnt = "+interCnt);
            }
        }while(this.currentRes != null && !this.isSuccess && !hasInformationGain(this.currentRes, totExample));

        if (this.currentState != null) {
            System.out.println(String.format("!!! compound aspect %s: res size %d, interCnt %d.", this.currentState.toString(), this.currentRes.size(), interCnt));
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
    private boolean hasInformationGain(ArrayList<String> currentRes, HashSet<String> totExamples) {
        if(currentRes.size() > totExamples.size())
            return true;
        for(String rent : currentRes) {
            if(!totExamples.contains(rent))
                return true;
        }
        if(this.currentState != null) {
            this.uselessAspectSet.add(this.currentState); //TODO: we should do some preprocessing to construct this useless information.
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

        basicAspectSet = new HashMap<SingleAspectHttp, Double>();
        posCompoundAspectSet = new HashSet<CompoundAspectHttp>();
        negCompoundAspectSet = new HashSet<CompoundAspectHttp>();

        candidateCompoundAspectSpace = new LinkedList<CompoundAspectHttp>();
        uselessAspectSet = new HashSet<>();
        visitedCompoundAspectSet = new HashSet<CompoundAspectHttp>();

        partialAns = new HashSet<String>();
    }

    /*
    @Override
    public Integer getEntityId(String entityName) {
        ArrayList<String> entityList = new ArrayList<String>();
        entityList.add(entityName);
        ArrayList<Integer> idList = kgService.getEntityIds(entityList);
        return idList.get(0);
    }
    */

    @Override
    public CompoundAspectHttp getState() {
        return this.currentState;
    }

    @Override
    public String getExplanableCompoundAspect(CompoundAspectHttp ca) {
        RDFGraphWithAspectHttp graph = kgService.getKG();
        StringBuilder sb = new StringBuilder();
        for(SingleAspectHttp asp : ca) {
            sb.append(graph.aspectStr(asp)+"\n");
        }
        return sb.toString();
    }

    /*
    @Override
    public String getEntityName(Integer eid) {
        RDFGraphWithAspect graph = kgService.getKG();
        return graph.entityName(eid);
    }
    */
    public Set<SingleAspectHttp> getBasicAspectSet() {
        return this.basicAspectSet.keySet();
    }

    public void incBasicAspectWeight(SingleAspectHttp asp, double w) {
        double newVal = this.basicAspectSet.get(asp);
        this.basicAspectSet.put(asp, newVal + w);
    }

    public void updateStatesWithNegExamples(HashSet<String> totPosExample, ArrayList<String> negExamples) {
        if(null == negExamples || negExamples.size() == 0) return ;
        HashSet<CompoundAspectHttp> maxAspect = kgService.computeMaximalAspect(totPosExample, negExamples);
        negCompoundAspectSet.addAll(maxAspect);
    }

    public void updateStatesWithPosExamples(ArrayList<String> posExamples) {
        if(null == posExamples || posExamples.size() == 0) return ;

        //1. create basic aspect set
        HashSet<SingleAspectHttp> newBasicAspectSet = kgService.getAspectSet(posExamples);
        for(SingleAspectHttp ele : newBasicAspectSet) {
            CompoundAspectHttp tmpCa = new CompoundAspectHttp();
            tmpCa.add(ele);
            if(basicAspectSet.containsKey(ele) || visitedCompoundAspectSet.contains(tmpCa))
                continue;
            basicAspectSet.put(ele, 0.0);
            candidateCompoundAspectSpace.add(tmpCa);
            visitedCompoundAspectSet.add(tmpCa);
        }
        System.out.println(String.format("initial set: %s", basicAspectSet.toString()));
        //2. create positive compound aspect set
        ArrayList<CompoundAspectHttp> newPositiveCompoundAspectList = kgService.getCompoundAspectList(posExamples);
        posCompoundAspectSet.addAll(newPositiveCompoundAspectList);
    }

    public void execStateTransition(CompoundAspectHttp state) {
        if(state == null) return;
        for (SingleAspectHttp ele : this.getBasicAspectSet()) {
            CompoundAspectHttp nextCa = new CompoundAspectHttp();
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

    protected boolean containsUselessAspectSet(CompoundAspectHttp nextCa) {
        boolean res = false;
        for(CompoundAspectHttp tmp : this.uselessAspectSet) {
            if(nextCa.containsAll(tmp)) {
                res = true;
                break;
            }
        }
        return res;
    }

    public CompoundAspectHttp nextValidState() {
        CompoundAspectHttp res = null;

        while(!this.candidateCompoundAspectSpace.isEmpty()) {
            CompoundAspectHttp tmp = this.candidateCompoundAspectSpace.poll();
            if(isSubsetOf(tmp, negCompoundAspectSet) || containsUselessAspectSet(tmp)) {
                execStateTransition(tmp);
                continue;
            }
            res = tmp;
            break;
        }
        return res;
    }

    public boolean checkResults(ArrayList<String> res, HashSet<String> groundTruth) {
        if(groundTruth == null) return false;
        if(res.size() == groundTruth.size()){
            boolean isValid = true;
            for(String ele : res) {
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

    public boolean isSubsetOf(CompoundAspectHttp ca, Collection<CompoundAspectHttp> negativeSet) {
        boolean res = false;
        for(CompoundAspectHttp negAspect : negativeSet) {
            if(negAspect.containsAll(ca)) {
                res = true;
                break;
            }
        }
        return res;
    }

}
