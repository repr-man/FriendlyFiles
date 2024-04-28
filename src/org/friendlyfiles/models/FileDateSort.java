package org.friendlyfiles.models;

import java.util.Comparator;

import org.friendlyfiles.FileModel;

public class FileDateSort extends SortStep implements Comparator<FileModel> {
	
	private OrderType order;

	public FileDateSort(String displayName, SortType type, OrderType order) {
		super(displayName, type, order);
		
		this.order = order;
	}

	@Override
	public int compare(FileModel o1, FileModel o2) {
		
		if (order == OrderType.ASCENDING) {
			
			return (int)(o1.timestamp - o2.timestamp);
		}
		
		return (int)(o2.timestamp - o1.timestamp);
	}
}
