package org.igv.ui.toolbar;

import org.apache.log4j.Logger;
import org.broad.igv.event.GenomeResetEvent;
import org.broad.igv.event.IGVEventBus;
import org.broad.igv.feature.genome.GenomeListItem;
import org.broad.igv.feature.genome.GenomeManager;
import org.broad.igv.feature.genome.GenomeServerException;
import org.igv.utils.MessageUtils;
import org.broad.igv.ui.panel.FrameManager;
import org.broad.igv.ui.util.ProgressBar;
import org.broad.igv.ui.util.ProgressMonitor;
import org.igv.utils.LongRunningTask;

import javafx.application.Platform;
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

        // If we haven't changed genomes do nothing
        if (genomeListItem == null || genomeListItem.getId().equalsIgnoreCase(GenomeManager.getInstance().getGenomeId())) {
            return;
        }
        log.info("Loading " + genomeListItem.getDisplayableName());

        final Runnable runnable = new Runnable() {

            ProgressMonitor monitor;
            ProgressBar.ProgressDialog progressDialog;

            public void run() {

                if (genomeListItem != null && genomeListItem.getPath() != null) {

                    log.info("Loading " + genomeListItem.getId());

                    //User selected "more", pull up dialog and revert combo box
                    if (genomeListItem == GenomeListItem.ITEM_MORE) {
                        GenomeListManager.getInstance().loadGenomeFromServer();
                        return;
                    }

//                    LongRunningTask.submit(() -> {
//                        monitor = new ProgressMonitor();
//                        progressDialog = ProgressBar.showProgressDialog(null, "Loading Genome...", monitor, false);
//                    });

                    try {
                        GenomeManager.getInstance().loadGenome(genomeListItem.getPath(), monitor);
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
                    } finally {
                        if (progressDialog != null) {
                            Platform.runLater(() -> progressDialog.setVisible(false));
                        }
                    }

                }
            }
        };

        if (Platform.isFxApplicationThread()) {
            LongRunningTask.submit(runnable);
		} else {
		    runnable.run();
		}
    }
}
