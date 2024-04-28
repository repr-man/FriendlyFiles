package org.friendlyfiles.models;

import java.util.Comparator;

import org.friendlyfiles.FileModel;

public class FileNameSort extends SortStep implements Comparator<FileModel> {

	private OrderType order;
	
	public FileNameSort(String displayName, SortType type, OrderType order) {
		super(displayName, type, order);
		
		this.order = order;
	}

	@Override
	public int compare(FileModel o1, FileModel o2) {
		
		if (order == OrderType.ASCENDING) {
			
			// TODO: Return comparison between o1 and o2's file name
		}
		
		// TODO: Return comparison between o2 and o1's file name
		return 0;
	}
}
