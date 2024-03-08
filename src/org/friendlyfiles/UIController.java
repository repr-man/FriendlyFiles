package org.friendlyfiles;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.cert.LDAPCertStoreParameters;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.PrimitiveIterator.OfDouble;
import java.util.stream.Collectors;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;

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
    private TreeView<Path> tvw_dirTree;

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

    }
    
    public void updateDirTree(/* collection of root directory paths */) {
    	
    	
    	// Create root node to hold one or many directory roots
    	TreeItem<Path> base = new TreeItem<Path>(null);
    	tvw_dirTree.setRoot(base);
    	
    	// TODO: For each root, walk through logical directory tree, add items to visual directory tree as logical directories are visited
    	
    	// Add all the children of the roots to the tree view
//	 	for (Path root : /* collection of root directory paths */) {
//	 		
//	 		// Add the current root to the base of the treeview
//	 		DirectoryTreeItem rootItem = new DirectoryTreeItem(root);
//	 		base.getChildren().add(rootItem);
//	 		
//	 		// Walk through subdirectories and add them as children
//	 		try {
//				Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
// 
//					@Override
//					public FileVisitResult preVisitDirectory(Path file, BasicFileAttributes attrs) throws IOException {
//						
//						// Create a new directory item within the visual tree according to its path relative to the root
//						createDirItemFromPath(file, rootItem);
//						
//						return FileVisitResult.CONTINUE;
//					}
//				});
//			} catch (IOException e) {
//				
//				e.printStackTrace();
//			}
//	 	}
		 
    }
    
    /**
     * Loop through from the root to the target, allowing a new tree item to be created in the correct location within the treeview
     * 
     * @param path the path of the target directory
     * @param root the TreeItem holding the root folder of the target directory
     */
    private void createDirItemFromPath(Path path, DirectoryTreeItem root) {
    	
    	// Create a relative path with its root directory set to the directory root, and extending down to the target directory
    	Path relativePath = path.relativize(root.getValue());
    	
    	// Get number of directories between the root and target, not including the root or target directory
    	int depth = relativePath.getNameCount() - 1;
    	
    	// Prime the "current item" to be the root of the TreeList
    	DirectoryTreeItem currItem = root;
    	
    	// The path will have to be rebuilt as the [path].getName method returns a singular directory name, but we need an entire path for each loop
    	StringBuilder pathBuilder = new StringBuilder("");
    	
    	/*
    	 * Loop/"Walk" down the relative path (from root towards target) in order to find the target's parent directory
    	 * While this loop is running, it is also using the path names to walk down the existing tree items, eventually finding which tree item to append the target directory
    	 * 
		 * Note: [path].getName(int index) returns the path name with i as the distance from the root
		 * The root element can be thought to have index -1, the root's child is 0, and so on
		 */
    	for (int i = -1; i < depth; i++) {
    		
    		// Add the next portion of the path
    		pathBuilder.append(relativePath.getName(i));
    		
    		// Create a stream to process the children of the current tree item, and filter to find the child whose pathname matches the pathBuilder's current path 
    		currItem = (DirectoryTreeItem) currItem.getChildren().stream().filter(child -> 
    			
    			// For each child, apply the filter to see if the child's path value equals the pathBuilder's path value
    			child.getValue().equals(Paths.get(pathBuilder.toString()))
    			
			// Get the first child matching the filter and return it to currItem
    		).findFirst().get();
    		
    	}
    	// At the end of this loop, currItem should be the lowermost tree item to which we can add the target item
    	
    	currItem.getChildren().add(new DirectoryTreeItem(path));
    }
}
