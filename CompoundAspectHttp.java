package com.org.shark.graphtoolkits.algorithm.aspectmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


/**
 * It is actually a HashSet.
 */
public class CompoundAspectHttp extends HashSet<SingleAspectHttp> {
    private double score = 0;

    public CompoundAspectHttp() {
        super();
    }

    public CompoundAspectHttp(ArrayList<SingleAspectHttp> obj) {
        super(obj);
    }

    public void setScore(double score) {
        this.score = score;
    }

    public double getScore() {
        return this.score;
    }

//    @Override
//    public int compareTo(Object o) {
//        CompoundAspect other = (CompoundAspect) o;
//        double delta = other.score - this.score;
//        if(Math.abs(delta) < 1e-6) return 0;
//        return delta < 0 ? -1 : 1;
//    }
}
