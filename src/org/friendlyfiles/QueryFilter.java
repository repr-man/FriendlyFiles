package org.friendlyfiles;

import org.friendlyfiles.ui.UIController;
import org.roaringbitmap.RoaringBitmap;

import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Handles and passes all filters except the search query between the UI and the backend.
 */
public final class QueryFilter {
    // This contains more than the visible items because they are not postprocessed.
    private RoaringBitmap visibleItems = RoaringBitmap.bitmapOfRange(0, 0x100000000L);
    private final ArrayList<String> roots = new ArrayList<>();
    private String query = UIController.fileSeparator;
    private long fileSizeLower, fileSizeUpper = Long.MAX_VALUE;
    private long dateTimeStart, dateTimeEnd = Long.MAX_VALUE;

    public RoaringBitmap getVisibleItems() {
        return visibleItems;
    }

    public void setVisibleItems(RoaringBitmap visibleItems) {
        this.visibleItems = visibleItems;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query.isEmpty() ? UIController.fileSeparator : query;
    }

    /**
     * @return the list of root directories without 'start of path' character
     */
    public ArrayList<String> getRoots() {
        // Removes the 'start of path' character.
        return (ArrayList<String>) roots.stream().map(item -> item.substring(1)).collect(Collectors.toList());
    }

    /**
     * @return the list of root directories with 'start of path' character
     */
    public ArrayList<String> getRootsWithStartOfPath() {
        // Removes the 'start of path' character.
        //return (ArrayList<String>) roots.stream().map(item -> item.substring(1)).collect(Collectors.toList());
        return roots;
    }

    /**
     * Adds a root directory to the filter.
     *
     * @param rootPath the path of the directory to add
     */
    public String addRoot(String rootPath) {
        // We don't want to add roots that we can already access.
        if (roots.stream().anyMatch(('\t' + rootPath)::startsWith)) {
            return rootPath;
        }
        // We add the 'start of path' character to ensure that the allowed items start with the root.
        // For example, if the root is "/bin", we won't get files starting with "/usr/bin".
        roots.add('\t' + rootPath);
        return null;
    }

    /**
     * Removes one of the root directories from the filter.
     *
     * @param rootPath the name of the directory to remove
     */
    public void removeRoot(String rootPath) {
        roots.remove(rootPath);
    }

    /**
     * Shrinks the file size range allowed by the filter.
     *
     * @param lower the potential new lower bound
     * @param upper the potential new upper bound
     * @return this filter
     */
    public QueryFilter addFileSizeRange(long lower, long upper) {
        assert (lower <= upper);
        if (lower > fileSizeLower && lower <= fileSizeUpper) {
            fileSizeLower = lower;
        }
        if (upper >= fileSizeLower && upper < fileSizeUpper) {
            fileSizeUpper = upper;
        }
        return this;
    }

    /**
     * Checks if a file size is in the range allowed by the filter.
     *
     * @param fileSize the file size to check
     * @return whether the file size is in the allowed range
     */
    public boolean isInFileSizeRange(long fileSize) {
        return fileSize >= fileSizeLower && fileSize <= fileSizeUpper;
    }
    
    /**
     * Shrinks the date range allowed by the filter.
     * 
     * @param start update the start date of the filter, or -1 if there is no start cutoff.
     * @param end update the end date of the filter, or -1 if there is no end cutoff.
     * @return
     */
    public QueryFilter setDateRange(long start, long end) {
    	assert (end <= start);
    	if (start > dateTimeStart && start <= dateTimeEnd) {
    		
    		dateTimeStart = start;
    	}
    	if (end >= dateTimeStart && start < dateTimeEnd) {
    		
    		dateTimeEnd = end;
    	}
    	return this;
    }
    
    /**
     * Checks if a file date is in the range allowed by the filter.
     *
     * @param fileDate the file date to check
     * @return whether the file date is in the allowed range
     */
    public boolean isInFileDateRange(long fileDate) {
        return fileDate >= dateTimeStart && fileDate <= dateTimeEnd;
    }
}