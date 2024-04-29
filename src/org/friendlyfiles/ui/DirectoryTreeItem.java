package org.friendlyfiles.ui;

import javafx.collections.ObservableList;
import javafx.scene.control.*;
import java.io.File;

public class DirectoryTreeItem extends CheckBoxTreeItem<String> {
    // Boolean to determine whether or not the checkbox was clicked by a user
    private static boolean isClicked = true;

    public DirectoryTreeItem(String name) {

        super(name);
    }

    /**
     * Overridden equals method allows for the ArrayList's indexOf method to compare the path values rather than the object references
     * @param o the object to be compared with this one
     * @return true if, and only if, the given object is a CheckBoxTreeItem holding an equal Path value
     */
    @Override
    public boolean equals(Object o) {

        // Try to compare the Directory tree items based on their paths before falling back to the default equals method
        // The following checks are required or else many exceptions may be thrown by JavaFX

        // First check that o is not null and is a DirectoryTreeItem
        return (o != null && o.getClass().equals(this.getClass()))
                // Then check that the values of both DirectoryTreeItems are not null
                && (this.getValue() != null && ((DirectoryTreeItem) o).getValue() != null)
                // Lastly check that the values of both DirectoryTreeItems are equal
                && this.getValue().equals(((DirectoryTreeItem) o).getValue());
    }

    /**
     * Recursively walks up the tree to reconstruct the full directory path of an item.
     * @return the absolute path
     */
    private String getFullDirectoryPath() {
        String ret;
        if (getParent().getValue() == null) {
            ret = getValue();
            return ret;
        }
        ret = ((DirectoryTreeItem) getParent()).getFullDirectoryPath();
        ret += UIController.fileSeparator + getValue();
        return ret;
    }

    /**
     * Gets the list of children with the correct type.
     * @return the list of children
     */
    private ObservableList<DirectoryTreeItem> retrieveChildren() {
        // This conversion is safe because all children are DirectoryTreeItems
        return (ObservableList<DirectoryTreeItem>) (Object) getChildren();
    }

    /**
     * Check each child object, either selecting or unselecting based on the value of the clicked checkbox
     */
    public void checkChildren(DirectoryTreeItem node) {
        if (isIndeterminate()) {
            node.setSelected(false);
        } else {
            node.setSelected(isSelected());
        }
        node.setIndeterminate(false);

        // Recursively dig down into child nodes to continue the selection/unselection
        for (DirectoryTreeItem child : node.retrieveChildren()) {
            checkChildren(child);
        }
    }

    /**
     * Determine whether parent checkboxes of the given node need to be selected or marked as indeterminate.
     */
    public void determineParents(DirectoryTreeItem node) {
        // Get the parent of the current node
        DirectoryTreeItem parent = (DirectoryTreeItem)node.getParent();

        // If that parent's parent is null, that would mean it is the root node, which we do not want to edit
        if (parent.getParent() != null) {

            // Boolean to determine if action is needed upon the parent's checkbox
            boolean pass = true;
            int unselectedCount = 0;

            // Loop through the parent's children; if any are unselected or indeterminate, switch the boolean
            for (DirectoryTreeItem child : parent.retrieveChildren()) {
                if (!child.isSelected() || child.isIndeterminate()) {
                    ++unselectedCount;
                    pass = false;
                }
            }

            // If any of the children were unselected or indeterminate, set the parent to indeterminate
            if (!pass) {
                if (unselectedCount == parent.retrieveChildren().size()) {
                    parent.setIndeterminate(false);
                    parent.setSelected(false);
                } else {
                    parent.setIndeterminate(true);
                }
                determineParents(parent);
            }
            // Otherwise, set the parent to selected and "determinate" (not indeterminate)
            else {
                parent.setIndeterminate(false);
                parent.setSelected(true);
                determineParents(parent);
            }
        }
    }

    /**
     * Adds a listener to the given item which will fire whenever the item is checked/unchecked.
     */
    public void addCheckListener(UIController controller) {
        // Check all boxes and add them to the set initially
        selectedProperty().set(true);
        setIndependent(true);

        selectedProperty().addListener((observable, oldValue, newValue) -> {
            // If the isClicked boolean is true, this means the current checkbox is the one that was clicked
            if (isClicked) {

                // Immediately set the boolean to false to prevent other checkboxes from acting on their listeners
                isClicked = false;

                // Update the file list
                if (isIndeterminate()) {
                    controller.disallowAllFilesInDirectory(getFullDirectoryPath());
                } else {
                    controller.toggleFilesInDirectory(getFullDirectoryPath());
                }

                // Check all children of the selected checkbox, either selecting or unselecting
                checkChildren(this);

                // Determine whether the parent checkboxes need to be checked or set as indeterminate
                determineParents(this);

                // Set the boolean back to true once all logic is complete
                isClicked = true;
            }
        });
    }

    /**
     * Recursively adds children to the file tree from the segments of a path.
     * @param controller the controller of the directory tree
     * @param childName the path from which to find children
     */
    public void addAllChildren(UIController controller, String childName) {
        setIndependent(true);
        int splitIdx = childName.indexOf(File.separatorChar, 1);
        String firstChild;
        if (splitIdx < 0) {
            firstChild = childName.startsWith(File.separator) ? childName.substring(1) : childName;
        } else {
            firstChild = childName.startsWith(File.separator) ? childName.substring(1, splitIdx) : childName.substring(0, splitIdx);
        }

        DirectoryTreeItem retItem;
        DirectoryTreeItem newItem = new DirectoryTreeItem(firstChild);
        int pos = getChildren().indexOf(newItem);
        if (pos < 0) {
            newItem.addCheckListener(controller);
            retItem = newItem;
            getChildren().add(retItem);
        } else {
            retItem = (DirectoryTreeItem) getChildren().get(pos);
        }
        if (splitIdx >= 0) {
            retItem.addAllChildren(controller, childName.substring(splitIdx));
        }
    }
}