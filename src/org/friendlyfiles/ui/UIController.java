package org.friendlyfiles.ui;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.friendlyfiles.Backend;
import org.friendlyfiles.FileSource;
import org.friendlyfiles.PostingList;
import org.friendlyfiles.Switchboard;
import org.friendlyfiles.models.*;
import org.friendlyfiles.testing.BackendDemo;
import org.friendlyfiles.utils.*;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.CacheHint;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.util.Callback;

public class UIController {
	
    @FXML
    private Accordion acc_leftPane;

    @FXML
    private Accordion acc_rightPane;

    @FXML
    private BorderPane bp_root;

    @FXML
    private Button btn_search;

    @FXML
    private CheckBox cbx_searchExtension;

    @FXML
    private HBox hbox_headerContent;

    @FXML
    private HBox hbx_statusBar;

    @FXML
    private Label lbl_advFilter;

    @FXML
    private Label lbl_view;

    @FXML
    private ScrollPane spn_fileDisplay;

    @FXML
    private ScrollPane spn_leftPane;

    @FXML
    private ScrollPane spn_rightPane;

    @FXML
    private Tab tab_db;

    @FXML
    private Tab tab_filter;

    @FXML
    private Tab tab_query;

    @FXML
    private Tab tab_sort;

    @FXML
    private TabPane tbp_header;

    @FXML
    private TextField tbx_search;

    @FXML
    private TitledPane tpn_dirTree;

    @FXML
    private TilePane tpn_fileDisplay;

    @FXML
    private TitledPane tpn_filterStack;

    @FXML
    private TreeView<RealPath> tvw_dirTree;

    @FXML
    private VBox vbox_header;

    @FXML
    private VBox vbx_leftPane;

    @FXML
    private VBox vbx_leftSpnBody;

    @FXML
    private VBox vbx_rightPane;

    @FXML
    private VBox vbx_rightSpnBody;

    @FXML
    public void btn_search_click(ActionEvent event) {
        // TODO: Make this not use RealPaths or ArrayLists.
        fileKeys = (ArrayList<RealPath>) switchboard.search(tbx_search.getText())
                .map(item -> RealPath.get(item.name))
                .collect(Collectors.toList());
    	updateFiles(fileKeys);
    }

    private Switchboard switchboard;

    // (Probably) temporary variable holding the paths of the root directories, for use in the updateTreeDirs() method
    private ArrayList<RealPath> rootDirPaths;
    
    // Current ordered list of files
    private ArrayList<RealPath> fileKeys;
    
    public void initialize() {
    	
    	// Enable caching of the file display panel
    	tpn_fileDisplay.setCache(true);
    	tpn_fileDisplay.setCacheHint(CacheHint.SPEED);
    	
		// For future reference, much of the following cellfactory instantiation code was based off of the following resource:
		// https://stackoverflow.com/a/39466520
		
		//Set the cell factory for the tree view in order to add checkbox functionality and allow for more control over the naming of the individual cells
		tvw_dirTree.setCellFactory(new Callback<TreeView<RealPath>, TreeCell<RealPath>>() {
            @Override
            public CheckBoxTreeCell<RealPath> call(TreeView<RealPath> p) {
                return new DirectoryTreeCell();
            }
        });

        Path dbPath = Paths.get("FriendlyFilesDatabase");
        if (Files.exists(dbPath)) {
            try (Backend backend = PostingList.deserializeFrom(dbPath)) {
                switchboard = new Switchboard(backend, new FileSource());
            } catch (Exception e) {
                // TODO: Show the user an error message or something and exit program.
                throw new Error(e);
            }
        } else {
            switchboard = new Switchboard(new PostingList(dbPath), new FileSource());
        }
    }

    /**
     * Update the list of file keys stored by the UI. The order of the keys in this list corresponds to the order in which they will display to the user.<br>
     * Additionally, this method will immediately display these files to the user.
     * @param fileKeys the file keys to be stored by the UI
     */
    public void updateFiles(ArrayList<RealPath> fileKeys) {
    	
    	this.fileKeys = fileKeys;
    	displayFiles(fileKeys);
    }
    
    /**
     * Adds a listener to the given item which will fire whenever the item is checked/unchecked.
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
					}
					else {
						
						// Remove the directory item from the selected set
						checkedDirItems.remove(i);
					}
				}
			}
		});
    }
    
    // Set of all the selected directory items
    // Since each item stores the full path of the directory it refers to, implementing a directory inclusion/exclusion toggle shouldn't be the most difficult thing in the world
    private ObservableSet<DirectoryTreeItem> checkedDirItems = FXCollections.observableSet();
    
    /**
     * Updates the Directory treeview using the provided list of top-level directories.<br>
     * Note: This method can cause issues if called while the UI is still setting up; at the earliest it should be called towards the end of the initialize() method.
     * @param rootDirs the list of directories to treat as the "root" or top-level directories. These directories and their subdirectories will be loaded into the treeview.
     */
    public void updateDirTree(List<RealPath> rootDirs) {
    	
    	// Set the treeview's root directory to a new Directory item with no path
    	DirectoryTreeItem treeRoot = new DirectoryTreeItem(null);
    	treeRoot.setIndependent(true);
    	tvw_dirTree.setRoot(treeRoot);
    	
    	// For each relative directory root, walk through its entire logical directory tree, and add items to the visual tree view as those logical directories are visited
	 	for (RealPath dirRoot : rootDirs) {
	 		
	 		// Add the current root to the base of the treeview
	 		DirectoryTreeItem dirRootItem = new DirectoryTreeItem(RealPath.create(dirRoot));
	 		dirRootItem.setIndependent(true);
	 		addCheckListenerToItem(dirRootItem);
	 		treeRoot.getChildren().add((TreeItem<RealPath>) dirRootItem);
	 		
	 		// Walk through subdirectories and add them as children
	 		try {
				Files.walkFileTree(dirRoot.toAbsolutePath(), new SimpleFileVisitor<Path>() {
 
					@Override
					public FileVisitResult preVisitDirectory(Path file, BasicFileAttributes attrs) throws IOException {
						
						RealPath realFile = RealPath.create(file);
						
						if (!realFile.toString().equals(dirRoot.toString())) {
							
							// Create a new directory item within the visual tree according to its path relative to the root
							createDirItemFromPath(realFile, dirRootItem);
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
    private void createDirItemFromPath(RealPath path, DirectoryTreeItem root) {
    	
    	// Get offset/distance of the root directory (for this specific tree of directories) from the filesystem root
    	int rootOffset = root.getValue().getNameCount();
    	
    	// Get number of directories between this file tree's relative root and the inner target directory
    	int targetDepth = path.getNameCount() - rootOffset;
    	
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
    			RealPath subPath = RealPath.get(
    					String.format("%s%s", path.getRoot(), path.subpath(0, rootOffset + i + 1)));
    			
        		// Use the subpath information to navigate through and create new children within the existing tree view items
    			
    			// Boolean to determine if the current item contains the requested child directory
    			boolean containsChild = false;
    			
    			// Loop over the children of the current item
    			for (TreeItem<RealPath> child : currItem.getChildren()) {
    				
    				// Grab a reference to the current child as a DirectoryTreeItem
    				DirectoryTreeItem childDir = (DirectoryTreeItem) child;
    				
    				// If the child's path is equal to the path of the directory item we are requesting,
    				// make that child the new current item
    				if (Files.isSameFile(childDir.getValue().toAbsolutePath(), subPath.toAbsolutePath())) {
    					
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
    		}
    		catch (Exception e) {
				
    			System.out.printf("\nPath: %s\nroot offset: %d\nTarget Depth: %d\n", path, rootOffset, targetDepth);
			}
    	}
    }
    
    /**
     * Display the given list of files to the user, presented in the order they are stored within the list
     * @param fileKeys the files to display to the user
     */
    private void displayFiles(ArrayList<RealPath> fileKeys) {
    	
    	// Clear previous file panes before filling in with new data
    	tpn_fileDisplay.getChildren().clear();
    	
    	// Get possible file icon images
    	Image otherIcon = new Image("/org/friendlyfiles/img/ico_other.png");
    	//Image txtIcon = new Image("/img/ico_txt");
    	//Image imgIcon = new Image("/img/ico_img");
    	
    	// Component size properties
    	int height = 112;
		int width = 80;
		int border = 12;

        tpn_fileDisplay.getChildren().addAll(switchboard.getAllFiles()
                .map(item -> {
                    FilePane filePane = new FilePane(item, height, width, border, otherIcon);
                    filePane.getSelectionArea().addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                        System.out.println(filePane.getFile().getName());
                    });
                    return filePane;
                })
                .collect(Collectors.toList()));
    }
    
    
    /**
     * A private class defining how the checkbox tree cells should be named.<br>
     * In this case we specify that it should using the lowermost directory name rather than the entire path to the file<br>
     * Cell Factory implementation is further detailed on Oracle's tree view documentation:<br>
     * <a href="https://docs.oracle.com/javafx/2/ui_controls/tree-view.htm">https://docs.oracle.com/javafx/2/ui_controls/tree-view.htm</a>
     */
    static final class DirectoryTreeCell extends CheckBoxTreeCell<RealPath> {
    	
    	// Do not edit this method to change cell naming, the majority of the code here is recommended by the JavaFX developers
        @Override
        public void updateItem(RealPath item, boolean empty) {
            super.updateItem(item, empty);
            
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            }
            else {
            	setText(getString());
            }
        }
        
        // Update this method to change how the tree cells are named
        private String getString() {
            return getItem() == null ? "" : "/" + getItem().getFileName().toString();
        }
    }
}
