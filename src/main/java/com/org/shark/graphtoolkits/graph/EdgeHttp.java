package com.org.shark.graphtoolkits.graph;

/**
 * Created by gsc.
 * the id of the edge is reserved but useless.
 */
public class EdgeHttp {
    private String label;
    private double weight;
    private String entity;

    public EdgeHttp(String entity, double weight) {
        this.entity = entity;
        this.weight = weight;
    }

    public EdgeHttp(String entity, String label, double weight) {
        this.entity = entity;
        this.label = label;
        this.weight = weight;
    }

    public EdgeHttp() {}

    public String getEntity() { return this.entity; }
    public void setEntity(String entity) { this.entity = entity; }

    public double getWeight() { return this.weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public String getLabel() { return this.label; }
    public void setLabel( String label ) { this.label = label; }

}
