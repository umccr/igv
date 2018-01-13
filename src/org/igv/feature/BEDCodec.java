/*
 * The MIT License
 *
 * Copyright (c) 2013 The Broad Institute
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
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.igv.feature;

import htsjdk.tribble.bed.FullBEDFeature;
import org.igv.tribble.AsciiFeatureCodec;
import org.igv.tribble.Feature;
import org.igv.tribble.annotation.Strand;
import org.igv.tribble.readers.LineIterator;
import org.igv.tribble.util.ParsingUtils;
import org.igv.utils.ColorUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Codec for parsing BED file, as described by UCSC
 * See https://genome.ucsc.edu/FAQ/FAQformat.html#format1
 *
 * @author jrobinso
 *         Date: Dec 20, 2009
 */
public class BEDCodec extends AsciiFeatureCodec {

    private static final Pattern SPLIT_PATTERN = Pattern.compile("\\t|( +)");

    public Feature decodeLoc(String line) {
        return decode(line);
    }

    @Override
    public Feature decode(String line) {

        if (line.trim().isEmpty()) {
            return null;
        }

        if (line.startsWith("#") || line.startsWith("track") || line.startsWith("browser")) {
            this.readHeaderLine(line);
            return null;
        }

        String[] tokens = SPLIT_PATTERN.split(line, -1);

        return decode(tokens);
    }

    @Override
    public Object readActualHeader(LineIterator reader) {
        return null;
    }

    public Feature decode(String[] tokens) {

        int tokenCount = tokens.length;

        // The first 3 columns are non optional for BED.  We will relax this
        // and only require 2.
        if (tokenCount < 2) {
            return null;
        }

        String chr = tokens[0];

        int start = Integer.parseInt(tokens[1]);

        int end = start;
        if (tokenCount > 2) {
            end = Integer.parseInt(tokens[2]);
        }

        // The rest of the columns are optional.  Stop parsing upon encountering
        // a non-expected value

        if (tokenCount < 4) {
            return new Bed3Feature(chr, start, end);
        }

        String name = tokens[3].replaceAll("\"", "");

        if (tokenCount < 5) {
            return new Bed4Feature(chr, start, end, name);
        }


        float score;
        try {
            score = Float.parseFloat(tokens[4]);
        } catch (NumberFormatException numberFormatException) {
            // Unexpected, but does not invalidate the previous values.
            // Stop parsing the line here but keep the feature
            // Don't log, would just slow parsing down.
            score = 1000;
        }


        // Strand
        Strand strand = Strand.NONE;
        if (tokenCount > 5) {
            String strandString = tokens[5].trim();
            char strandChar = (strandString.isEmpty()) ? ' ' : strandString.charAt(0);
            if (strandChar == '-') {
                strand = Strand.NEGATIVE;
            } else if (strandChar == '+') {
                strand = Strand.POSITIVE;
            }
        }

        if (tokenCount < 7) {
            return new Bed6Feature(chr, start, end, name, score, strand);
        } else {
            Bed12Feature feature = new Bed12Feature(chr, start, end, name, score, strand);

            // Thick start & end
            if(tokenCount > 7) {
                feature.setThickStart(Integer.parseInt(tokens[6]));
                feature.setThickEnd(Integer.parseInt(tokens[7]));
            }

            //Color
            if (tokenCount > 8) {
                String colorString = tokens[8];
                if(!(colorString.equals("0") || colorString.equals("."))) {
                    feature.setColor(ColorUtils.parseColor(colorString));
                }
            }


            //Exons
            if (tokenCount > 11) {
                createExons(start, tokens, feature, feature.getStrand());
            }

            return feature;
        }
    }

    protected boolean readHeaderLine(String line) {
        //We don't parse BED header
        return false;
    }

    private void createExons(int start, String[] tokens, Bed12Feature gene,
                             Strand strand) throws NumberFormatException {

        int cdStart = Integer.parseInt(tokens[6]);
        int cdEnd = Integer.parseInt(tokens[7]);

        int exonCount = Integer.parseInt(tokens[9]);
        String[] exonSizes = new String[exonCount];
        String[] startsBuffer = new String[exonCount];
        ParsingUtils.split(tokens[10], exonSizes, ',');
        ParsingUtils.split(tokens[11], startsBuffer, ',');

        int exonNumber = (strand == Strand.NEGATIVE ? exonCount : 1);
        List<Exon> exons = new ArrayList<>();

        if (startsBuffer.length == exonSizes.length) {
            for (int i = 0; i < startsBuffer.length; i++) {

                int exonStart = start + Integer.parseInt(startsBuffer[i]);
                int exonEnd = exonStart + Integer.parseInt(exonSizes[i]) - 1;
                if (strand == Strand.NEGATIVE) {
                    exonNumber--;
                } else {
                    exonNumber++;
                }

                Exon exon = new Exon(exonNumber, exonStart, exonEnd);

                if (cdStart >= exonEnd || cdEnd <= exonStart) {
                    exon.setUtr(true);
                } else {
                    exon.setUtr(false);
                    if (cdStart >= exonStart) {
                        exon.setCdStart(cdStart);
                    }
                    if (cdEnd <= exonEnd) {
                        exon.setCdEnd(cdEnd);
                    }
                }
                exons.add(exon);
            }
        }

        gene.setExons(exons);
    }

    @Override
    public boolean canDecode(final String path) {
        return path.toLowerCase().endsWith(".bed");
    }

    /**
     * Indicate whether co-ordinates or 0-based or 1-based.
     * <p/>
     * Tribble uses 1-based, BED files use 0.
     * e.g.:
     * start_position = bedline_start_position - startIndex.value()
     */
    public enum StartOffset {
        ZERO(0),
        ONE(1);
        private int start;

        private StartOffset(int start) {
            this.start = start;
        }

        public int value() {
            return this.start;
        }
    }

}
