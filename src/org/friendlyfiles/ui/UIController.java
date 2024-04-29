package org.friendlyfiles.ui;

import com.sun.javafx.scene.control.skin.LabeledText;
import javafx.collections.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.CacheHint;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.util.Callback;
import org.friendlyfiles.*;
import org.friendlyfiles.models.FilterStep;
import org.friendlyfiles.models.SortStep;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.*;

/**
 * Handles everything associated with the UI.
 */
public class UIController {
    public static final String fileSeparator = File.separator;

    @FXML
    private BorderPane bp_root;

    @FXML
    private ListView<FilterStep> lsv_filterStack;
    
    @FXML
    private ListView<SortStep> lsv_sortStack;

    @FXML
    private ListView<String> lsv_fileDisplay;

    @FXML
    private TextField tbx_search;
    
    @FXML
    private TextField tbx_numRowsSelected;

    @FXML
    private TreeView<String> tvw_dirTree;
    
    public BorderPane getRoot() {
    	
    	return bp_root;
    }
    
    @FXML
    void selectAllRows(ActionEvent ignoredEvent) {
    	
    	lsv_fileDisplay.getSelectionModel().selectAll();
    }

    @FXML
    void selectTopRows(ActionEvent ignoredEvent) {
    	
    	lsv_fileDisplay.getSelectionModel().clearSelection();
    	lsv_fileDisplay.getSelectionModel().selectRange(0, Integer.parseInt(tbx_numRowsSelected.getText()));
    }
    
    @FXML
    void btn_addFolder_clicked(ActionEvent ignoredEvent) {
        loadDirectory();
    }

    @FXML
    public void updateFiles(ActionEvent ignoredEvent) {
        filter.setQuery(tbx_search.getText());
        fileNames = switchboard.search(filter);
        displayFiles();
    }

    @FXML
    public void btn_delete_clicked(ActionEvent ignoredEvent) {
        switchboard.delete(lsv_fileDisplay.getSelectionModel().getSelectedItems());
        updateFiles(ignoredEvent);
    }

    @FXML
    public void btn_rename_clicked(ActionEvent ignoredEvent) {
        TextInputDialog inputDialog = new TextInputDialog();
        inputDialog.setTitle("Enter new file name:");
        inputDialog.setHeaderText("Rename Files");
        inputDialog.showAndWait().ifPresent(newName -> {
            switchboard.rename(lsv_fileDisplay.getSelectionModel().getSelectedItems(), newName);
        });
        updateFiles(ignoredEvent);
    }

    @FXML
    public void btn_move_clicked(ActionEvent ignoredEvent) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choose directory to move files to:");
        String dest = chooser.showDialog(null).getAbsolutePath();
        switchboard.move(lsv_fileDisplay.getSelectionModel().getSelectedItems(), dest);
        updateFiles(ignoredEvent);
    }

    @FXML
    void lsv_fileDisplay_clicked(MouseEvent event) {
    	
    	if (event.getClickCount() == 2 && event.getTarget().getClass() == LabeledText.class) {
    		
            String filePath = lsv_fileDisplay.getSelectionModel().getSelectedItem();
            switchboard.openFile(filePath);
        }
    }

    @FXML
    void btn_filterStackRemove_clicked(ActionEvent ignoredEvent) {
    	
    	if (selectedFilterIndex != -1) {
    		
    		filterList.remove(selectedFilterIndex);
    		
    		if (selectedFilterIndex != 0) {
    			
    			selectedFilterIndex--;
    		}
    	}
    	
    	if (filterList.isEmpty()) {
    		
    		selectedFilterIndex = -1;
    	}

        filter.resetFilterSteps(filterList);
        updateFiles(null);
    }

    @FXML
    void btn_sortStackRemove_clicked(ActionEvent ignoredEvent) {
    	
    	if (selectedSortIndex != -1) {
    		
    		sortList.remove(selectedSortIndex);
    		
    		if (selectedSortIndex != 0) {
        		
        		selectedSortIndex--;
        	}
            filter.getSortSteps().remove(selectedSortIndex);
            updateFiles(null);
    	}
    	
    	if (sortList.isEmpty()) {
    		
    		selectedSortIndex = -1;
    	}
    }

    @FXML
    private void displaySortCreateDialog() {
    	
    	SortDialog sortDialog = new SortDialog(this);
    	sortDialog.displayCreateDialog(sortList);
    	
    }
    
    private void displaySortEditDialog() {
    	
    	SortDialog sortDialog = new SortDialog(this);
    	sortDialog.displayEditDialog(sortList, selectedSortIndex);
    }
    
    public void onSortAdd(SortStep sort) {
    	
    	sortList.add(sort);
        filter.getSortSteps().add(sort);
        updateFiles(null);
    }
    
    public void onSortEdit(int index, SortStep edited) {
    	
    	sortList.remove(index);
    	sortList.add(index, edited);
    	
    	lsv_sortStack.getSelectionModel().clearAndSelect(index);

        filter.getSortSteps().remove(index);
        filter.getSortSteps().add(index, edited);
        updateFiles(null);
    }
    
    @FXML
    void displayFilterCreateDialog() {
    	
    	FilterDialog filterDialog = new FilterDialog(this);
    	filterDialog.displayCreateDialog(filterList);
    }
    
    private void displayFilterEditDialog() {
    	
    	FilterDialog filterDialog = new FilterDialog(this);
    	filterDialog.displayEditDialog(filterList, selectedFilterIndex);
    }
    
    public void onFilterAdd(FilterStep filterStep) {
    	filterList.add(filterStep);
        filter.resetFilterSteps(filterList);
        updateFiles(null);
    }
    
    public void onFilterEdit(int index, FilterStep edited) {
    	
    	filterList.remove(index);
    	filterList.add(index, edited);
    	
    	lsv_filterStack.getSelectionModel().clearAndSelect(index);

        filter.resetFilterSteps(filterList);
        updateFiles(null);
    }
    
    @FXML
    void btn_sortStackUp_clicked(ActionEvent ignoredEvent) {
    	
    	if (selectedSortIndex > 0) {
    		
			sortList.remove(selectedSortIndex);
			sortList.add(selectedSortIndex - 1, selectedSortStep);
			
			selectedSortIndex--;
    		lsv_sortStack.getSelectionModel().clearAndSelect(selectedSortIndex);
    	}
    }
    
    @FXML
    void btn_sortStackDown_clicked(ActionEvent ignoredEvent) {
    	
    	if (selectedSortIndex != -1 && selectedSortIndex < sortList.size() - 1) {
    		
    		sortList.remove(selectedSortIndex);
    		sortList.add(selectedSortIndex + 1, selectedSortStep);
    		
    		selectedSortIndex++;
    		lsv_sortStack.getSelectionModel().clearAndSelect(selectedSortIndex);
    	}
    }

    @FXML
    void lsv_filterStack_clicked(MouseEvent event) {
    	
    	if (lsv_filterStack.getSelectionModel().getSelectedIndex() == -1) {
    		
    		// If getSelectedIndex returns -1, no item is selected
    		return;
    	}
    	
    	// Get the index of the filter that was clicked
		// We can then use the index to select the filter from the list of filters below this method
        selectedFilterIndex = lsv_filterStack.getSelectionModel().getSelectedIndex();

        if (event.getClickCount() == 2) {

    		displayFilterEditDialog();
        }
    }
    
    @FXML
    void lsv_sortStack_clicked(MouseEvent event) {
    	
    	if (lsv_sortStack.getSelectionModel().getSelectedIndex() == -1) {
    		
    		// If getSelectedIndex returns -1, no item is selected
    		return;
    	}
    		
    	// Get the index of the sort criteria that was clicked
		// We can then use the index to select the individual sorting criteria from the list of criteria below this method
        selectedSortIndex = lsv_sortStack.getSelectionModel().getSelectedIndex();
        selectedSortStep = sortList.get(selectedSortIndex);

    	if (event.getClickCount() == 2) {
    		
    		displaySortEditDialog();
        }
    }
    
    private ObservableList<FilterStep> filterList;
    private int selectedFilterIndex = -1;
    private ObservableList<SortStep> sortList;
    private SortStep selectedSortStep = null;
    private int selectedSortIndex = -1;

    private Dialog<Object> waitingForSwapDialog = null;
    private boolean waitingForSwap = false;

    private Switchboard switchboard;

    // We park the stream of file names here so that multiple functions can consume it, or part of it.
    private Stream<String> fileNames;

    private final QueryFilter filter = new QueryFilter();

    public void initialize() {
        // Enable caching of the file display panel
        lsv_fileDisplay.setCache(true);
        lsv_fileDisplay.setCacheHint(CacheHint.SPEED);
        
        // Set up file listview selection
        lsv_fileDisplay.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        
        // Ensure only numbers are entered into the "select top n" text box
        tbx_numRowsSelected.textProperty().addListener((obs, oldVal, newVal) -> {

			if (!newVal.matches("\\d*")) {
				tbx_numRowsSelected.setText(newVal.replaceAll("[^\\d]", ""));
			}
		});
        
        filterList = FXCollections.observableList(new ArrayList<>());
        sortList = FXCollections.observableArrayList(new ArrayList<>());
        
        lsv_filterStack.setItems(filterList);
        lsv_sortStack.setItems(sortList);
        
        // OnClick moved to FX event (above)

        // For future reference, much of the following cellfactory instantiation code was based off of the following resource:
        // https://stackoverflow.com/a/39466520

        //Set the cell factory for the tree view in order to add checkbox functionality and allow for more control over the naming of the individual cells
        tvw_dirTree.setCellFactory(new Callback<TreeView<String>, TreeCell<String>>() {
            @Override
            public CheckBoxTreeCell<String> call(TreeView<String> p) {
                return new DirectoryTreeCell();
            }
        });
        
        // Set cell factory for the filter stack list view in order to show only the names of our custom filters
        // Ensure the filter item has a getName() property or some other way to access text that identifies the filter to the user
        lsv_filterStack.setCellFactory(cell -> new ListCell<FilterStep>() {
        	
        	@Override
        	protected void updateItem(FilterStep item, boolean empty) {
        		super.updateItem(item, empty);
        		
        		if (item == null || empty) {
        			
        			setText(null);
        		}
        		else {
        			
        			setText(item.getName());
        		}
        	}
        });
        
        
        // Set cell factory for the sort stack list view in order to show only the names of our custom sorting criteria
        // Ensure the sort item has a getName() property or some other way to access text that identifies the sorting criteria to the user
        lsv_sortStack.setCellFactory(cell -> new ListCell<SortStep>() {
        	
        	@Override
        	protected void updateItem(SortStep item, boolean empty) {
        		super.updateItem(item, empty);
        		
        		if (item == null || empty) {
        			
        			setText(null);
        		}
        		else {
        			
        			setText(item.getName());
        		}
        	}
        });

        Path dbPath = Paths.get("FriendlyFilesDatabase");
        if (Files.exists(dbPath)) {
            try {
                PostingList backend = PostingList.deserializeFrom(dbPath);
                switchboard = new Switchboard(this, backend, new FileSource());
            } catch (Exception e) {
                // If we can't open the database file, we just start making a new one.
                switchboard = new Switchboard(this, new PostingList(dbPath), new FileSource());
                waitingForSwap = true;
            }
        } else {
            switchboard = new Switchboard(this, new PostingList(dbPath), new FileSource());
            waitingForSwap = true;
        }
    }
    
    /**
     * Prompts the user for a root directory and loads its files into the UI.
     */
    public void loadDirectory() {
        if (waitingForSwap) showWaitingForSwapDialog();
    	DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Add search root:");
        try {
            String topDirectory = chooser.showDialog(null).getAbsolutePath();
            boolean directoryAlreadyAccessible = switchboard.addRootToFilter(topDirectory, filter);
            if (directoryAlreadyAccessible) return;

            fileNames = switchboard.search(filter);
            displayFiles();
            updateDirTree();
        } catch (NullPointerException ignored) {}
    }

    /**
     * Shuts down the items controlled by the switchboard.
     */
    public void shutDown() {
        switchboard.shutDown();
    }

    /**
     * Updates the Directory treeview using the provided list of top-level directories.<br>
     * Note: This method can cause issues if called while the UI is still setting up; at the earliest it should be called towards the end of the initialize() method.
     */
    public void updateDirTree() {
        List<String> directories = switchboard.getDirectories(filter).sorted().collect(Collectors.toList());

        // Set the treeview's root directory to a new Directory item with no path
        DirectoryTreeItem treeRoot = new DirectoryTreeItem(null);
        tvw_dirTree.setRoot(treeRoot);

        filter.getRoots().forEach(root -> {
            // Add the current root to the base of the treeview
            DirectoryTreeItem dirRootItem = new DirectoryTreeItem(root);
            dirRootItem.setIndependent(true);
            dirRootItem.addCheckListener(this);
            treeRoot.getChildren().add(dirRootItem);

            directories.stream()
                    .filter(dirPath -> dirPath.startsWith(root))
                    .filter(dirPath -> dirPath.length() != root.length())
                    .map(dirPath -> dirPath.substring(root.length()))
                    .forEach(dirPath -> dirRootItem.addAllChildren(this, dirPath));
        });
    }

    /**
     * Display the list of files to the user.
     * <p>
     * This function is separated from the `displayFiles` instance method to allow for swapping out the backend
     * after startup.
     */
    public void displayFiles() {

        // Clear previous file panes before filling in with new data
        lsv_fileDisplay.getItems().clear();

        if (fileNames != null) {
            lsv_fileDisplay.getItems().addAll(fileNames.collect(Collectors.toList()));
        }
    }

    /**
     * Closes the waiting dialog and populates the main window with files.
     */
    public void notifyBackendSwapCompleted() {
        if (!waitingForSwap) {
            fileNames = switchboard.search(filter);
            displayFiles();
        } else {
            waitingForSwap = false;
        }
        if (waitingForSwapDialog != null) {
            waitingForSwapDialog.close();
            waitingForSwapDialog.getDialogPane().getScene().getWindow().hide();
        }
    }

    /**
     * Creates and shows the user a dialog when there is no available backend.
     */
    private void showWaitingForSwapDialog() {
        waitingForSwapDialog = new Dialog<>();
        waitingForSwapDialog.setHeaderText("One moment, please...");
        waitingForSwapDialog.setContentText("We are indexing your file system.");
        waitingForSwapDialog.showAndWait();
    }

    /**
     * Shows the user an error dialog.
     * @param contentText the message to show the user
     */
    public void showErrorDialog(String contentText) {
        Alert errorDialog = new Alert(Alert.AlertType.ERROR, contentText);
        errorDialog.showAndWait();
    }

    /**
     * Makes a subtree of files invisible in the main listing.
     * @param dirPath the top of the subtree to disallow
     */
    public void disallowAllFilesInDirectory(String dirPath) {
        fileNames = switchboard.disallowFilesInDirectory(filter, dirPath);
        displayFiles();
    }

    /**
     * Toggles the visiility of a subtree of files in the main listing.
     * @param dirPath the top of the subtree to toggle
     */
    public void toggleFilesInDirectory(String dirPath) {
        fileNames = switchboard.toggleVisibleFiles(filter, dirPath);
        displayFiles();
    }

    /**
     * A private class defining how the checkbox tree cells should be named.<br>
     * In this case we specify that it should using the lowermost directory name rather than the entire path to the file<br>
     * Cell Factory implementation is further detailed on Oracle's tree view documentation:<br>
     * <a href="https://docs.oracle.com/javafx/2/ui_controls/tree-view.htm">https://docs.oracle.com/javafx/2/ui_controls/tree-view.htm</a>
     */
    static final class DirectoryTreeCell extends CheckBoxTreeCell<String> {

        // Do not edit this method to change cell naming, the majority of the code here is recommended by the JavaFX developers
        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                setText(getString());
            }
        }

        // Update this method to change how the tree cells are named
        private String getString() {
            return getItem() == null ? "" : getItem();
        }
    }
}
