package org.friendlyfiles;

public class FilterStep {
	
	private String displayName;
	
	public enum FilterType {TEXT, EXTENSION, DATE_EDITED, FILESIZE}
	private static String[] filterNames = {"Text", "File Extension", "Date Edited", "File Size"};
	private FilterType type;
	
	public FilterStep(String displayName, FilterType type) {
		
		this.displayName = displayName;
		this.type = type;
	}

	public String getName() {
		return displayName;
	}

	public void setName(String displayName) {
		this.displayName = displayName;
	}
	
	public FilterType getType() {
		
		return type;
	}
	
	public void setType(FilterType type) {
		
		this.type = type;
	}
	
	public static String[] getTypeNames() {
		
		return filterNames;
	}
}
