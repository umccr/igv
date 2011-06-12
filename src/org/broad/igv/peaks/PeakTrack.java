/*
 * Copyright (c) 2007-2011 by The Broad Institute, Inc. and the Massachusetts Institute of
 * Technology.  All Rights Reserved.
 *
 * This software is licensed under the terms of the GNU Lesser General Public License (LGPL),
 * Version 2.1 which is available at http://www.opensource.org/licenses/lgpl-2.1.php.
 *
 * THE SOFTWARE IS PROVIDED "AS IS." THE BROAD AND MIT MAKE NO REPRESENTATIONS OR
 * WARRANTES OF ANY KIND CONCERNING THE SOFTWARE, EXPRESS OR IMPLIED, INCLUDING,
 * WITHOUT LIMITATION, WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, NONINFRINGEMENT, OR THE ABSENCE OF LATENT OR OTHER DEFECTS, WHETHER
 * OR NOT DISCOVERABLE.  IN NO EVENT SHALL THE BROAD OR MIT, OR THEIR RESPECTIVE
 * TRUSTEES, DIRECTORS, OFFICERS, EMPLOYEES, AND AFFILIATES BE LIABLE FOR ANY DAMAGES
 * OF ANY KIND, INCLUDING, WITHOUT LIMITATION, INCIDENTAL OR CONSEQUENTIAL DAMAGES,
 * ECONOMIC DAMAGES OR INJURY TO PROPERTY AND LOST PROFITS, REGARDLESS OF WHETHER
 * THE BROAD OR MIT SHALL BE ADVISED, SHALL HAVE OTHER REASON TO KNOW, OR IN FACT
 * SHALL KNOW OF THE POSSIBILITY OF THE FOREGOING.
 */

package org.broad.igv.peaks;

import org.broad.igv.data.DataSource;
import org.broad.igv.data.LocusScoreUtils;
import org.broad.igv.feature.Chromosome;
import org.broad.igv.feature.FeatureUtils;
import org.broad.igv.feature.LocusScore;
import org.broad.igv.feature.genome.Genome;
import org.broad.igv.renderer.DataRange;
import org.broad.igv.renderer.Renderer;
import org.broad.igv.tdf.TDFDataSource;
import org.broad.igv.tdf.TDFReader;
import org.broad.igv.track.*;
import org.broad.igv.ui.IGV;
import org.broad.igv.ui.panel.ReferenceFrame;
import org.broad.igv.util.ParsingUtils;
import org.broad.igv.util.ResourceLocator;
import org.broad.tribble.Feature;
import org.broad.tribble.util.SeekableStream;
import org.broad.tribble.util.SeekableStreamFactory;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.*;
import java.util.List;

/**
 * @author jrobinso
 * @date Apr 22, 2011
 */
public class PeakTrack extends AbstractTrack {


    static List<SoftReference<PeakTrack>> instances = new ArrayList();

    private static PeakControlDialog controlDialog;
    private static float scoreThreshold = 30;
    private static float foldChangeThreshold = 0;

    private static ColorOption colorOption = ColorOption.SCORE;
    private static boolean showPeaks = true;
    private static boolean showSignals = true;

    int nTimePoints;
    Map<String, List<Peak>> peakMap = new HashMap();
    Map<String, List<Peak>> filteredPeakMap = new HashMap();
    Renderer renderer = new PeakRenderer();


    Map<String, Long> index = new HashMap();


    // Path to the compressed signal (TDF) file and data source
    String signalPath;
    WrappedDataSource signalSource;

    // Paths to the time series signal files and data sources
    String[] timeSignalPaths;
    WrappedDataSource[] timeSignalSources;

    // Data range
    DataRange scoreDataRange = new DataRange(0, 0, 100);
    DataRange signalDataRange = new DataRange(0, 0, 1000f);

    static boolean commandBarAdded = false;

    int bandHeight;
    int signalHeight;
    int peakHeight;
    int gapHeight;

    Genome genome;
    private String peaksPath;

    /**
     * @param locator -- path to a peaks.cfg file
     * @param genome
     * @throws IOException
     */
    public PeakTrack(ResourceLocator locator, Genome genome) throws IOException {
        super(locator);
        this.genome = genome;
        setHeight(30);
        parse(locator.getPath());
        loadPeaks();

        instances.add(new SoftReference(this));

        if (!commandBarAdded) {
            IGV.getInstance().getContentPane().addCommandBar(new PeakCommandBar());
            commandBarAdded = true;
        }
    }

    /**
     * timePoints=0,30,60,120
     * peaks=http://www.broadinstitute.org/igvdata/ichip/peaks/AHR.peak
     * signals=http://www.broadinstitute.org/igvdata/ichip/tdf/compressed/AHR.merged.bam.tdf
     * timeSignals=http://www.broadinstitute.org/igvdata/ichip/tdf/timecourses/AHR_0/AHR_0.merged.bam.tdf,http...
     *
     * @param path
     * @throws IOException
     */

    private void parse(String path) throws IOException {

        BufferedReader br = null;


        try {
            br = ParsingUtils.openBufferedReader(path);

            String nextLine = br.readLine();
            if (nextLine.startsWith("track")) {
                TrackProperties props = new TrackProperties();
                ParsingUtils.parseTrackLine(nextLine, props);
                setProperties(props);
            }

            nextLine = br.readLine();
            String[] tokens = nextLine.split("=");
            if (tokens.length < 2 || !tokens[0].equals("timePoints")) {
                throw new RuntimeException("Unexpected timePoints line: " + nextLine);
            }
            tokens = tokens[1].split(",");
            nTimePoints = tokens.length;

            nextLine = br.readLine();
            tokens = nextLine.split("=");
            if (tokens.length < 2 || !tokens[0].equals("peaks")) {
                throw new RuntimeException("Unexpected timePoints line: " + nextLine);
            }
            peaksPath = tokens[1];


            nextLine = br.readLine();
            tokens = nextLine.split("=");
            if (tokens.length < 2 || !tokens[0].equals("signals")) {
                throw new RuntimeException("Unexpected timePoints line: " + nextLine);
            }
            signalPath = tokens[1];
            if (signalPath != null) {
                signalSource = new WrappedDataSource(new TDFDataSource(TDFReader.getReader(signalPath), 0, "", genome));
                signalSource.setNormalizeCounts(true, 1.0e9f);
            }

            nextLine = br.readLine();
            tokens = nextLine.split("=");
            if (tokens.length < 2 || !tokens[0].equals("timeSignals")) {
                throw new RuntimeException("Unexpected timePoints line: " + nextLine);
            }
            timeSignalPaths = tokens[1].split(",");

            nextLine = br.readLine();
            if (!nextLine.startsWith("#index")) {
                throw new RuntimeException("Missing index");
            }
            while ((nextLine = br.readLine()) != null) {
                tokens = nextLine.split("\t");
                String chr = tokens[0];
                long position = Long.parseLong(tokens[1]);
                index.put(chr, position);
            }


        } finally {
            if (br != null) br.close();
        }


    }


    @Override
    public JPopupMenu getPopupMenu(TrackClickEvent te) {
        return new PeakTrackMenu(this, te);
    }

    @Override
    public DataRange getDataRange() {
        return showSignals ? signalDataRange : scoreDataRange;
    }

    public void render(RenderContext context, Rectangle rect) {

        try {
            List<Peak> peakList = getFilteredPeaks(context.getChr());
            if (peakList == null) {
                return;
            }

            renderer.render(peakList, context, rect, this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Renderer getRenderer() {
        return renderer;
    }

    @Override
    public int getMinimumHeight() {
        int h = 0;
        if (showPeaks) h += 5;
        if (showSignals) h += 10;
        if (showPeaks && showSignals) h += 2;

        if (getDisplayMode() == Track.DisplayMode.COLLAPSED) {
            return h;
        } else {
            return nTimePoints * h + gapHeight;
        }
    }


    @Override
    public void setHeight(int h) {
        super.setHeight(h);

        int nBands = getDisplayMode() == DisplayMode.COLLAPSED ? 1 : nTimePoints;

        bandHeight = h / nBands;
        peakHeight = Math.max(5, Math.min(bandHeight / 3, 10));
        signalHeight = bandHeight - peakHeight - gapHeight;

    }

    @Override
    public void setDisplayMode(DisplayMode mode) {
        super.setDisplayMode(mode);
        if (mode == Track.DisplayMode.COLLAPSED) {
            setHeight(bandHeight);
        } else {
            setHeight(nTimePoints * bandHeight + gapHeight);
        }
    }


    public String getValueStringAt(String chr, double position, int y, ReferenceFrame frame) {
        try {
            StringBuffer buf = new StringBuffer();
            buf.append(getName());
            if (showPeaks) {
                List<Peak> scores = getFilteredPeaks(chr);
                LocusScore score = getLocusScoreAt(scores, position, frame);
                buf.append((score == null) ? "" : score.getValueString(position, getWindowFunction()));
                if (showSignals) {
                    buf.append("<br>");
                }
            }
            if (showSignals && signalSource != null) {
                List<LocusScore> scores = signalSource.getSummaryScoresForRange(chr, (int) frame.getOrigin(), (int) frame.getEnd(), frame.getZoom());
                LocusScore score = getLocusScoreAt(scores, position, frame);
                buf.append((score == null) ? "" : "Score = " + score.getScore());
            }


            return buf.toString();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return "Error loading peaks: " + e.toString();
        }
    }


    // TODO -- the code below is an exact copy of code in DataTrack.   Refactor to share this.

    private LocusScore getLocusScoreAt(List<? extends LocusScore> scores, double position, ReferenceFrame frame) {

        if (scores == null) {
            return null;
        } else {
            // give a 2 pixel window, otherwise very narrow features will be missed.
            double bpPerPixel = frame.getScale();
            int buffer = (int) (2 * bpPerPixel);    /* * */
            return LocusScoreUtils.getFeatureAt(position, buffer, scores);
        }
    }

    public synchronized List<Peak> getFilteredPeaks(String chr) throws IOException {
        List<Peak> filteredPeaks = filteredPeakMap.get(chr);
        if (filteredPeaks == null) {
            filteredPeaks = new ArrayList();
            List<Peak> allPeaks = getAllPeaks(chr);
            if (allPeaks != null) {
                for (Peak peak : allPeaks) {
                    if (peak.getCombinedScore() >= scoreThreshold &&
                            peak.getFoldChange() >= foldChangeThreshold) {
                        filteredPeaks.add(peak);
                    }
                }
            }
        }
        filteredPeakMap.put(chr, filteredPeaks);


        return filteredPeaks;
    }

    private List<Peak> getAllPeaks(String chr) throws IOException {
        if (peakMap.isEmpty()) {
            loadPeaks();
        }
        return peakMap.get(chr);
    }

    private void loadPeaks() throws IOException {
        InputStream is = null;
        try {
            String binPath = peaksPath + ".bin.gz";
            is = ParsingUtils.openInputStream(new ResourceLocator(binPath));
            List<Peak> p = PeakParser.loadPeaksBinary(is);

            for (Peak peak : p) {
                final String peakChr = peak.getChr();
                List<Peak> peakList = peakMap.get(peakChr);
                if (peakList == null) {
                    peakList = new ArrayList(1000);
                    peakMap.put(peakChr, peakList);
                }
                peakList.add(peak);
            }

        }
        finally {
            if (is != null) is.close();
        }
    }


    private static void clearFilteredLists() {
        for (SoftReference<PeakTrack> instance : instances) {
            PeakTrack track = instance.get();
            if (track != null) {
                track.filteredPeakMap.clear();
            }
        }
    }


    public static boolean controlDialogIsOpen() {
        return controlDialog != null && controlDialog.isVisible();
    }


    static synchronized void openControlDialog() {
        if (controlDialog == null) {
            controlDialog = new PeakControlDialog(IGV.getMainFrame());
        }
        controlDialog.setVisible(true);
    }


    public static float getScoreThreshold() {
        return scoreThreshold;
    }

    public static void setScoreThreshold(float t) {
        scoreThreshold = t;
        clearFilteredLists();
    }

    public static ColorOption getColorOption() {
        return colorOption;
    }

    public static void setShadeOption(ColorOption colorOption) {
        PeakTrack.colorOption = colorOption;
    }

    public static float getFoldChangeThreshold() {
        return foldChangeThreshold;
    }

    public static void setFoldChangeThreshold(float foldChangeThreshold) {
        PeakTrack.foldChangeThreshold = foldChangeThreshold;
        clearFilteredLists();
    }


    public float getRegionScore(String chr, int start, int end, int zoom, RegionScoreType type, ReferenceFrame frame) {

        int interval = end - start;
        if (interval <= 0) {
            return Float.MIN_VALUE;
        }

        try {
            List<Peak> scores = getFilteredPeaks(chr);
            int startIdx = Math.max(0, FeatureUtils.getIndexBefore(start, scores));

            float regionScore = Float.MIN_VALUE;
            for (int i = startIdx; i < scores.size(); i++) {
                Peak score = scores.get(i);
                if (score.getEnd() < start) continue;
                if (score.getStart() > end) break;
                final float v = score.getScore();
                if (v > regionScore) regionScore = v;
            }
            return regionScore;
        } catch (IOException e) {
            return Float.MIN_VALUE;
        }
    }


    /**
     * Get the closet filter peak, within 2kb, of the given position.
     *
     * @param chr
     * @param position
     * @return
     */
    public Peak getFilteredPeakNearest(String chr, double position) {
        try {
            List<Peak> scores = getFilteredPeaks(chr);
            int startIdx = FeatureUtils.getIndexBefore(position, scores);

            Peak closestPeak = null;
            double closestDistance = Integer.MAX_VALUE;
            if (startIdx >= 0) {
                if (startIdx > 0) startIdx--;
                for (int i = startIdx; i < scores.size(); i++) {
                    Peak peak = scores.get(i);
                    if (position > peak.getStart() && position < peak.getEnd()) {
                        return peak;
                    }
                    double distance = Math.min(Math.abs(position - peak.getStart()), Math.abs(position - peak.getEnd()));
                    if (distance > closestDistance) {
                        return closestDistance < 2000 ? closestPeak : null;

                    } else {
                        closestDistance = distance;
                        closestPeak = peak;
                    }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;

    }


    public static boolean isShowPeaks() {
        return showPeaks;
    }

    public static void setShowPeaks(boolean b) {
        showPeaks = b;
    }

    public static boolean isShowSignals() {
        return showSignals;
    }

    public static void setShowSignals(boolean b) {
        showSignals = b;
    }

    public DataSource[] getTimeSignalSources() {

        if (timeSignalSources == null) {
            if (timeSignalPaths != null && timeSignalPaths.length > 0) {
                timeSignalSources = new WrappedDataSource[timeSignalPaths.length];
                for (int i = 0; i < timeSignalPaths.length; i++) {
                    try {
                        timeSignalSources[i] = new WrappedDataSource(new TDFDataSource(TDFReader.getReader(timeSignalPaths[i]), 0, "", genome));
                        timeSignalSources[i].setNormalizeCounts(true, 1.0e9f);
                    } catch (Exception e) {
                        timeSignalSources[i] = null;
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
            }
        }

        return timeSignalSources;
    }


    enum ColorOption {
        SCORE, FOLD_CHANGE
    }


    class WrappedDataSource implements DataSource {

        TDFDataSource source;

        WrappedDataSource(TDFDataSource source) {
            this.source = source;
        }

        public List<LocusScore> getSummaryScoresForRange(String chr, int startLocation, int endLocation, int zoom) {

            List<LocusScore> scores = new ArrayList(1000);


            if (scoreThreshold <= 0 && foldChangeThreshold <= 0) {
                return source.getSummaryScoresForRange(chr, startLocation, endLocation, zoom);
            } else {
                try {
                    List<Peak> peaks = getFilteredPeaks(chr);
                    if (peaks == null) {
                        return scores;
                    }
                    int startIdx = FeatureUtils.getIndexBefore(startLocation, peaks);
                    if (startIdx >= 0) {
                        for (int i = startIdx; i < peaks.size(); i++) {
                            Peak peak = peaks.get(i);

                            final int peakEnd = peak.getEnd();
                            if (peakEnd < startLocation) continue;

                            final int peakStart = peak.getStart();
                            if (peakStart > endLocation) break;

                            List<LocusScore> peakScores = source.getSummaryScoresForRange(chr, peakStart, peakEnd, zoom);
                            for (LocusScore ps : peakScores) {
                                if (ps.getEnd() < peakStart) continue;
                                if (ps.getStart() > peakEnd) break;
                                scores.add(ps);
                            }

                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                return scores;
            }
        }


        public double getDataMax() {
            return source.getDataMax();
        }

        public double getDataMin() {
            return source.getDataMin();
        }

        public TrackType getTrackType() {
            return source.getTrackType();
        }

        public void setWindowFunction(WindowFunction statType) {
            source.setWindowFunction(statType);
        }

        public boolean isLogNormalized() {
            return source.isLogNormalized();
        }

        public void refreshData(long timestamp) {
            source.refreshData(timestamp);
        }

        public WindowFunction getWindowFunction() {
            return source.getWindowFunction();
        }

        public Collection<WindowFunction> getAvailableWindowFunctions() {
            return source.getAvailableWindowFunctions();
        }

        public void setNormalizeCounts(boolean b, float v) {
            source.setNormalizeCounts(b, v);
        }
    }


}
