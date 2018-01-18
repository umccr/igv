package org.igv.feature;

import javafx.scene.paint.Color;
import org.igv.tribble.annotation.Strand;

import java.util.List;

/**
 * Created by jrobinso on 1/12/18.
 * <p>
 * Represents a full 12-column bed feature
 */
public class Bed12Feature extends Bed6Feature {


    private int thickStart;
    private int thickEnd;
    private Color color;
    List<Exon> exons;

    public Bed12Feature(String chr, int start, int end, String name, float score, Strand strand) {
        super(chr, start, end, name, score, strand);
    }

    public void setThickStart(int thickStart) {
        this.thickStart = thickStart;
    }

    public void setThickEnd(int thickEnd) {
        this.thickEnd = thickEnd;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setExons(List<Exon> exons) {
        this.exons = exons;
    }

    @Override
    public long getThickStart() {
        return thickStart;
    }

    @Override
    public long getThickEnd() {
        return thickEnd;
    }

    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public List<Exon> getExons() {
        return exons;
    }

}
