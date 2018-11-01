package com.org.shark.graphtoolkits.algorithm.aspectmodel;

/**
 * A single aspect is defined by a certain pattern.
 */
public class SingleAspectHttp {
    public enum SAType {
        SP, /* (s, p, ?) */
        PO, /* (?, p, 0) */
        REL /* (?, p, ?) */
    }
    private double priorScore = 1.0;
    private String rel;
    private String ent;
    private SAType saType;

    //REL
    public SingleAspectHttp(String rel) {
        this.saType = SAType.REL;
        this.rel = rel;
        this.ent = "";
    }

    //SP and PO
    public SingleAspectHttp(SAType saType, String ent, String rel) {
        this.saType = saType;
        this.ent = ent;
        this.rel = rel;
    }

    public String getRel() {
        return this.rel;
    }

    public String getEntity() {
        return this.ent;
    }

    public SAType getSAType() {
        return this.saType;
    }

    public double getPriorScore(){
        return this.priorScore;
    }

    public void setPriorScore(double score) {
        this.priorScore = score;
    }

    /**
     * refer to https://stackoverflow.com/questions/11742593/what-is-the-hashcode-for-a-custom-class-having-just-two-int-properties
     * @return
     *
    public int hashCode() {
        int hash = 17;
        hash = hash * 31 + relId;
        hash = hash * 31 + eid;
        hash = hash * 31 + saType.ordinal();
        return hash;
    }
    */
    public boolean equals(Object obj) {
        SingleAspectHttp other = (SingleAspectHttp) obj;
        return other.ent.equals(ent) && other.rel.equals(rel) && other.saType.ordinal() == saType.ordinal();
    }

    public String toString() {
        return "(r:"+rel +",o:"+ent+", "+saType.toString()+")";
    }

}
