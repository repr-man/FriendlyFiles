package org.friendlyfiles.models;

public class FileSearchModel {
	
	String query = "";
	boolean extensionIncluded = false;
	
	public FileSearchModel(String query, boolean extensionIncluded) {
		
		this.query = query;
		this.extensionIncluded = extensionIncluded;
	}
	
	public String getQuery() {
		
		return query;
	}
	
	public boolean isExtensionIncluded() {
		
		return extensionIncluded;
	}
}
