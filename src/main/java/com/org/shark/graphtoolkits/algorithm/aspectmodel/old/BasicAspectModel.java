package com.org.shark.graphtoolkits.algorithm.aspectmodel.old;

import com.org.shark.graphtoolkits.algorithm.aspectmodel.CompoundAspect;
import com.org.shark.graphtoolkits.graph.RDFGraphWithAspect;

import java.util.*;

public class BasicAspectModel extends AspectModel {

    public static Random rand = new Random();

    public BasicAspectModel(RDFGraphWithAspect g) {
        super(g);
    }

    public AspectGraph query(ArrayList<String> entitySet) {
        //0. pre-processing
        ArrayList<Integer> es = g.getEntityIds(entitySet);

        /* check the validation of query */
        for(Integer eid : es) {
            if(eid == null) {
                return null;
            }
        }

        //1. compute common aspects
        ArrayList<Integer> ca = queryCommonAspects(es);

        //2. compute aspect related entities
        HashSet<Integer> candEntities = findCoveredEntity(ca);

        //3. find the aspect set to construct the aspect graph
        AspectGraph aGraph = createAspectGraph(ca, candEntities);

        //4. post-processing
        return aGraph;
    }

    private ArrayList<Integer> queryCommonAspects(ArrayList<Integer> entitySet) {
        HashMap<Integer, Integer> aspectCount = new HashMap<Integer, Integer>();
        for(Integer eid : entitySet) {
            ArrayList<Integer> aspects = g.getAspects(eid);
            for(Integer a : aspects) {
                if(aspectCount.containsKey(a)) {
                    aspectCount.put(a, aspectCount.get(a) + 1);
                }
                else {
                    aspectCount.put(a, 1);
                }
            }
        }
        ArrayList<Integer> hs = new ArrayList<Integer>();
        int size = entitySet.size();
        for(Integer key : aspectCount.keySet()) {
            if (aspectCount.get(key) == size) {
                hs.add(key);
            }
        }
        return hs;
    }

    private HashSet<Integer> findCoveredEntity(ArrayList<Integer> ca) {
        HashSet<Integer> candEntities = new HashSet<Integer>();
        for(Integer aspectId : ca) {
            ArrayList<Integer> tmp = g.getEntityWithAspect(aspectId);
            candEntities.addAll(tmp);
        }
        return candEntities;
    }

    private AspectGraph createAspectGraph(ArrayList<Integer> commonAspects, HashSet<Integer> entitySet) {
        HashSet<Integer> ca = new HashSet<Integer>(commonAspects);
        AspectGraph aGraph = new AspectGraph();
        for(Integer eid : entitySet) {
            ArrayList<Integer> aspects = g.getAspects(eid);
            ArrayList<Integer> tmpCompoundAspect = new ArrayList<Integer>();
            for(Integer aspect : aspects) {
                if(ca.contains(aspect)) {
                    tmpCompoundAspect.add(aspect);
                }
            }
            //TODO: expensive for set intersection
            aGraph.addCompoundAspect(tmpCompoundAspect);
        }
        aGraph.buildGraph();
        return aGraph;
    }

    public int queryByExample(ArrayList<String> entitySet, int partIdx) {
        ArrayList<Integer> examples = new ArrayList<Integer>();
        HashSet<Integer> groundTruth = new HashSet<Integer>();
        //0. pre-processing
        ArrayList<Integer> es = g.getEntityIds(entitySet);
        for(int idx = 0; idx < es.size(); idx++) {
            Integer val = es.get(idx);
            if(val == null) return -1;
            if(idx < partIdx) examples.add(val);
            groundTruth.add(val);
        }

        //1. iterative computing
        int iterNum = 0;
        HashSet<Integer> initialAspectSet = g.getAspectSet(examples);
        ArrayList<CompoundAspect> exampleCompoundAspectList = g.getCompoundAspectList(examples);

        HashSet<CompoundAspect> positiveSet = new HashSet<CompoundAspect>();
        HashSet<CompoundAspect> negativeSet = new HashSet<CompoundAspect>();

        HashSet<CompoundAspect> isVisited = new HashSet<CompoundAspect>();
        HashSet<Integer> partialAns = new HashSet<Integer>();

        LinkedList<CompoundAspect> queue = new LinkedList<CompoundAspect>();
        for(int ele : initialAspectSet) {
            CompoundAspect tmpCa = new CompoundAspect();
            tmpCa.add(ele);
            queue.push(tmpCa);
            isVisited.add(tmpCa);
        }

        System.out.println(String.format("GroundTruth: %s", groundTruth.toString()));
        System.out.println(String.format("initial set: %s", initialAspectSet.toString()));

        /**
         * The following process is actual the BFS process.
         * The main trick of reducing the search space is to
         * compute the maximal aspect of
         * examples and negative ones.
         *
         * TODO: XXX
         *   1) do not consider the union operation now!!!
         *   2) duplication problem
         *
         */
        boolean isSuccuss = false;
        while(!queue.isEmpty() && iterNum < 1000) {
            iterNum ++;
            CompoundAspect ca = queue.pop();

            if(isSubsetOf(ca, negativeSet)) {
                //NOTE: in case that the initial aspect can expand to different combinations.
                for (int ele : initialAspectSet) {
                    CompoundAspect nextCa = new CompoundAspect();
                    nextCa.add(ele);
                    nextCa.addAll(ca);
                    if (isVisited.contains(nextCa)
                            || isSubsetOf(nextCa, negativeSet)
                            || !isSubsetOf(nextCa, exampleCompoundAspectList)) {
                        continue;
                    }
                    queue.add(nextCa);
                    isVisited.add(nextCa);
                }
                continue;
            }



            //query KG
            ArrayList<Integer> res = g.query(ca);
//            HashSet<Integer> resSet = new HashSet<Integer>(res);
            System.out.println(String.format("iterNum %d: compound aspect %s: res size %d.", iterNum, ca.toString(), res.size()));
            //check the consistency between query results and ground truth
            if(res.size() == groundTruth.size()){
                boolean isValid = true;
                for(Integer ele : res) {
                    if(!groundTruth.contains(ele)) {
                        isValid = false;
                        break;
                    }
                }
                if(isValid) {
                    isSuccuss = true;
                    break;
                }
            }
            else if(groundTruth.size() > res.size() && groundTruth.containsAll(res)) {
               partialAns.addAll(res); //union answer
               if(partialAns.equals(groundTruth)) {
                   isSuccuss = true;
                   break;
               }
            }

            //IF not terminated, expand the queue
            ArrayList<Integer> negExample = randomPickKNegative(res, groundTruth, 1);

            if(negExample.size() > 0) {

                //expand ca and maxAspect according to initialAspect.
                HashSet<CompoundAspect> maxAspect = g.computeMaximalAspect(examples, negExample);
                negativeSet.addAll(maxAspect);

                //TODO: Do not need to expand negative aspects, because the true aspects can be reached by other ways.
//                for(int ele : initialAspectSet) {
//                    for(CompoundAspect maxCa : maxAspect) {
//                        if(maxCa.contains(ele)) {
//                            continue;
//                        }
//                        CompoundAspect nextCa = new CompoundAspect();
//                        nextCa.add(ele);
//                        nextCa.addAll(maxCa);
//                        if(isVisited.contains(nextCa)) {
//                            continue;
//                        }
//                        queue.add(nextCa);
//                        isVisited.add(nextCa);
//                    }
//                }

            }

            if(res.size() > examples.size()) {
                for (int ele : initialAspectSet) {
                    CompoundAspect nextCa = new CompoundAspect();
                    nextCa.add(ele);
                    nextCa.addAll(ca);
                    if (isVisited.contains(nextCa)
                            || isSubsetOf(nextCa, negativeSet)
                            || !isSubsetOf(nextCa, exampleCompoundAspectList)) {
                        continue;
                    }
                    queue.add(nextCa);
                    isVisited.add(nextCa);
                }
            }

        }

        return isSuccuss ? iterNum : -1;
    }

    private boolean isSubsetOf(CompoundAspect ca, Collection<CompoundAspect> negativeSet) {
        boolean res = false;
        for(CompoundAspect negAspect : negativeSet) {
            if(negAspect.containsAll(ca)) {
                res = true;
                break;
            }
        }
        return res;
    }

    private ArrayList<Integer> randomPickKNegative(ArrayList<Integer> candidates, HashSet<Integer> groundTruth, int k) {
        for(Integer ele : groundTruth) {
            candidates.remove(ele);
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

}
