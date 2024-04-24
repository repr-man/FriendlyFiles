package org.friendlyfiles;

public class SortStep {
	
	private String displayName;
	
	public enum SortType {NAME, EXTENSION, DATE_CREATED, DATE_EDITED, FILESIZE}
	private SortType type;
	
	public SortStep(String displayName, SortType type) {
		
		this.displayName = displayName;
		this.type = type;
	}
	
	

	public String getName() {
		return displayName;
	}

	public void setName(String displayName) {
		this.displayName = displayName;
	}
	
	public SortType getType() {
		
		return type;
	}
	
	public void setType(SortType type) {
		
		this.type = type;
	}
}
