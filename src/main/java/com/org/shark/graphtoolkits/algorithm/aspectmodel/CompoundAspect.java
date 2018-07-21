package com.org.shark.graphtoolkits.algorithm.aspectmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


/**
 * It is actually a HashSet.
 */
public class CompoundAspect extends HashSet<Integer> {
    private double score = 0;

    public CompoundAspect() {
        super();
    }

    public CompoundAspect(ArrayList<Integer> obj) {
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
