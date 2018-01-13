package org.igv.feature;

import javafx.scene.paint.Color;
import org.igv.tribble.annotation.Strand;

import java.util.List;

/**
 * Created by jrobinso on 1/12/18.
 */
public class Bed6Feature extends Bed4Feature{

    private float score;
    private Strand strand;

    public Bed6Feature(String chr, int start, int end, String name, float score, Strand strand) {
        super(chr, start, end, name);
        this.score = score;
        this.strand = strand;
    }

    @Override
    public double getScore() {
        return score;
    }

    @Override
    public Strand getStrand() {
        return strand;
    }
}
