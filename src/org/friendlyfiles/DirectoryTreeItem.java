package org.friendlyfiles;

import java.nio.file.Path;

import javafx.scene.control.CheckBoxTreeItem;

// An extension of the CheckboxTreeItem, primarily to override the .equals() method
public class DirectoryTreeItem extends CheckBoxTreeItem<Path> {
	
	public DirectoryTreeItem(Path name) {
		
		super(name);
	}

	/** Overriden equals method allows for the ArrayList's indexOf method to compare the path values rather than the object references
	 * 
	 * @param o the object to be compared with this one
	 * @return true if, and only if, the given object is a CheckBoxTreeItem holding an equal Path value
	 */
	@Override
	public boolean equals(Object o) {
		
		// Check to ensure that the objects are of the same class before making the comparison
		if (o.getClass() == this.getClass()) {
			
			return this.getValue().equals(((DirectoryTreeItem)o).getValue());
		}
		
		// Return false if the other object is not a DirectoryTreeItem
		return false;
	}
}
