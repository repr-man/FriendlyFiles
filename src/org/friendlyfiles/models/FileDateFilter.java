package org.friendlyfiles.models;

import org.friendlyfiles.QueryFilter;

import java.time.*;

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
		return startDate.toEpochSecond(ZonedDateTime.now().getOffset());
	}
	
	public long getEndEpoch() {
		return endDate.toEpochSecond(ZonedDateTime.now().getOffset());
	}

	@Override
	public void addToQueryFilter(QueryFilter filter) {
		filter.setFileDateRange(getStartEpoch(), getEndEpoch());
	}
}
