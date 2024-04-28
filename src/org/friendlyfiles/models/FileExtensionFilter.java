package org.friendlyfiles.models;

import org.friendlyfiles.QueryFilter;

public class FileExtensionFilter extends FilterStep {
	
	private String extension;
	
	public FileExtensionFilter(String displayName, FilterType type, String extension) {
		super(displayName, type);
		
		this.extension = extension;
	}

	public String getExtension() {
		return extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}

	@Override
	public void addToQueryFilter(QueryFilter filter) {
		filter.addExtFilter(extension);
	}
}
