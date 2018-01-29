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
package org.igv.ui.panel;

import org.igv.ui.Track;

// Intended as the rough equivalent of the TrackPanel class of the Swing UI.  Work in progress.
// Note: not dealing with Track sorting yet.
public class TrackRow extends IGVRow<TrackNamePane, AttributePane, AxisPane, DataPaneContainer, TrackScrollPane> {
    private String name;

    public TrackRow(String name, Track track, MainContentPane mainContentPane) {
        this.name = name;
        // TODO: determine correct row & track height settings
        // The following is just a mock-up to explore sizing before we start drawing actual data.
        double rowHeight = 200.0;
        TrackScrollPane trackScrollPane = new TrackScrollPane(this);
        trackScrollPane.prefHeightProperty().set(rowHeight);
        trackScrollPane.minHeightProperty().set(rowHeight);
        trackScrollPane.maxHeightProperty().set(rowHeight);
        TrackNamePane namePane = new TrackNamePane();
        AttributePane attributePane = new AttributePane();
        AxisPane axisPane = new AxisPane();
        DataPaneContainer contentContainer = new DataPaneContainer(track);
        init(mainContentPane, namePane, attributePane, axisPane, contentContainer, trackScrollPane);
    }

    public String getName() {
        return name;
    }
}
