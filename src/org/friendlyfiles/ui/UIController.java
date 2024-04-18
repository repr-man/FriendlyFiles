package org.friendlyfiles.ui;

import com.sun.javafx.scene.control.skin.LabeledText;
import javafx.beans.value.*;
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

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.*;

public class UIController {

	@FXML
    private BorderPane bp_root;

    @FXML
    private Button btn_addFilter;

    @FXML
    private Button btn_filterStackDown;

    @FXML
    private Button btn_filterStackUp;

    @FXML
    private Button btn_search;

    @FXML
    private ListView<String> lsv_filterStack;

    @FXML
    private ListView<String> lv_fileDisplay;

    @FXML
    private Tab tab_db;

    @FXML
    private Tab tab_filter;

    @FXML
    private Tab tab_query;

    @FXML
    private Tab tab_sort;

    @FXML
    private TextField tbx_search;

    @FXML
    private TreeView<String> tvw_dirTree;

    @FXML
    public void btn_search_click(ActionEvent ignoredEvent) {
    	
        fileNames = switchboard.search(tbx_search.getText(), filter);
        updateFiles();
    }
    
    @FXML
    void btn_addFilter_clicked(ActionEvent event) {
    	
    	// TODO: Create popup to allow a new filter to be configured and added to the filterList
    }
    
    @FXML
    void lv_fileDisplay_clicked(MouseEvent event) {
    	
    	if (event.getClickCount() == 2 && event.getTarget().getClass() == LabeledText.class) {
    		
            String filePath = lv_fileDisplay.getSelectionModel().getSelectedItem();
            switchboard.openFile(filePath);
        }
    }
    
    @FXML
    void btn_filterStackUp_clicked(ActionEvent event) {
    	
    	// TODO: Implement actual code here (this code is completely untested and doesn't update the listview)
    	
    	// Demo
//    	if (selectedFilterIndex > 0) {
//    		
//			filterList.remove(selectedFilterIndex);
//			filterList.add(selectedFilterIndex - 1, selectedFilter);
//			
//    	}
    }
    
    @FXML
    void btn_filterStackDown_clicked(ActionEvent event) {
    	
    	// TODO: Implement actual code here (this code is completely untested and doesn't update the listview)
    	
    	// Demo
//    	if (selectedFilterIndex != -1 && selectedFilterIndex < filterList.size() - 1) {
//    		
//    		filterList.remove(selectedFilterIndex);
//    		filterList.add(selectedFilterIndex + 1, selectedFilter);
//    		
//    	}
    }

    @FXML
    void lsv_filterStack_clicked(MouseEvent event) {

    	if (event.getClickCount() == 2 && event.getTarget().getClass() == LabeledText.class) {

    		// Get the index of the filter that was clicked
    		// We can then use the index to select the filter from the list of filters below this method
//            selectedFilterIndex = lv_fileDisplay.getSelectionModel().getSelectedIndex();
//            selectedFilter = filterList.get(selectedFilterIndex);
        }
    	else {

    		// selectedFilterIndex = -1;
    	}
    }
    
    /** TODO: Custom filter objects to specify each individual filtering step. OurFilter is simply a placeholder name.
     * (individual filters could be applied individually or read in by the master QueryFilter to compile and execute one large query?)
     */
//    private ObservableList<OurFilter> filterList = FXCollections.observableList(new ArrayList<OurFilter>());
//    private OurFilter selectedFilter = null;
//    private int selectedFilterIndex = -1;

    private Dialog<Object> waitingForSwapDialog = null;

    private Switchboard switchboard;

    // We park the stream of file names here so that multiple functions can consume it, or part of it.
    private Stream<String> fileNames;

    private final QueryFilter filter = new QueryFilter();

    public void initialize() {

        // Enable caching of the file display panel
        lv_fileDisplay.setCache(true);
        lv_fileDisplay.setCacheHint(CacheHint.SPEED);
        
        // Set up file listview selection
        lv_fileDisplay.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        
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
//        lsv_filterStack.setCellFactory(cell -> new ListCell<OurFilter>() {
//        	
//        	@Override
//        	protected void updateItem(OurFilter item, boolean empty) {
//        		super.updateItem(item, empty);
//        		
//        		if (item == null || empty) {
//        			
//        			setText(null);
//        		}
//        		else {
//        			
//        			setText(item.getName());
//        		}
//        	}
//        });

        Path dbPath = Paths.get("FriendlyFilesDatabase");
        if (Files.exists(dbPath)) {
            try {
                Backend backend = PostingList.deserializeFrom(dbPath);
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
        
        // Prompt the user to load a directory
        // Possibly remove this from the initialize method and have the user manually click an "add folder" button each time?
        loadDirectory();
    }
    
    /**
     * Load the selected directory and its files into the application
     */
    public void loadDirectory() {
    	DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Add search root:");
        try {
            String topDirectory = chooser.showDialog(null).getAbsolutePath();
            filter.addRoot(topDirectory);
            fileNames = switchboard.search(filter);
            updateFiles();
        } catch (NullPointerException ignored) {}
    }

    /**
     * Shuts down the items controlled by the switchboard.
     */
    public void shutDown() {
        switchboard.shutDown();
    }

    /**
     * Update the list of file keys stored by the UI. The order of the keys in this list corresponds to the order in which they will display to the user.<br>
     * Additionally, this method will immediately display these files to the user.
     */
    public void updateFiles() {
        displayFiles();
    }

    /**
     * Adds a listener to the given item which will fire whenever the item is checked/unchecked.
     *
     * @param i the tree item to add the listener to
     */
    private void addCheckListenerToItem(DirectoryTreeItem i) {

        // Check all boxes and add them to the set initially
        i.selectedProperty().set(true);
        checkedDirItems.add(i);

        i.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {

                // Prevents any issues with the listener being triggered without any change occuring (should never happen)
                if (newValue != oldValue) {

                    if (newValue) {

                        // Add the directory item to the selected set
                        checkedDirItems.add(i);
                    } else {

                        // Remove the directory item from the selected set
                        checkedDirItems.remove(i);
                    }
                }
            }
        });
    }

    // Set of all the selected directory items
    // Since each item stores the full path of the directory it refers to, implementing a directory inclusion/exclusion toggle shouldn't be the most difficult thing in the world
    // TODO: Probably turn this into a `RoaringBitmap`...
    private final ObservableSet<DirectoryTreeItem> checkedDirItems = FXCollections.observableSet();

    /**
     * Updates the Directory treeview using the provided list of top-level directories.<br>
     * Note: This method can cause issues if called while the UI is still setting up; at the earliest it should be called towards the end of the initialize() method.
     *
     * @param rootDirs the list of directories to treat as the "root" or top-level directories. These directories and their subdirectories will be loaded into the treeview.
     */
    public void updateDirTree(List<String> rootDirs) {

        // Set the treeview's root directory to a new Directory item with no path
        DirectoryTreeItem treeRoot = new DirectoryTreeItem(null);
        treeRoot.setIndependent(true);
        tvw_dirTree.setRoot(treeRoot);

        // For each relative directory root, walk through its entire logical directory tree, and add items to the visual tree view as those logical directories are visited
        for (String dirRoot : rootDirs) {

            // Add the current root to the base of the treeview
            DirectoryTreeItem dirRootItem = new DirectoryTreeItem(dirRoot);
            dirRootItem.setIndependent(true);
            addCheckListenerToItem(dirRootItem);
            treeRoot.getChildren().add(dirRootItem);

            // Walk through subdirectories and add them as children
            try {
                Files.walkFileTree(Paths.get(dirRoot).toAbsolutePath(), new SimpleFileVisitor<Path>() {

                    @Override
                    public FileVisitResult preVisitDirectory(Path file, BasicFileAttributes attrs) throws IOException {

                        if (!file.toString().equals(dirRoot)) {

                            // Create a new directory item within the visual tree according to its path relative to the root
                            createDirItemFromPath(file.toString(), dirRootItem);
                        }


                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {

                e.printStackTrace();
            }
        }
    }

    /**
     * Method used by updateRootDirectories to attach subdirectories to the roots, no use for this to be called elsewhere.<br>
     * Loops through from the "root"/top-level directory down to the target path, allowing a new tree item(s) to be created in the correct location within the treeview.
     *
     * @param path the path of the target directory
     * @param root the tree item holding the target directory's relative root directory
     */
    private void createDirItemFromPath(String path, DirectoryTreeItem root) {

        // Get offset/distance of the root directory (for this specific tree of directories) from the filesystem root
        int rootOffset = Paths.get(root.getValue()).getNameCount();

        // Get number of directories between this file tree's relative root and the inner target directory
        int targetDepth = Paths.get(path).getNameCount() - rootOffset;

        // Prime the "current item" to be the root of the TreeList
        DirectoryTreeItem currItem = root;

        /*
         *
         * Note: [path].getName(int index) returns the path name with i as the distance from the root
         * The root element can be thought to have index -1, the root's child is 0, and so on
         */

        for (int i = 0; i < targetDepth; i++) {

            try {

                // Drill down into the file path, starting from the child of the relative root directory, down to the target
                // The loop will only happen once if the target is the child of the relative root directory
                String subPath = String.format("%s%s", Paths.get(path).getRoot(), Paths.get(path).subpath(0, rootOffset + i + 1));

                // Use the subpath information to navigate through and create new children within the existing tree view items

                // Boolean to determine if the current item contains the requested child directory
                boolean containsChild = false;

                // Loop over the children of the current item
                for (TreeItem<String> child : currItem.getChildren()) {

                    // Grab a reference to the current child as a DirectoryTreeItem
                    DirectoryTreeItem childDir = (DirectoryTreeItem) child;

                    // If the child's path is equal to the path of the directory item we are requesting,
                    // make that child the new current item
                    if (Files.isSameFile(Paths.get(childDir.getValue()).toAbsolutePath(), Paths.get(subPath).toAbsolutePath())) {

                        containsChild = true;
                        currItem = childDir;
                        break;
                    }
                }

                // If the child was not found in the current item, create/add it and make it the new current item
                if (!containsChild) {

                    DirectoryTreeItem newChild = new DirectoryTreeItem(subPath);
                    currItem.getChildren().add(newChild);
                    currItem = newChild;

                    // Add check listener to the item
                    addCheckListenerToItem(currItem);
                }

                // Before moving to the next part of the tree, set the current item to be independent
                // Therefore, checking/unchecking the item will not cause other items in the treeview to become selected/unselected
                currItem.setIndependent(true);
            } catch (Exception e) {

                System.out.printf("\nPath: %s\nroot offset: %d\nTarget Depth: %d\n", path, rootOffset, targetDepth);
            }
        }
    }

    /**
     * Display the list of files to the user.
     * <p>
     * This function is separated from the `displayFiles` instance method to allow for swapping out the backend
     * after startup.
     */
    public void displayFiles() {

        // Clear previous file panes before filling in with new data
        lv_fileDisplay.getItems().clear();

        if (fileNames != null) {
            lv_fileDisplay.getItems().addAll(fileNames.collect(Collectors.toList()));
        }
    }

    public void notifyBackendSwapCompleted() {
        if (waitingForSwapDialog != null) {
            fileNames = switchboard.search(filter);
            updateFiles();
            waitingForSwapDialog.close();
            waitingForSwapDialog.getDialogPane().getScene().getWindow().hide();
        }
    }

    private void showWaitingForSwapDialog() {
        waitingForSwapDialog = new Dialog<>();
        waitingForSwapDialog.setHeaderText("One moment, please...");
        waitingForSwapDialog.setContentText("We are indexing your file system.");
        waitingForSwapDialog.show();
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
            return getItem() == null ? "" : "/" + getItem();
        }
    }
}
