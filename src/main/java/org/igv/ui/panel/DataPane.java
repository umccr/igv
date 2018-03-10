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

import javafx.event.EventHandler;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;

import org.apache.log4j.Logger;
import org.broad.igv.event.IGVEventBus;
import org.broad.igv.event.ViewChange;
import org.broad.igv.ui.panel.ReferenceFrame;
import org.igv.ui.IGVBackendPlaceholder;
import org.igv.ui.JavaFXUIUtilities;
import org.igv.ui.Track;
import org.igv.utils.MessageUtils;

// Intended as the rough equivalent of the DataPanel class of the Swing UI.  Work in progress.
public class DataPane extends ContentPane {

    private Track track;
    private boolean isDragging = false;

    private double viewOrigin;
    private double viewEnd;

    private static Logger log = Logger.getLogger(DataPane.class);
    
    private DataPaneContainer parent;

    public DataPane(ReferenceFrame frame, Track track, DataPaneContainer parent) {
        super(frame);
        this.track = track;
        this.parent = parent;
        init();

        JavaFXUIUtilities.bindComponentHeightToProperty(this, track.prefHeightProperty());
        
        completeInitialization();
    }

    private void init() {

//        this.setOnMouseClicked(event -> {
//            System.out.println("Event on Source: mouse click");
//            double newHeight = 100.0 + (900.0 * Math.random());
//            System.out.println("Changing track height to: " + newHeight);
//            track.prefHeightProperty().set(newHeight);
//        });
        this.setOnMouseClicked((event) -> {
            final double mouseX = event.getX();
            final int clickCount = event.getClickCount();
            double newLocation = frame.getScale() * mouseX;
            if (clickCount > 1) {
                final int newZoom = frame.getZoom() + 1;
                frame.doSetZoomCenter(newZoom, newLocation);
            } else {
                frame.centerOnLocation(newLocation);
            }

            ViewChange result = ViewChange.Result();
            result.setRecordHistory(true);
            IGVEventBus.getInstance().post(result);
        });
        
        MenuItem setTrackHeightMenuItem = new MenuItem("Set Track Height...");
        setTrackHeightMenuItem.setOnAction(event -> {
            // TODO: probably need to truncate the value fed in as default.
            String newHeightText = MessageUtils.showInputDialog("Enter Track Height", 
                    String.valueOf(track.prefHeightProperty().doubleValue()));
            System.out.println("Return value: " + newHeightText);
            try {
                double newHeight = Double.parseDouble(newHeightText);
                track.prefHeightProperty().set(newHeight);
            }
            catch (NumberFormatException nfe) {
                System.out.println(nfe);
                // Swallow, for now.
            }
        });
        MenuItem removeTrackMenuItem = new MenuItem("Remove Track");
        removeTrackMenuItem.setOnAction(event -> {
            IGVBackendPlaceholder.removeTrack(track);
        });
        ContextMenu contextMenu = new ContextMenu(setTrackHeightMenuItem, removeTrackMenuItem);
        
        this.setOnContextMenuRequested(event -> {
            contextMenu.show(this, event.getScreenX(), event.getScreenY());
            event.consume();
        });
        this.setOnMousePressed(event -> {
            contextMenu.hide();
        });

        this.setOnMouseReleased(new EventHandler <MouseEvent>()
        {
            public void handle(MouseEvent event)
            {
                System.out.println("Event on Source: mouse released");
            }
        });

        this.setOnMouseDragged(new EventHandler <MouseEvent>()
        {
            public void handle(MouseEvent event)
            {
                System.out.println("Event on Source: mouse dragged");
                render();
            }

        });

        this.setOnDragDetected(new EventHandler <MouseEvent>()
        {
            public void handle(MouseEvent event)
            {
                System.out.println("Event on Source: drag detected");
            }
        });

        // Add mouse event handlers for the target
        this.setOnMouseDragEntered(new EventHandler <MouseDragEvent>()
        {
            public void handle(MouseDragEvent event)
            {
                System.out.println("Event on Target: mouse dragged");
            }
        });

        this.setOnMouseDragOver(new EventHandler <MouseDragEvent>()
        {
            public void handle(MouseDragEvent event)
            {
                System.out.println("Event on Target: mouse drag over");
            }
        });

        this.setOnMouseDragReleased(new EventHandler <MouseDragEvent>()
        {
            public void handle(MouseDragEvent event)
            {
                System.out.println("Event on Target: mouse drag released");
            }
        });

        this.setOnMouseDragExited(new EventHandler <MouseDragEvent>()
        {
            public void handle(MouseDragEvent event)
            {
                System.out.println("Event on Target: mouse drag exited");
            }
        });


    }

    protected void render() {

        GraphicsContext gc = getCanvas().getGraphicsContext2D();

        // TODO: decide whether we *always* want to clear the canvas during render.
        gc.clearRect(0.0, 0.0, getCanvas().getWidth(), getCanvas().getHeight());
        
        if(track != null) {
            
            double dataPanelWidth = this.getPrefWidth();
            if (dataPanelWidth <= 0.0) {
                return;
            }
            
            track.draw(gc, this.frame);

        }

    }
}
