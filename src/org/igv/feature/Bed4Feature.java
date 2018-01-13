package org.igv.feature;

import javafx.scene.paint.Color;
import org.igv.tribble.annotation.Strand;

import java.util.List;

/**
 * Created by jrobinso on 1/12/18.
 */
public class Bed4Feature extends Bed3Feature {

    private String name;


    public Bed4Feature(String chr, int start, int end, String name) {
        super(chr, start, end);
        this.name = name;
    }


    @Override
    public String getName() {
        return name;
    }

}
