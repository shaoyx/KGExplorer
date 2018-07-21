package com.org.shark.graphtoolkits.algorithm.classifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class NaiveBayes {

    private int dataCnt;
    private HashMap<Integer, ArrayList<ArrayList<Integer>>> data;
    private HashMap<Integer, Integer> priorAttrCnt;
    private HashMap<Integer, Integer> negCondCnt;
    private HashMap<Integer, Integer> posCondCnt;

    public NaiveBayes() {
        this.data = new HashMap<>();
        priorAttrCnt = new HashMap<>();
        negCondCnt = new HashMap<>();
        posCondCnt = new HashMap<>();
        dataCnt = 0;
    }

    public void addExample(ArrayList<Integer> example, int label) {
        if(!data.containsKey(label)) {
            data.put(label, new ArrayList<>());
        }
        data.get(label).add(example);
        processExample(example, label);
        dataCnt++;
    }

    private void processExample(ArrayList<Integer> example, int label) {

        /* update prior count */
        for(int aid : example) {
            if(!priorAttrCnt.containsKey(aid)) {
                priorAttrCnt.put(aid, 0);
            }
            priorAttrCnt.put(aid, priorAttrCnt.get(aid) + 1);
        }

        if(label == -1) {
            /* update positive cnt */
            for(int aid : example) {
                if (!negCondCnt.containsKey(aid)) {
                    negCondCnt.put(aid, 0);
                }
                negCondCnt.put(aid, negCondCnt.get(aid) + 1);
            }
        }
        else {
           for(int aid: example) {
               if(!posCondCnt.containsKey(aid)) {
                   posCondCnt.put(aid, 0);
               }
               posCondCnt.put(aid, posCondCnt.get(aid) + 1);
           }
        }
    }

    public Double getNegProb(HashSet<Integer> expAttrSet) {
        double res = Double.NEGATIVE_INFINITY;
        if(!data.containsKey(-1)) return res;
        double negExpCnt = data.get(-1).size();
        double totCnt = dataCnt;
        double tmp = 0.0;
        for(int aid : priorAttrCnt.keySet()) {
            /* prob(label) * prob(aid|label)/prob(aid) = prob(label|aid) */
            if(priorAttrCnt.get(aid) == dataCnt) continue; //TODO: this aspect(aid) has no ability to distinct examples.

            double cnt = expAttrSet.contains(aid) ? negCondCnt.getOrDefault(aid, 0) : (negExpCnt - negCondCnt.getOrDefault(aid, 0));
            double priorCnt = expAttrSet.contains(aid) ? priorAttrCnt.get(aid) : (totCnt - priorAttrCnt.get(aid));
            if(cnt == 0) {
                tmp = Double.NEGATIVE_INFINITY;
                break;
            }
            tmp +=  Math.log(cnt) - Math.log(priorCnt);
//            res = res * (negExpCnt / totCnt) * (cnt / negExpCnt)
//                    / (priorCnt / totCnt);
        }
        res = tmp;
        return res;
    }
}
