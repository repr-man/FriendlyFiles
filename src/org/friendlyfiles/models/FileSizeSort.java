package org.friendlyfiles.models;

import java.util.Comparator;

import org.friendlyfiles.FileModel;

public class FileSizeSort extends SortStep implements Comparator<FileModel> {
	
	private OrderType order;

	public FileSizeSort(String displayName, SortType type, OrderType order) {
		super(displayName, type, order);
		
		this.order = order;
	}

	@Override
	public int compare(FileModel o1, FileModel o2) {
		
		if (order == OrderType.ASCENDING) {
			
			return (int)(o1.size - o2.size);
		}
		
		return (int)(o2.size - o1.size);
	}
}
