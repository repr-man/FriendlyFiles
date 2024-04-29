package org.friendlyfiles;

import javafx.collections.ObservableList;
import org.friendlyfiles.models.*;
import org.friendlyfiles.ui.UIController;
import org.roaringbitmap.RoaringBitmap;

import java.util.ArrayList;

/**
 * Handles and passes all filters except the search query between the UI and the backend.
 */
public final class QueryFilter {
    // This contains more than the visible items because they are not postprocessed.
    private final RoaringBitmap visibleItems = RoaringBitmap.bitmapOfRange(0, 0x100000000L);
    private final ArrayList<String> roots = new ArrayList<>();
    private String query = UIController.fileSeparator;
    private long fileSizeLower, fileSizeUpper = Long.MAX_VALUE;
    private long dateTimeStart, dateTimeEnd = Long.MAX_VALUE;
    private final ArrayList<String> textSearchTerms = new ArrayList<>();
    private final ArrayList<String> extSearchTerms = new ArrayList<>();
    private final ArrayList<SortStep> sortSteps = new ArrayList<>();

    /**
     * @return the bit set of visible files
     */
    public RoaringBitmap getVisibleItems() {
        return visibleItems;
    }

    /**
     * Gets the string query that was in the search box.
     * @return the query
     */
    public String getQuery() {
        return query;
    }

    /**
     * Sets the query to a given value, or to the file separator character if the query is blank
     * @param query the search query
     */
    public void setQuery(String query) {
        this.query = query.trim().isEmpty() ? UIController.fileSeparator : query;
    }

    /**
     * @return the list of root directories
     */
    public ArrayList<String> getRoots() {
        return roots;
    }

    /**
     * @return the list of sort steps
     */
    public ArrayList<SortStep> getSortSteps() {
        return sortSteps;
    }

    /**
     * Adds a root directory to the filter.
     *
     * @param rootPath the path of the directory to add
     * @return true if `rootPath` is already accessible
     */
    public boolean addRoot(String rootPath) {
        // We don't want to add roots that we can already access.
        if (roots.stream().anyMatch(rootPath::startsWith) || roots.stream().anyMatch(root -> root.startsWith(rootPath))) {
            return true;
        }
        roots.add(rootPath);
        return false;
    }

    /**
     * Clears the current filter parameters and adds new ones from a list of filter steps in the UI.
     * @param filterList the filter list from which to populate the QueryFilter
     */
    public void resetFilterSteps(ObservableList<FilterStep> filterList) {
        textSearchTerms.clear();
        extSearchTerms.clear();
        fileSizeLower = 0;
        fileSizeUpper = Long.MAX_VALUE;
        dateTimeStart = 0;
        dateTimeEnd = Long.MAX_VALUE;

        filterList.forEach(f -> {
            f.addToQueryFilter(this);
        });
    }

    /**
     * Adds an additive query term.
     * @param text the term to add
     */
    public void addTextFilter(String text) {
    	
    	assert(!text.trim().isEmpty());
    	
    	textSearchTerms.add(text);
    }

    /**
     * @return the list of additive query terms
     */
    public ArrayList<String> getTextSearchTerms() {
        return textSearchTerms;
    }

    /**
     * Adds an allowed file extension.
     * @param ext the file extension to add
     */
    public void addExtFilter(String ext) {
    	
    	assert(ext.length() > 1 && ext.startsWith("."));
    	
    	extSearchTerms.add(ext);
    }

    /**
     * @return the list of allowed file extensions
     */
    public ArrayList<String> getExtSearchTerms() {
        return extSearchTerms;
    }

    /**
     * Shrinks the file size range allowed by the filter.
     *
     * @param lower the potential new lower bound
     * @param upper the potential new upper bound
     */
    public void addFileSizeRange(long lower, long upper) {
        assert (lower <= upper);
        if (lower > fileSizeLower && lower <= fileSizeUpper) {
            fileSizeLower = lower;
        }
        if (upper >= fileSizeLower && upper < fileSizeUpper) {
            fileSizeUpper = upper;
        }
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
     * @param end   update the end date of the filter, or -1 if there is no end cutoff.
     */
    public void setFileDateRange(long start, long end) {
    	assert (end <= start);
    	if (start > dateTimeStart && start <= dateTimeEnd) {
    		
    		dateTimeStart = start;
    	}
    	if (end >= dateTimeStart && start < dateTimeEnd) {
    		
    		dateTimeEnd = end;
    	}
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