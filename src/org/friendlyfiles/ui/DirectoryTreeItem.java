package org.friendlyfiles.ui;

import javafx.scene.control.*;
import java.io.File;

// An extension of the CheckboxTreeItem, primarily to override the .equals() method
public class DirectoryTreeItem extends CheckBoxTreeItem<String> {

    public DirectoryTreeItem(String name) {

        super(name);
    }

    /**
     * Overridden equals method allows for the ArrayList's indexOf method to compare the path values rather than the object references
     *
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
     * Adds a listener to the given item which will fire whenever the item is checked/unchecked.
     */
    public void addCheckListener(UIController controller) {
        // Check all boxes and add them to the set initially
        selectedProperty().set(true);
        setIndependent(true);

        addEventHandler(DirectoryTreeItem.checkBoxSelectionChangedEvent(), event -> {
            if (event.getTarget() == this) {
                if (event.wasIndeterminateChanged() && !isIndeterminate() && !isSelected()) {
                    // Clear all the bits associated with this query
                    controller.allowAllFilesInDirectory(getFullDirectoryPath());
                } else {
                    // XOR the bits
                    controller.toggleFilesInDirectory(getFullDirectoryPath());
                }
            }
        });
    }

    public void addAllChildren(UIController controller, String childName) {
        setIndependent(true);
        int splitIdx = childName.indexOf(File.separatorChar, 1);
        String firstChild;
        if (splitIdx < 0) {
            firstChild = childName.substring(1);
        } else {
            firstChild = childName.substring(1, splitIdx);
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