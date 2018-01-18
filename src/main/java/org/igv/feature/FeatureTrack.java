package org.igv.feature;

import htsjdk.samtools.util.CloseableIterator;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.broad.igv.ui.panel.ReferenceFrame;
import org.broad.igv.util.ResourceLocator;
import org.igv.tribble.Feature;
import org.igv.tribble.FeatureReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jrobinso on 1/16/18.
 * <p>
 * Prototype track class.
 */
public class FeatureTrack {


    FeatureReader reader;

    // Static map chr -> features.  To be replaced by queryable feature source
    Map<String, List<Feature>> featureMap;


    public FeatureTrack(ResourceLocator locator) {

        reader = FeatureReaderFactory.getReader(locator);

        try {
            init();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void init() throws IOException {

        featureMap = new HashMap<>();

        CloseableIterator<Feature> iter = reader.iterator();

        while (iter.hasNext()) {

            Feature f = iter.next();

            String chrName = f.getChr().startsWith("chr") ? f.getChr() : "chr" + f.getChr();  // TODO -- replace with proper aliasing.

            List<Feature> features = featureMap.get(chrName);
            if (features == null) {
                features = new ArrayList<>();
                featureMap.put(chrName, features);
            }
            features.add(f);

        }


    }


    public void draw(GraphicsContext ctx, ReferenceFrame frame) {

        int startBP = (int) frame.getOrigin();
        int endBP = (int) frame.getEnd();

        List<Feature> featureList = featureMap.get(frame.getChrName());

        if (featureList == null) return;

        for (Feature f : featureList) {

            if (f.getEnd() < startBP) continue;
            if (f.getStart() > endBP) break;

            // Draw rectangle for entire feature.  Exons etc to come later
            double p0 = (f.getStart() - frame.getOrigin()) / frame.getScale();  // Scale is in bp / pixel
            double p1 = (f.getEnd() - frame.getOrigin()) / frame.getScale();


            ctx.setFill(Color.BLUE);
            ctx.fillRect(p0, 5, (p1 - p0), 25);

        }


    }


}
