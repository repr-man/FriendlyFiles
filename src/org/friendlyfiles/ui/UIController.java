package org.friendlyfiles.ui;

import com.sun.javafx.scene.control.skin.LabeledText;
import javafx.collections.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.friendlyfiles.*;
import org.friendlyfiles.SortStep.OrderType;
import org.friendlyfiles.SortStep.SortType;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
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
    private Button btn_addFolder;

    @FXML
    private Button btn_addFilter;

    @FXML
    private Button btn_sortStackAdd;

    @FXML
    private Button btn_sortStackRemove;
    
    @FXML
    private Button btn_sortStackUp;
    
    @FXML
    private Button btn_filterStackAdd;
    
    @FXML
    private Button btn_filterStackRemove;

    @FXML
    private Button btn_search;

    @FXML
    private Button btn_delete;

    @FXML
    private Button btn_rename;

    @FXML
    private Button btn_move;

    @FXML
    private ListView<FilterStep> lsv_filterStack;
    
    @FXML
    private ListView<SortStep> lsv_sortStack;

    @FXML
    private ListView<String> lsv_fileDisplay;

    @FXML
    private Tab tab_db;

    @FXML
    private Tab tab_filter;

    @FXML
    private Tab tab_query;

    @FXML
    private Tab tab_sort;
    
    @FXML
    private Tab tab_home;

    @FXML
    private TextField tbx_search;

    @FXML
    private TreeView<String> tvw_dirTree;
    
    @FXML
    void btn_addFolder_clicked(ActionEvent event) {
        loadDirectory();
    }

    @FXML
    public void btn_search_clicked(ActionEvent ignoredEvent) {
        filter.setQuery(tbx_search.getText());
        fileNames = switchboard.search(filter);
        updateFiles();
    }

    @FXML
    public void btn_delete_clicked(ActionEvent ignoredEvent) {
        switchboard.delete(lsv_fileDisplay.getSelectionModel().getSelectedItems());
        btn_search_clicked(ignoredEvent);
    }

    @FXML
    public void btn_rename_clicked(ActionEvent ignoredEvent) {
        TextInputDialog inputDialog = new TextInputDialog();
        inputDialog.setTitle("Enter new file name:");
        inputDialog.setHeaderText("Rename Files");
        inputDialog.showAndWait().ifPresent(newName -> {
            switchboard.rename(lsv_fileDisplay.getSelectionModel().getSelectedItems(), newName);
        });
        btn_search_clicked(ignoredEvent);
    }

    @FXML
    public void btn_move_clicked(ActionEvent ignoredEvent) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choose directory to move files to:");
        String dest = chooser.showDialog(null).getAbsolutePath();
        switchboard.move(lsv_fileDisplay.getSelectionModel().getSelectedItems(), dest);
        btn_search_clicked(ignoredEvent);
    }

    @FXML
    void btn_addFilter_clicked(ActionEvent event) {
    	
    	// TODO: Create popup to allow a new filter to be configured and added to the filterList
    }
    
    @FXML
    void btn_addSort_clicked(ActionEvent event) {
    	
    	displaySortCreateDialog();
    }
    
    @FXML
    void lsv_fileDisplay_clicked(MouseEvent event) {
    	
    	if (event.getClickCount() == 2 && event.getTarget().getClass() == LabeledText.class) {
    		
            String filePath = lsv_fileDisplay.getSelectionModel().getSelectedItem();
            switchboard.openFile(filePath);
        }
    }
    
    @FXML
    void btn_filterStackAdd_clicked(ActionEvent event) {
    	
    	filterList.add(new FilterStep("Filter" + lsv_filterStack.getItems().size(), FilterStep.FilterType.NAME));
    }

    @FXML
    void btn_filterStackRemove_clicked(ActionEvent event) {
    	
    	if (selectedFilterIndex != -1) {
    		
    		filterList.remove(selectedFilterIndex);
    		
    		if (selectedFilterIndex != 0) {
    			
    			selectedFilterIndex--;
    		}
    	}
    	
    	if (filterList.size() == 0) {
    		
    		selectedFilterIndex = -1;
    	}
    }
    
    @FXML
    void btn_sortStackAdd_clicked(ActionEvent event) {
    	
    	displaySortCreateDialog();
    }
    
    private void displaySortCreateDialog() {
    	
    	// Creata a new stage with the primary stage as the owner
    	final Stage sortDialog = new Stage();
    	sortDialog.setTitle("Sort Builder");
    	sortDialog.initModality(Modality.APPLICATION_MODAL);
    	sortDialog.initOwner(bp_root.getScene().getWindow());
    	
    	// Create VBox for the root element of the scene
    	VBox sortScreen = new VBox(8);
    	sortScreen.setAlignment(Pos.TOP_CENTER);
    	
    	// Add name field
    	sortScreen.getChildren().add(new Text("Name"));
    	TextField txt_sortName = new TextField();
    	txt_sortName.setMaxWidth(150);;
    	sortScreen.getChildren().add(txt_sortName);
    	
    	// Add selection drop downs
    	sortScreen.getChildren().add(new Text("Sorting Type"));
    	ArrayList<String> sortOptions = new ArrayList<String>(Arrays.asList(SortStep.getTypeNames()));
    	ComboBox<String> cbx_sortTypes = new ComboBox<String>((FXCollections.observableArrayList(sortOptions)));
    	sortScreen.getChildren().add(cbx_sortTypes);
    	
    	sortScreen.getChildren().add(new Text("Sort Order"));
    	ArrayList<String> orderOptions = new ArrayList<String>(Arrays.asList(SortStep.getOrderNames()));
    	ComboBox<String> cbx_order = new ComboBox<String>((FXCollections.observableArrayList(orderOptions)));
    	sortScreen.getChildren().add(cbx_order);
    	
    	// Add buttons
    	Button createButton = new Button("Add");
    	Button exitButton = new Button("Cancel");
    	HBox buttons = new HBox(20);
    	VBox.setMargin(buttons, new Insets(50, 0, 0, 0));
    	buttons.setAlignment(Pos.TOP_CENTER);
    	buttons.getChildren().addAll(createButton, exitButton);
    	sortScreen.getChildren().add(buttons);
    	
    	// Set up button actions
    	createButton.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				
				String name = txt_sortName.getText();
				int typeIndex = cbx_sortTypes.getSelectionModel().getSelectedIndex();
				int orderIndex = cbx_order.getSelectionModel().getSelectedIndex();
				
				if (name.trim() != "" && typeIndex >= 0 && orderIndex >= 0) {
					
					SortStep step = new SortStep(name, SortType.values()[typeIndex], OrderType.values()[orderIndex]);
					onSortAdd(step);
					
					sortDialog.close();
				}
				else {
					
					Alert alert = new Alert(AlertType.INFORMATION);
					alert.setHeaderText("The provided sort could not be added.");
					alert.setContentText("Please ensure all sorting information is set correctly, or press cancel");
					
					alert.showAndWait();
				}
			}
		});
    	
    	exitButton.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				
				sortDialog.close();
			}
		});
    	
    	
    	// Create a scene using the VBox and set it as the root element
    	Scene sortDialogScene = new Scene(sortScreen, 300, 260);
    	sortDialog.setScene(sortDialogScene);
    	sortDialog.show();
    }
    
    private void displaySortEditDialog(int stepIndex) {
    	
    	SortStep step = sortList.get(selectedSortIndex);
    	
    	// Creata a new stage with the primary stage as the owner
    	final Stage sortDialog = new Stage();
    	sortDialog.setTitle("Sort Editor");
    	sortDialog.initModality(Modality.APPLICATION_MODAL);
    	sortDialog.initOwner(bp_root.getScene().getWindow());
    	
    	// Create VBox for the root element of the scene
    	VBox sortScreen = new VBox(8);
    	sortScreen.setAlignment(Pos.TOP_CENTER);
    	
    	// Add name field
    	sortScreen.getChildren().add(new Text("Name"));
    	TextField txt_sortName = new TextField(step.getName());
    	txt_sortName.setMaxWidth(150);;
    	sortScreen.getChildren().add(txt_sortName);
    	
    	// Add selection drop downs
    	sortScreen.getChildren().add(new Text("Sorting Type"));
    	ArrayList<String> sortOptions = new ArrayList<String>(Arrays.asList(SortStep.getTypeNames()));
    	ComboBox<String> cbx_sortTypes = new ComboBox<String>((FXCollections.observableArrayList(sortOptions)));
    	cbx_sortTypes.getSelectionModel().clearAndSelect(step.getType().ordinal());
    	sortScreen.getChildren().add(cbx_sortTypes);
    	
    	sortScreen.getChildren().add(new Text("Sort Order"));
    	ArrayList<String> orderOptions = new ArrayList<String>(Arrays.asList(SortStep.getOrderNames()));
    	ComboBox<String> cbx_order = new ComboBox<String>((FXCollections.observableArrayList(orderOptions)));
    	cbx_order.getSelectionModel().clearAndSelect(step.getOrder().ordinal());
    	sortScreen.getChildren().add(cbx_order);
    	
    	// Add buttons
    	Button createButton = new Button("Apply Changes");
    	Button exitButton = new Button("Cancel");
    	HBox buttons = new HBox(20);
    	VBox.setMargin(buttons, new Insets(50, 0, 0, 0));
    	buttons.setAlignment(Pos.TOP_CENTER);
    	buttons.getChildren().addAll(createButton, exitButton);
    	sortScreen.getChildren().add(buttons);
    	
    	// Set up button actions
    	createButton.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				
				String name = txt_sortName.getText();
				int typeIndex = cbx_sortTypes.getSelectionModel().getSelectedIndex();
				int orderIndex = cbx_order.getSelectionModel().getSelectedIndex();
				
				if (name.trim() != "" && typeIndex >= 0 && orderIndex >= 0) {
					
					SortStep step = new SortStep(name, SortType.values()[typeIndex], OrderType.values()[orderIndex]);
					onSortEdit(selectedSortIndex, step);
					
					sortDialog.close();
				}
				else {
					
					Alert alert = new Alert(AlertType.INFORMATION);
					alert.setHeaderText("The edits could not be applied.");
					alert.setContentText("Please ensure all sorting information is set correctly, or press cancel");
					
					alert.showAndWait();
				}
			}
		});
    	
    	exitButton.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				
				sortDialog.close();
			}
		});
    	
    	
    	// Create a scene using the VBox and set it as the root element
    	Scene sortDialogScene = new Scene(sortScreen, 300, 260);
    	sortDialog.setScene(sortDialogScene);
    	sortDialog.show();
    }
    
    private void onSortAdd(SortStep sort) {
    	
    	sortList.add(sort);
    }
    
    private void onSortEdit(int index, SortStep edited) {
    	
    	sortList.remove(index);
    	sortList.add(index, edited);
    	
    	lsv_sortStack.getSelectionModel().clearAndSelect(index);
    }

    @FXML
    void btn_sortStackRemove_clicked(ActionEvent event) {
    	
    	if (selectedSortIndex != -1) {
    		
    		sortList.remove(selectedSortIndex);
    		
    		if (selectedSortIndex != 0) {
        		
        		selectedSortIndex--;
        	}
    	}
    	
    	if (sortList.size() == 0) {
    		
    		selectedSortIndex = -1;
    	}
    }
    
    @FXML
    void btn_sortStackUp_clicked(ActionEvent event) {
    	
    	if (selectedSortIndex > 0) {
    		
			sortList.remove(selectedSortIndex);
			sortList.add(selectedSortIndex - 1, selectedSortStep);
			
			selectedSortIndex--;
    		lsv_sortStack.getSelectionModel().clearAndSelect(selectedSortIndex);
    	}
    }
    
    @FXML
    void btn_sortStackDown_clicked(ActionEvent event) {
    	
    	// TODO: Implement actual code here (this code is completely untested and doesn't update the listview)
    	
    	// Demo
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
        selectedFilterStep = filterList.get(selectedFilterIndex);
    	
    	if (event.getClickCount() == 2) {

    		// TODO: Double click to open edit window
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
    		
    		displaySortEditDialog(selectedSortIndex);
        }
    }
    
    /** TODO: Custom sorting/filtering objects to specify the layout/order in which to process filters and sorting steps.
     * (individual sorts/filters could either be applied individually or read in by the master QueryFilter/Sorting algorithm to compile and execute one large query?)
     */
    private ObservableList<FilterStep> filterList;
    private FilterStep selectedFilterStep = null;
    private int selectedFilterIndex = -1;
    private ObservableList<SortStep> sortList;
    private SortStep selectedSortStep = null;
    private int selectedSortIndex = -1;

    private Dialog<Object> waitingForSwapDialog = null;

    private Switchboard switchboard;

    // We park the stream of file names here so that multiple functions can consume it, or part of it.
    private Stream<String> fileNames;

    private final QueryFilter filter = new QueryFilter();

    private final ObservableSet<DirectoryTreeItem> checkedDirItems = FXCollections.observableSet();

    public void initialize() {
        // Enable caching of the file display panel
        lsv_fileDisplay.setCache(true);
        lsv_fileDisplay.setCacheHint(CacheHint.SPEED);
        
        // Set up file listview selection
        lsv_fileDisplay.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        
        filterList = FXCollections.observableList(new ArrayList<FilterStep>());
        sortList = FXCollections.observableArrayList(new ArrayList<SortStep>());
        
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
        // TODO: Implement the filter item that will be added to this list
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
        // TODO: Implement the sort item that will be added to this list
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
                showWaitingForSwapDialog();
            }
        } else {
            switchboard = new Switchboard(this, new PostingList(dbPath), new FileSource());
            showWaitingForSwapDialog();
        }
    }
    
    /**
     * Prompts the user for a root directory and loads its files into the UI.
     */
    public void loadDirectory() {
    	DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Add search root:");
        try {
            String topDirectory = chooser.showDialog(null).getAbsolutePath();
            String result = filter.addRoot(topDirectory);
            if (result != null) {
                showErrorDialog(String.format("Already able to access directory `%s` through another root.", result));
                return;
            }
            fileNames = switchboard.search(filter);
            updateFiles();
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
     * Update the list of files.
     */
    // TODO: Do we need to delete this?
    public void updateFiles() {
        displayFiles();
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

        filter.getRoots().parallelStream().forEach(root -> {
            // Add the current root to the base of the treeview
            DirectoryTreeItem dirRootItem = new DirectoryTreeItem(root);
            dirRootItem.setIndependent(true);
            dirRootItem.addCheckListener(this);
            treeRoot.getChildren().add(dirRootItem);

            directories.stream()//.parallel()
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
        if (waitingForSwapDialog != null) {
            fileNames = switchboard.search(filter);
            updateFiles();
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
        waitingForSwapDialog.show();
    }

    /**
     * Shows the user an error dialog.
     *
     * @param contentText the message to show the user
     */
    public void showErrorDialog(String contentText) {
        Alert errorDialog = new Alert(Alert.AlertType.ERROR, contentText);
        errorDialog.showAndWait();
    }

    public void disallowAllFilesInDirectory(String dirPath) {
        fileNames = switchboard.disallowFilesInDirectory(filter, dirPath);
        updateFiles();
    }

    public void toggleFilesInDirectory(String dirPath) {
        fileNames = switchboard.toggleVisibleFiles(filter, dirPath);
        updateFiles();
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
