package com.org.shark.graphtoolkits.simulation;

import com.org.shark.graphtoolkits.algorithm.aspectmodel.CompoundAspect;
import com.org.shark.graphtoolkits.algorithm.classifier.NaiveBayes;

import java.util.*;

public class PriorKGExplorationSystem extends BaseKGExplorationSystem {

    private ArrayList<CompoundAspect> candidateAspectList;
    private NaiveBayes nb;

    @Override
    public void initialization(IKGService kgService) {
        super.initialization(kgService);
        this.candidateAspectList = new ArrayList<CompoundAspect>();
        this.nb = new NaiveBayes();
    }

    public int getProgress() {
        if(isSuccess) return 1;
        if(this.currentState == null && candidateAspectList.size() == 0) return -1;
        return 0;
    }
    public void clean() {
        super.clean();
        candidateAspectList = new ArrayList<CompoundAspect>();
    }
    /**
     * update scores of aspects with negative examples.
     * @param totPosExample
     * @param negExamples
     */
    @Override
    public void updateStatesWithNegExamples(HashSet<Integer> totPosExample, ArrayList<Integer> negExamples) {
        if(null == negExamples || negExamples.size() == 0) return ;
        HashSet<CompoundAspect> maxAspect = kgService.computeMaximalAspect(totPosExample, negExamples);
        negCompoundAspectSet.addAll(maxAspect);
        //update weights;
        for(CompoundAspect ca: maxAspect) {
            for (Integer aspectId : ca) {
                this.incBasicAspectWeight(aspectId, -1.0); //TODO: fixed score
            }
            nb.addExample(new ArrayList<Integer>(ca), -1);
        }
    }

    /**
     * update scores of aspects with positive examples.
     * @param posExamples
     */
    @Override
    public void updateStatesWithPosExamples(ArrayList<Integer> posExamples) {
        if(null == posExamples || posExamples.size() == 0) return ;

        //1. create basic aspect set
        HashSet<Integer> newBasicAspectSet = kgService.getAspectSet(posExamples);
        //TODO: ensure that the input positive example is only one.
        nb.addExample(new ArrayList<>(newBasicAspectSet), 1);
        for(Integer ele : newBasicAspectSet) {
            CompoundAspect tmpCa = new CompoundAspect();
            tmpCa.add(ele);
            if(basicAspectSet.containsKey(ele) || visitedCompoundAspectSet.contains(tmpCa))
                continue;
            basicAspectSet.put(ele, 0.0);
            candidateAspectList.add(tmpCa);
            visitedCompoundAspectSet.add(tmpCa);
        }
        System.out.println(String.format("initial set: %s", basicAspectSet.toString()));
        //2. create positive compound aspect set
        ArrayList<CompoundAspect> newPositiveCompoundAspectList = kgService.getCompoundAspectList(posExamples);
        posCompoundAspectSet.addAll(newPositiveCompoundAspectList);
        //3. update weights
        for(CompoundAspect ca: newPositiveCompoundAspectList) {
            for (Integer aspectId : ca) {
                this.incBasicAspectWeight(aspectId,1.0); //TODO: fixed score
            }
        }
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
            candidateAspectList.add(nextCa);
            visitedCompoundAspectSet.add(nextCa);
        }
    }

    public CompoundAspect nextValidState() {
        CompoundAspect res = null;

        /**
         * The core is to design the rank function.
         * The rank function should be able to capture the user behavior
         * to model the interests of apsect.
         */
        Collections.sort(candidateAspectList, new Comparator<CompoundAspect>() {
            @Override
            public int compare(CompoundAspect o1, CompoundAspect o2) {
                Double score1 = nb.getNegProb(o1); //computeScore(o1);
                Double score2 = nb.getNegProb(o2); //computeScore(o2);

                //TODO:
                o1.setScore(score1);
                o2.setScore(score2);

                if(score1.isInfinite() && score2.isInfinite()) return 0;
                if(score1.isInfinite()) return -1;
                if(score2.isInfinite()) return 1;

                double delta = score1 - score2;

                if(Math.abs(delta) < 1e-6) return 0;

                return delta > 0 ? 1 : -1;
//                return delta > 0 ? -1 : 1;
            }
        });

        int idx = 0;
        int size = candidateAspectList.size();
//        int cnt = 0;
//        for(CompoundAspect o : candidateAspectList) {
//            cnt ++;
//            if(cnt == 10) break;
//            System.out.println(o.getScore());
//        }
        res = null;
        while(idx < size && res == null) {
            CompoundAspect tmp = candidateAspectList.get(idx);
            idx++;
            if (isSubsetOf(tmp, negCompoundAspectSet)) {
                execStateTransition(tmp);
                continue;
            }
            res = tmp;
        }

        if(idx < candidateAspectList.size()) {
            candidateAspectList = new ArrayList<CompoundAspect>(candidateAspectList.subList(idx, candidateAspectList.size()));
        }
        else {
            candidateAspectList = new ArrayList<CompoundAspect>();
        }
        return res;
    }

    private double computeScore(CompoundAspect ca) {
        double score = 0;
        for(Integer aid : ca) {
            score = score + this.basicAspectSet.getOrDefault(aid, 0.0);
        }
        return score;
    }

}
