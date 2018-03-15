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

package org.igv.ui.toolbar;

import org.apache.log4j.Logger;
import org.broad.igv.feature.genome.GenomeListItem;
import org.broad.igv.feature.genome.GenomeManager;
import org.broad.igv.feature.genome.GenomeServerException;
import org.igv.utils.LongRunningTask;
import org.igv.ui.utils.MessageUtils;
import org.igv.ui.utils.ProgressBarDialog;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by jrobinso on 7/6/17.
 * @author eby rewritten for JavaFX
 */
public class GenomeComboBox extends ComboBox<GenomeListItem> {

    private static Logger log = Logger.getLogger(GenomeComboBox.class);

    public GenomeComboBox() {
        setOnAction((event) -> {
            GenomeListItem genomeListItem = getSelectionModel().getSelectedItem();
            loadGenomeListItem(genomeListItem);
        });
    }

    // Should be able to replace this with binding to an ObjectProperty<Genome>
    public void refreshGenomeList() {
        Platform.runLater(() -> {
            Collection<GenomeListItem> genomes;
	        try {
	            genomes = GenomeListManager.getInstance().getGenomeItemMap().values();
	        } catch (IOException e) {
	            log.error("Error reading genome list ", e);
	            genomes = new ArrayList<GenomeListItem>();
	            MessageUtils.showErrorMessage("Error reading genome list ", e);
	        }
	        
	        List<GenomeListItem> genomeList = new ArrayList<GenomeListItem>(genomes);
	        genomeList.sort((o1, o2) -> o1.getDisplayableName().compareTo(o2.getDisplayableName()));
	        genomeList.add(GenomeListItem.ITEM_MORE);
	
	        setCellFactory(new Callback<ListView<GenomeListItem>, ListCell<GenomeListItem>>() {
	
	            @Override
	            public ListCell<GenomeListItem> call(ListView<GenomeListItem> param) {
	                return new ListCell<GenomeListItem>() {
	
	                    @Override
	                    protected void updateItem(GenomeListItem item, boolean empty) {
	                        super.updateItem(item, empty);
	                        
	                        if (empty || item == null) {
	                            setText(null);
	                            setGraphic(null);
	                        } else {
	                            setText(item.getDisplayableName());
	                        }
	                    }
	                };
	            }
	        });
	        
	        getItems().clear();
	        getItems().addAll(genomeList);
	
	        String curId = GenomeManager.getInstance().getGenomeId();
	        GenomeListItem item = GenomeListManager.getInstance().getLoadedGenomeListItemById(curId);
	        if (item != null) getSelectionModel().select(item);
        });
    }

    private void loadGenomeListItem(final GenomeListItem genomeListItem) {

        log.info("Enter genome combo box");

        // If we haven't changed genomes do nothing (also check for valid selection)
        if (genomeListItem == null ||  genomeListItem.getPath() == null ||
                genomeListItem.getId().equalsIgnoreCase(GenomeManager.getInstance().getGenomeId())) {
            return;
        }

        //User selected "more", pull up dialog and revert combo box
        if (genomeListItem == GenomeListItem.ITEM_MORE) {
            GenomeListManager.getInstance().loadGenomeFromServer();
            return;
        }

        log.info("Loading " + genomeListItem.getDisplayableName());

        final Task<Void> genomeLoadTask = new Task<Void>() {
            public Void call() {
                log.info("Loading " + genomeListItem.getId());

                try {
                    GenomeManager.getInstance().loadGenome(genomeListItem.getPath(), null);
                } catch (GenomeServerException e) {
                    log.error("Error loading genome: " + genomeListItem.getId() + "  " + genomeListItem.getPath(), e);
                    MessageUtils.showMessage("Error loading genome: " + genomeListItem.getDisplayableName());
                } catch (Exception e) {
                    log.error(e);
                    boolean ok = MessageUtils.confirm("The genome [" + genomeListItem.getId() +
                                    "] could not be read. Would you like to remove the selected entry?");

                    if (ok) {
                        GenomeListManager.getInstance().removeGenomeListItem(genomeListItem);
                        refreshGenomeList();
                        log.error("Error initializing genome", e);
                    }
                }
                
                return null;
            }
        };
        
        ProgressBarDialog<Void> progressBarDialog = new ProgressBarDialog<>("Loading Genome...", genomeLoadTask);
        progressBarDialog.show();
        LongRunningTask.submit(genomeLoadTask);
    }
}
