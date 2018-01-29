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

import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

import org.igv.ui.JavaFXUIUtilities;

// Intended as the rough equivalent of the IGVPanel class of the Swing UI.  Work in progress.
// Note: N, A, X, C might need to become more specific types later (e.g. RowComponent).  Pane is general enough for now.
public class IGVRow<N extends Pane, A extends Pane, X extends Pane, C extends Pane, S extends ScrollPane> extends HBox {

    private static final double INSET_SPACING = 5;

    private MainContentPane mainContentPane;
    private N namePane;
    private A attributePane;
    private X axisPane;
    private C contentContainer;
    private S scrollPane;

    protected IGVRow() {
        super(INSET_SPACING);
    }

    protected void init(MainContentPane mainContentPane, N namePane, A attributePane, X axisPane, C contentContainer, S scrollPane) {
        this.mainContentPane = mainContentPane;
        this.namePane = namePane;
        this.attributePane = attributePane;
        this.axisPane = axisPane;
        this.contentContainer = contentContainer;
        this.scrollPane = scrollPane;

        //scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
        
        JavaFXUIUtilities.bindWidthToContainer(mainContentPane, scrollPane);
        JavaFXUIUtilities.bindWidthToProperty(namePane, mainContentPane.namePaneWidthProperty());
        JavaFXUIUtilities.bindWidthToProperty(attributePane, mainContentPane.attributePaneWidthProperty());
        JavaFXUIUtilities.bindWidthToProperty(axisPane, mainContentPane.axisPaneWidthProperty());

        // The contentContainer should take the rest of the space.  That is:
        // total width - (name pane width + attr + axis pane width + (2 * insets) + scrollbar width)
        // The following is a guess on scrollbar width (30) but seems to work.
        contentContainer.prefWidthProperty().bind(this.prefWidthProperty()
                .subtract(mainContentPane.namePaneWidthProperty().add(mainContentPane.attributePaneWidthProperty().add(mainContentPane.axisPaneWidthProperty())).add(3 * INSET_SPACING + 30)));

        JavaFXUIUtilities.bindComponentHeightToOther(this, contentContainer);
        JavaFXUIUtilities.bindComponentHeightToOther(namePane, contentContainer);
        JavaFXUIUtilities.bindComponentHeightToOther(attributePane, contentContainer);
        JavaFXUIUtilities.bindComponentHeightToOther(axisPane, contentContainer);
        
        getChildren().addAll(namePane, attributePane, axisPane, contentContainer);

        JavaFXUIUtilities.bindWidthToContainer(mainContentPane, this);

        backgroundProperty().bind(mainContentPane.backgroundProperty());
        namePane.backgroundProperty().bind(mainContentPane.backgroundProperty());
        attributePane.backgroundProperty().bind(mainContentPane.backgroundProperty());
        axisPane.backgroundProperty().bind(mainContentPane.backgroundProperty());
        contentContainer.backgroundProperty().bind(mainContentPane.backgroundProperty());
    }

    public MainContentPane getMainContentPane() {
        return mainContentPane;
    }

    public N getNamePane() {
        return namePane;
    }

    public A getAttributePane() {
        return attributePane;
    }

    public X getAxisPane() {
        return axisPane;
    }

    public C getContentContainer() {
        return contentContainer;
    }

    public S getScrollPane() {
        return scrollPane;
    }
}
