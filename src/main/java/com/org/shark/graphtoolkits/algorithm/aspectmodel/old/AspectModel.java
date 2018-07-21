package com.org.shark.graphtoolkits.algorithm.aspectmodel.old;

import com.org.shark.graphtoolkits.graph.RDFGraphWithAspect;

import java.util.ArrayList;

public abstract class AspectModel {
    protected RDFGraphWithAspect g;

    public AspectModel(RDFGraphWithAspect g) {
        this.g = g;
    }

    public abstract AspectGraph query(ArrayList<String> entitySet);

    public abstract int queryByExample(ArrayList<String> entity, int partIdx);
}
