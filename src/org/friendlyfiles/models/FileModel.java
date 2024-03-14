package org.friendlyfiles.models;

import java.time.ZonedDateTime;

import org.friendlyfiles.utils.RealPath;

public class FileModel {
	
	// File name, stored separately for slight performance improvements
	private String fileName;
	
	// Path of the file within the file system
	private RealPath path;
	
	// Size of the file in bytes
	private long size;
	
	// Creation/Modification Dates
	private ZonedDateTime dateCreated;
	private ZonedDateTime dateModified;
	
	public FileModel(RealPath path) {
		
		this.path = path;
		
		fileName = path.getFileName().toString();
	}
	
	public String getName() {
		
		return fileName;
	}
	
	public RealPath getPath() {
		
		return path;
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
