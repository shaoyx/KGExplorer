package com.org.shark.graphtoolkits.graph.old;


import com.org.shark.graphtoolkits.algorithm.aspectmodel.SingleAspect;
import com.org.shark.graphtoolkits.graph.Edge;
import com.org.shark.graphtoolkits.graph.RDFGraph;
import com.org.shark.graphtoolkits.graph.RDFGraphWithAspect;
import org.la4j.Vector;
import org.la4j.matrix.sparse.CCSMatrix;
import org.la4j.matrix.sparse.CRSMatrix;
import org.la4j.vector.SparseVector;
import org.la4j.vector.sparse.CompressedVector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class RDFGraphWithAspectMatrix extends RDFGraphWithAspect {

    /* TODO: sparse vector vs. sparse matrix */
    private CRSMatrix a2eCRSMatrix;
    private CCSMatrix a2eCCSMatrix;
    // sparse matrix with row/column operation.

    public RDFGraphWithAspectMatrix () {
        super();
    }

    public void buildIndex() {
        for(Integer eid : this.getVertexSet()) {
            ArrayList<Edge> nbrList = this.getNbrList(eid, Direction.OUT);
            for (Edge edge : nbrList) {
                SingleAspect relAspect = new SingleAspect(edge.getLabel());
                SingleAspect factAspect = new SingleAspect(SingleAspect.SAType.PO, edge.getId(),  edge.getLabel());
                indexAspect(relAspect);
                indexAspect(factAspect);
            }
        }

        int aspectSize = this.aspectSize();
        int entitySize = this.entitySize();
        a2eCRSMatrix = CRSMatrix.zero(aspectSize, entitySize);
        a2eCCSMatrix = CCSMatrix.zero(aspectSize, entitySize);
        /**
         * TODO: The construction of matrix is too slow.
         */
        for(Integer eid : this.getVertexSet()) {
            ArrayList<Edge> nbrList = this.getNbrList(eid, Direction.OUT);
            HashSet<Integer> exist = new HashSet<Integer>();

            for (Edge edge : nbrList) {
                SingleAspect relAspect = new SingleAspect(edge.getLabel());
                SingleAspect factAspect = new SingleAspect(SingleAspect.SAType.PO, edge.getId(),  edge.getLabel());
                int relAspectId = indexAspect(relAspect);
                int factAspectId = indexAspect(factAspect);

                if(!exist.contains(relAspectId)) {
                    a2eCCSMatrix.set(relAspectId, eid, 1);
                    a2eCRSMatrix.set(relAspectId, eid, 1);
                    exist.add(relAspectId);
                }
                if(!exist.contains(factAspectId)) {
                    a2eCCSMatrix.set(factAspectId, eid, 1);
                    a2eCRSMatrix.set(factAspectId, eid, 1);
                    exist.add(factAspectId);
                }
            }
        }
    }

    public SparseVector sumEntityAspectsVectors(ArrayList<Integer> es) {
        Vector sv = CompressedVector.zero(this.aspectSize());
        for(int idx : es) {
            sv = sv.add(a2eCCSMatrix.getColumn(idx));
        }
        return sv.toSparseVector();
    }

    public SparseVector sumAspectEntitiesVectors(ArrayList<Integer> es) {
        Vector sv = CompressedVector.zero(this.entitySize());
        for(int idx : es) {
            sv = sv.add(a2eCRSMatrix.getRow(idx));
        }
        return sv.toSparseVector();
    }

    public SparseVector getAspectsVector(int index) {
        return this.a2eCCSMatrix.getColumn(index).toSparseVector();
    }
}
