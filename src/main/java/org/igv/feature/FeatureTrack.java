/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2007-2018 Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.igv.feature;

import htsjdk.samtools.util.CloseableIterator;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.broad.igv.renderer.*;
import org.broad.igv.ui.panel.ReferenceFrame;
import org.broad.igv.util.ResourceLocator;
import org.igv.tribble.Feature;
import org.igv.tribble.FeatureReader;
import org.igv.ui.Track;

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
public class FeatureTrack implements Track {
    private String name;

    FeatureReader reader;
    FeatureRenderer renderer;
    
    // TODO: set height properly based on data.
    // This is a mock-up for now.
    private DoubleProperty prefHeightProperty = new SimpleDoubleProperty(40.0);

    // Static map chr -> features.  To be replaced by queryable feature source
    Map<String, List<Feature>> featureMap;


    public FeatureTrack(String name, ResourceLocator locator) {
        this.name = name;
        
        reader = FeatureReaderFactory.getReader(locator);
        renderer = new FeatureRenderer();

        try {
            init();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String getName() {
        return name;
    }
    
    public DoubleProperty prefHeightProperty() {
        return prefHeightProperty;
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
        ctx.setStroke(Color.rgb(200, 200, 210));
        double displayWidth = frame.displayWidthProperty().doubleValue();

        // TODO: starting working in doubles everywhere for JavaFX compatibility
        int startBP = (int) frame.getOrigin();
        int endBP = (int) frame.getEnd();
        int featureHeight = 25;
        
        // Draw a border; temporary, just to show position
        ctx.strokeRect(0.0, 0.0, displayWidth, featureHeight);

        List<Feature> featureList = featureMap.get(frame.getChrName());

        if (featureList == null) return;

        for (Feature f : featureList) {

            if (f.getEnd() < startBP) continue;
            if (f.getStart() > endBP) break;

            ctx.setFill(Color.BLUE);
            renderer.renderFeature(f, startBP, frame.getScale(), featureHeight, ctx);
            // Draw rectangle for entire feature.  Exons etc to come later
//            double p0 = (f.getStart() - frame.getOrigin()) / frame.getScale();  // Scale is in bp / pixel
//            double p1 = (f.getEnd() - frame.getOrigin()) / frame.getScale();
//
//
//            ctx.setFill(Color.BLUE);
//            ctx.fillRect(p0, 5, (p1 - p0), 25);

        }


    }


}
