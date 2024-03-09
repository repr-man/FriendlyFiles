package org.friendlyfiles;

import javafx.scene.control.CheckBoxTreeItem;

// An extension of the CheckboxTreeItem, primarily to override the .equals() method
public class DirectoryTreeItem extends CheckBoxTreeItem<RealPath> {
	
	public DirectoryTreeItem(RealPath name) {
		
		super(name);
	}

	/** Overriden equals method allows for the ArrayList's indexOf method to compare the path values rather than the object references
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
}
