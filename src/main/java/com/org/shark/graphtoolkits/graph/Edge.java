package com.org.shark.graphtoolkits.graph;

/**
 * Created by yxshao on 10/5/15.
 */
public class Edge {
    private int id;
    private int label;
    private double weight;

    public Edge(int id, double weight) {
        this.id = id;
        this.weight = weight;
    }

    public Edge(int id, int label, double weight) {
        this.id = id;
        this.label = label;
        this.weight = weight;
    }

    public Edge() {}

    public int getId() { return this.id; }
    public void setId(int id) { this.id = id; }

    public double getWeight() { return this.weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public int getLabel() { return this.label; }
    public void setLabel( int label ) { this.label = label; }

}
