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

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.apache.log4j.Logger;
import org.broad.igv.prefs.PreferencesManager;
import org.broad.igv.ui.panel.FrameManager;
import org.igv.ui.IGVToolBarManager;
import org.igv.ui.Track;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.broad.igv.prefs.Constants.BACKGROUND_COLOR;
import static org.broad.igv.prefs.Constants.NAME_PANEL_WIDTH;

// Intended as the rough equivalent of the MainPanel class of the Swing UI.  Work in progress.
public class MainContentPane extends BorderPane {
    private static Logger log = Logger.getLogger(MainContentPane.class);
    
    // Probably most/all components should be instance vars.  Will migrate as need arises.
    private HeaderRow headerRow = null;
    private VBox trackContainer = new VBox();

    private DoubleProperty namePaneWidthProp = new SimpleDoubleProperty(
            PreferencesManager.getPreferences().getAsFloat(NAME_PANEL_WIDTH));
    private DoubleProperty attributePaneWidthProp = new SimpleDoubleProperty(20);
    private DoubleProperty axisPaneWidthProp = new SimpleDoubleProperty(10);

    // May not be needed.  Not used for now but created for managing tracks by name (change order, remove, etc).
    // I *think* it will still be needed but some other means might present itself.
    private final Map<String, TrackRow> trackRowByName = new HashMap<String, TrackRow>();

    private IGVToolBarManager igvToolBarManager;
    
    public MainContentPane() {
    }

    public IGVToolBarManager getIgvToolBarManager() {
        return igvToolBarManager;
    }

    public void setIgvToolBarManager(IGVToolBarManager igvToolBarManager) {
        this.igvToolBarManager = igvToolBarManager;
    }

    // Used by the callback method of IGVStageBuilder to finish setting up this UI, after Stage init is done.
    public void initializeUI() {
        // Guard to prevent repeat call
        if (headerRow != null) {
            return;
        }

        headerRow = new HeaderRow(this);
        this.setTop(headerRow.getScrollPane());
        ScrollPane trackContainerScrollPane = new ScrollPane(trackContainer);
        trackContainerScrollPane.setFitToHeight(true);
        trackContainerScrollPane.setFitToWidth(true);
        this.setCenter(trackContainerScrollPane);

        Color bgColor = PreferencesManager.getPreferences().getAsJavaFxColor(BACKGROUND_COLOR);
        Background background = new Background(new BackgroundFill(bgColor, CornerRadii.EMPTY, Insets.EMPTY));
        this.backgroundProperty().set(background);
        
        this.resetContent();
    }


    // The following should only be called within Platform.runLater() or otherwise on the Application thread
    public TrackRow addTrackRow(Track track) {
        String name = track.getName();
        TrackRow trackRow = new TrackRow(name, track, this);
        TrackScrollPane trackScrollPane = trackRow.getScrollPane();
        trackRowByName.put(name, trackRow);
        trackContainer.getChildren().add(trackScrollPane);
        return trackRow;
    }
    
    
    public DoubleProperty namePaneWidthProperty() {
        return namePaneWidthProp;
    }

    public DoubleProperty attributePaneWidthProperty() {
        return attributePaneWidthProp;
    }

    public DoubleProperty axisPaneWidthProperty() {
        return axisPaneWidthProp;
    }

    public void hideNamePanel() {
        namePaneWidthProp.set(0);
    }

    public void showNamePanel() {
        namePaneWidthProp.set(PreferencesManager.getPreferences().getAsFloat(NAME_PANEL_WIDTH));
    }

    public boolean isNamePanelHidden() {
        return namePaneWidthProp.get() <= 0;
    }

    public Collection<TrackRow> getAllTrackRows() {
        return trackRowByName.values();
    }

    public HeaderRow getHeaderRow() {
        return headerRow;
    }
    
    public void clearTracks() {
        trackContainer.getChildren().clear();
        trackRowByName.clear();
    }

    public void resetContent() {
        HeaderPaneContainer headerPaneContainer = headerRow.getContentContainer();
        headerPaneContainer.frameSpacingProperty().bind(FrameManager.frameSpacingProperty());
        FrameManager.totalDisplayWidthProperty().bind(headerPaneContainer.prefWidthProperty());

        FrameManager.computeFrameBounds();
        headerPaneContainer.createHeaderPanes();

        for (TrackRow trackRow : trackRowByName.values()) {
            DataPaneContainer dataPaneContainer = trackRow.getContentContainer();
            dataPaneContainer.frameSpacingProperty().bind(FrameManager.frameSpacingProperty());
            dataPaneContainer.createDataPanes();
        }
    }

    // Not presently used; was again anticipated for TrackRow management
    public TrackRow getTrackRow(String name) {
        TrackRow row = trackRowByName.get(name);
        if (row != null) {
            return row;
        }
        return null;
    }
    
    public void removeTrackRow(Track track) {
        String name = track.getName();
        TrackRow trackRow = trackRowByName.get(name);
        if (trackRow != null) {
            TrackScrollPane trackScrollPane = trackRow.getScrollPane();
            trackContainer.getChildren().remove(trackScrollPane);
            trackRowByName.remove(name);
        }
    }
}
