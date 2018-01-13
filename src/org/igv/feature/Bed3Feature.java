package org.igv.feature;

import org.igv.tribble.Feature;

/**
 * Created by jrobinso on 1/12/18.
 * <p>
 * Represents a full 12-column bed feature
 */
public class Bed3Feature implements Feature {

    private String chr;
    private int start;
    private int end;

    public Bed3Feature(String chr, int start, int end) {
        this.chr = chr;
        this.start = start;
        this.end = end;
    }

    @Override
    public String getChr() {
        return chr;
    }

    @Override
    public long getStart() {
        return start;
    }

    @Override
    public long getEnd() {
        return end;
    }



}
