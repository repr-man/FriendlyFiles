package org.friendlyfiles.models;

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
}
