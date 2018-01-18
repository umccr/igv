package org.igv.feature;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.igv.tribble.Feature;
import org.igv.tribble.annotation.Strand;

import java.util.List;

/**
 * Created by jrobinso on 1/12/18.
 */
public class FeatureRenderer {


    void renderFeature(Feature feature, int bpStart, double xScale, int pixelHeight, GraphicsContext ctx) {

//        var x, e, exonCount, cy, direction, exon, ePx, ePx1, ePxU, ePw, py2, h2, py,
//                windowX, windowX1,
//                coord = calculateFeatureCoordinates(feature, bpStart, xScale),
//                h = this.featureHeight,
//                step = this.arrowSpacing,
//                color = this.color;

        Coords coord = calculateFeatureCoordinates(feature, bpStart, xScale);
//        if (this.config.colorBy) {
//            var colorByValue = feature[this.config.colorBy.field];
//            if (colorByValue) {
//                color = this.config.colorBy.pallete[colorByValue];
//            }
//        }
//        else if (feature.color) {
        Color color = feature.getColor();
//        }


        ctx.setFill(color);
        ctx.setStroke(color);

//        if (this.displayMode === "SQUISHED" && feature.row !== undefined) {
//            h = this.featureHeight / 2;
//            py = this.expandedCallHeight * feature.row + 2;
//        } else if (this.displayMode === "EXPANDED" && feature.row !== undefined) {
//            py = this.squishedCallHeight * feature.row + 5;
//        } else {  // collapsed
//            py = 5;
//        }

        int arrowStep = 30;
        int py = 5;
        int h = 20;

        int cy = py + h / 2;
        int h2 = h / 2;
        int py2 = cy - h2 / 2;

        int exonCount = feature.getExons().size();

        if (exonCount == 0) {
            ctx.fillRect(coord.px, py, coord.pw, h);

        } else {
            // multi-exon transcript
            ctx.strokeLine(coord.px + 1, cy, coord.px1 - 1, cy); // center line for introns

            int direction = feature.getStrand() == Strand.POSITIVE ? 1 : -1;

            for (int x = coord.px + arrowStep / 2; x < coord.px1; x += arrowStep) {
                // draw arrowheads along central line indicating transcribed orientation
                ctx.strokeLine(x - direction * 2, cy - 2, x, cy);
                ctx.strokeLine(x - direction * 2, cy + 2, x, cy);
            }

            List<Exon> exons = feature.getExons();

            for (Exon exon : exons) {

                // draw the exons
                int ePx = (int) Math.round((exon.getStart() - bpStart) / xScale);
                int ePx1 = (int) Math.round((exon.getEnd() - bpStart) / xScale);
                int ePw = Math.max(1, ePx1 - ePx);

                if (exon.isUtr()) {
                    ctx.fillRect(ePx, py2, ePw, h2); // Entire exon is UTR
                } else {
                    if (exon.getCdStart() > bpStart) {
                        int ePxU = (int) Math.round((exon.getCdStart() - bpStart) / xScale);
                        ctx.fillRect(ePx, py2, ePxU - ePx, h2); // start is UTR
                        ePw -= (ePxU - ePx);
                        ePx = ePxU;
                    }
                    if (exon.getCdEnd() < exon.getEnd()) {
                        int ePxU = (int) Math.round((exon.getCdEnd() - bpStart) / xScale);
                        ctx.fillRect(ePxU, py2, ePx1 - ePxU, h2); // start is UTR
                        ePw -= (ePx1 - ePxU);
                        ePx1 = ePxU;
                    }

                    ctx.fillRect(ePx, py, ePw, h);

                    // Arrows
                    if (ePw > arrowStep + 5) {
                        ctx.setFill(Color.WHITE);
                        ctx.setStroke(Color.WHITE);
                        for (int x = ePx + arrowStep / 2; x < ePx1; x += arrowStep) {
                            // draw arrowheads along central line indicating transcribed orientation
                            ctx.strokeLine(x - direction * 2, cy - 2, x, cy);
                            ctx.strokeLine(x - direction * 2, cy + 2, x, cy);
                        }
                        ctx.setFill(color);
                        ctx.setStroke(color);

                    }
                }
            }
        }

    }


    private Coords calculateFeatureCoordinates(Feature feature, int bpStart, double xScale) {

        int px = (int) Math.round((feature.getStart() - bpStart) / xScale),
                px1 = (int) Math.round((feature.getEnd() - bpStart) / xScale),
                pw = px1 - px;

        if (pw < 3) {
            pw = 3;
            px -= 1;
        }

        return new Coords(px, px1, pw);

    }

    static class Coords {
        int px;
        int px1;
        int pw;

        public Coords(int px, int px1, int pw) {
            this.px = px;
            this.px1 = px1;
            this.pw = pw;
        }
    }
}
