package org.friendlyfiles.models;

import org.friendlyfiles.QueryFilter;

public class FileTextFilter extends FilterStep {
	
	private String text;

	public FileTextFilter(String displayName, FilterType type, String text) {
		super(displayName, type);
		
		this.text = text;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@Override
	public void addToQueryFilter(QueryFilter filter) {
		filter.addTextFilter(text);
	}
}
