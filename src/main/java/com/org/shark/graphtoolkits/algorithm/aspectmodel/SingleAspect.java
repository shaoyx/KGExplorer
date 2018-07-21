package com.org.shark.graphtoolkits.algorithm.aspectmodel;

/**
 * A single aspect is defined by a certain pattern.
 */
public class SingleAspect {
    public enum SAType {
        SP, /* (s, p, ?) */
        PO, /* (?, p, 0) */
        REL /* (?, p, ?) */
    }
    private double priorScore = 1.0;
    private int relId;
    private int eid;
    private SAType saType;

    public SingleAspect(int relId) {
        this.saType = SAType.REL;
        this.relId = relId;
        this.eid = -1;
    }

    public SingleAspect(SAType saType, int eid, int relId) {
        this.saType = saType;
        this.eid = eid;
        this.relId = relId;
    }

    public int getRelId() {
        return this.relId;
    }

    public int getEntityId() {
        return this.eid;
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
     */
    public int hashCode() {
        int hash = 17;
        hash = hash * 31 + relId;
        hash = hash * 31 + eid;
        hash = hash * 31 + saType.ordinal();
        return hash;
    }

    public boolean equals(Object obj) {
        SingleAspect other = (SingleAspect) obj;
        return other.eid == eid && other.relId == relId && other.saType.ordinal() == saType.ordinal();
    }

    public String toString() {
        return "(r:"+relId +",o:"+eid+", "+saType.toString()+")";
    }

}
