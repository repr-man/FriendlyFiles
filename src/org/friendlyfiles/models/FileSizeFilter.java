package org.friendlyfiles.models;

import org.friendlyfiles.QueryFilter;

public class FileSizeFilter extends FilterStep {
	
	public enum SizeUnit {B, KB, MB, GB, TB}
	private static final String[] unitNames = {"B", "KB", "MB", "GB", "TB"};
	
	private SizeUnit maxSizeUnit;
	private long maxSize;
	
	private SizeUnit minSizeUnit;
	private long minSize;

	public FileSizeFilter(String displayName, FilterType type, SizeUnit maxSizeUnit, long maxSize, SizeUnit minSizeUnit, long minSize) {
		super(displayName, type);
		
		this.maxSizeUnit = maxSizeUnit;
		this.maxSize = maxSize;
		this.minSizeUnit = minSizeUnit;
		this.minSize = minSize;
	}
	
	public SizeUnit getMaxSizeUnit() {
		return maxSizeUnit;
	}
	
	public void setMaxSizeUnit(SizeUnit maxSizeUnit) {
		this.maxSizeUnit = maxSizeUnit;
	}

	public SizeUnit getMinSizeUnit() {
		return minSizeUnit;
	}

	public void setMinSizeUnit(SizeUnit minSizeUnit) {
		this.minSizeUnit = minSizeUnit;
	}

	public long getMaxSize() {
		return maxSize;
	}

	public void setMaxSize(long maxSize) {
		this.maxSize = maxSize;
	}
	
	public long getMinSize() {
		return minSize;
	}

	public void setMinSize(long minSize) {
		this.minSize = minSize;
	}
	
	public static String[] getUnitNames() {
		
		return unitNames;
	}

	@Override
	public void addToQueryFilter(QueryFilter filter) {
		filter.addFileSizeRange(minSize, maxSize);
	}
}
