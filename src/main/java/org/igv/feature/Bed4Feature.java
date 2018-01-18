package org.igv.feature;

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
