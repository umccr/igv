package org.igv.feature;

/**
 * Created by jrobinso on 1/12/18.
 */
public class Exon {

    int number;
    long start;
    long end;
    long cdStart;
    long cdEnd;
    boolean utr;

    public Exon(int number, long start, long end) {
        this.number = number;
        this.start = start;
        this.end = end;
    }

    public int getNumber() {
        return number;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public long getCdStart() {
        return cdStart;
    }

    public void setCdStart(long cdStart) {
        this.cdStart = cdStart;
    }

    public long getCdEnd() {
        return cdEnd;
    }

    public void setCdEnd(long cdEnd) {
        this.cdEnd = cdEnd;
    }

    public boolean isUtr() {
        return utr;
    }

    public void setUtr(boolean utr) {
        this.utr = utr;
    }
}
