package org.friendlyfiles.models;

import java.time.ZonedDateTime;

public class FileModel {
	private String fileName;
	
	// Size of the file in bytes
	private long size;
	
	// Creation/Modification Dates
	private ZonedDateTime dateCreated;
	private ZonedDateTime dateModified;
	
	public FileModel(String path) {
		fileName = path;
	}
	
	public String getName() {
		
		return fileName;
	}
	
	public void setSize(long size) {
		
		this.size = size;
	}
	
	public long getSize() {
		
		return size;
	}
	
	public void setDateCreated(ZonedDateTime time) {
		
		dateCreated = time;
	}
	
	public ZonedDateTime getDateCreated() {
		
		return dateCreated;
	}
	
	public void setDateModified(ZonedDateTime time) {
		
		dateModified = time;
	}
	
	public ZonedDateTime getDateModified() {
		
		return dateModified;
	}
	
}
