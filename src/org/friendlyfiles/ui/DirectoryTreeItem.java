package org.friendlyfiles.ui;

import javafx.beans.value.*;
import javafx.collections.*;
import javafx.scene.control.*;

import java.io.File;
import java.util.Objects;
import java.util.stream.IntStream;

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

    /**
     * Adds a listener to the given item which will fire whenever the item is checked/unchecked.
     */
    public void addCheckListener(ObservableSet<DirectoryTreeItem> checkedDirItems) {
        // Check all boxes and add them to the set initially
        selectedProperty().set(true);
        checkedDirItems.add(this);

        selectedProperty().addListener((observable, oldValue, newValue) -> {
            // Prevents any issues with the listener being triggered without any change occuring (should never happen)
            if (newValue != oldValue) {
                if (newValue) {
                    // Add the directory item to the selected set
                    checkedDirItems.add(this);
                } else {
                    // Remove the directory item from the selected set
                    checkedDirItems.remove(this);
                }
            }
        });
    }

    public void addAllChildren(String childName, ObservableSet<DirectoryTreeItem> checkedDirItems) {
        if (childName.isEmpty()) return;

        int splitIdx = childName.indexOf(File.separatorChar, 1);
        String firstChild;
        if (splitIdx < 0) {
            firstChild = childName.substring(1);
        } else {
            firstChild = childName.substring(1, splitIdx);
        }

        DirectoryTreeItem newItem = new DirectoryTreeItem(firstChild);
        boolean hasNewItem = checkedDirItems.contains(newItem);
        if (!hasNewItem) {
            newItem.setIndependent(true);
            newItem.addCheckListener(checkedDirItems);
        }
        if (splitIdx >= 0) {
            newItem.addAllChildren(childName.substring(splitIdx), checkedDirItems);
        }
    }
}