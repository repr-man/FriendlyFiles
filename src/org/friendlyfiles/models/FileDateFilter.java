package org.friendlyfiles.models;

import java.time.LocalDateTime;

public class FileDateFilter extends FilterStep {
	
	private LocalDateTime startDate;
	private LocalDateTime endDate;
	
	public FileDateFilter(String displayName, FilterType type, LocalDateTime startDate, LocalDateTime endDate) {
		super(displayName, type);
		
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public LocalDateTime getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDateTime startDate) {
		this.startDate = startDate;
	}

	public LocalDateTime getEndDate() {
		return endDate;
	}

	public void setEndDate(LocalDateTime endDate) {
		this.endDate = endDate;
	}
	
	public long getStartEpoch() {
		
		return 0;
	}
	
	public long getEndEpoch() {
		
		return 0;
	}
}
