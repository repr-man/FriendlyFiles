package org.friendlyfiles.models;

public class SortStep {
	
	private String displayName;
	
	public enum SortType {NAME, EXTENSION, DATE_EDITED, FILESIZE}
	private static String[] sortNames = {"Name", "File Extension", "Date Edited", "File Size"};
	private SortType type;
	
	public enum OrderType {ASCENDING, DESCENDING};
	private static String[] orderNames = {"Ascending", "Descending"};
	private OrderType order;
	
	
	public SortStep(String displayName, SortType type, OrderType order) {
		
		this.displayName = displayName;
		this.type = type;
		this.order = order;
	}

	public String getName() {
		return displayName;
	}

	public void setName(String displayName) {
		this.displayName = displayName;
	}
	
	public SortType getType() {
		
		return type;
	}
	
	public void setType(SortType type) {
		
		this.type = type;
	}
	
	public static String[] getTypeNames() {
		
		return sortNames;
	}
	
	public OrderType getOrder() {
		
		return order;
	}
	
	public void setOrder(OrderType order) {
		
		this.order = order;
	}
	
	public static String[] getOrderNames() {
		
		return orderNames;
	}
}
