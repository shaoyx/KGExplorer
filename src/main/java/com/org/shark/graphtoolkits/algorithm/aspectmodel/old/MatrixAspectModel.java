package com.org.shark.graphtoolkits.algorithm.aspectmodel.old;

import com.org.shark.graphtoolkits.graph.old.RDFGraphWithAspectMatrix;
import org.la4j.Vector;
import org.la4j.iterator.VectorIterator;
import org.la4j.vector.SparseVector;

import java.util.ArrayList;

public class MatrixAspectModel extends AspectModel {
    private RDFGraphWithAspectMatrix m;

    public MatrixAspectModel(RDFGraphWithAspectMatrix g) {
        super(g);
        this.m = g;
    }

    @Override
    public AspectGraph query(ArrayList<String> entitySet) {
        //0. pre-processing
        ArrayList<Integer> es = g.getEntityIds(entitySet);

        /* check the validation of query */
        for(Integer eid : es) {
            if(eid == null) {
                return null;
            }
        }

        /* TODO: use the matrix algebra logic */
        //1. compute common aspects
        int freqTh = es.size();
        SparseVector commonAspects = m.sumEntityAspectsVectors(es); //entity2aspect
        ArrayList<Integer> commonAspectsIdxList = new ArrayList<Integer>();
        VectorIterator iter = commonAspects.nonZeroIterator();
        while(iter.hasNext()) {
            double val = iter.next();
            int index = iter.index();
            if(val == freqTh) {
                commonAspectsIdxList.add(index);
            }
        }

        //2. compute aspect related entities
        SparseVector candEntitiesVector = m.sumAspectEntitiesVectors(commonAspectsIdxList); //aspect2entity

        //3. find the aspect set to construct the aspect graph
        iter = candEntitiesVector.nonZeroIterator();
        AspectGraph aGraph = new AspectGraph();

        while(iter.hasNext()) {
            iter.next();
            int index  = iter.index();
            Vector sv = m.getAspectsVector(index);
            sv = sv.add(commonAspects);
            VectorIterator iter2 = sv.toSparseVector().nonZeroIterator();
            ArrayList<Integer> tmpCompoundAspect = new ArrayList<Integer>();
            while(iter2.hasNext()) {
                double val = iter2.next();
                int acId  = iter2.index();
                if(val > freqTh) {
                    tmpCompoundAspect.add(acId);
                }
            }
            aGraph.addCompoundAspect(tmpCompoundAspect);
        }
        aGraph.buildGraph();

        //4. post-processing
        return aGraph;
    }

    public int queryByExample(ArrayList<String> entities, int partIdx) {
        return 0;
    }

}
