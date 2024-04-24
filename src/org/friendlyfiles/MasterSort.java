package org.friendlyfiles;

import java.util.Comparator;


public class MasterSort<T> implements Comparator<T> {
	
	// TODO: Create 
	
	// The individual comparator objects to be applied
	private Comparator<T>[] steps;
	
	public MasterSort(Comparator<T>[] steps) {
		
		this.steps = steps;
	}
	
	// Compare the two given objects, looping through the list of comparators either until the end of the list or once a comparable difference is found
	@Override
	public int compare(T o1, T o2) {
		
		for (Comparator<T> step : steps) {
			
			int result = step.compare(o1, o2);
			if (result != 0) return result;
		}
		
		return 0;
	}
	
	
}
