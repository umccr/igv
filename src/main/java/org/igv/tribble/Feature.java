package org.igv.tribble;

import javafx.scene.paint.Color;
import org.igv.feature.Exon;
import org.igv.tribble.annotation.Strand;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by jrobinso on 1/12/18.
 */
public interface Feature {

    String getChr();

    long getStart();

    default long getEnd() {
        return getStart() + 1;
    }

    default String getName() {
        return "";
    }

    default double getScore() {
        return 0.0;
    }

    default Strand getStrand() {
        return Strand.NONE;
    }

    default long getThickStart() {
        return getStart();
    }

    default long getThickEnd() {
        return getEnd();
    }

    default Color getColor() {
        return Color.BLACK;
    }

    default List<Exon> getExons() {
        return Collections.EMPTY_LIST;
    }

    default Map<String, Object> getAttributes() {
        return Collections.EMPTY_MAP;
    }

}
