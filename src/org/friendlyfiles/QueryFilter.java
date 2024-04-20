package org.friendlyfiles;

import java.util.ArrayList;

/**
 * Handles and passes all filters except the search query between the UI and the backend.
 */
public final class QueryFilter {
    private final ArrayList<String> roots = new ArrayList<>();
    private long fileSizeLower, fileSizeUpper = Long.MAX_VALUE;

    /**
     * @return the list of root directories
     */
    public ArrayList<String> getRoots() {
        return roots;
    }

    /**
     * Adds a root directory to the filter.
     *
     * @param rootPath the path of the directory to add
     */
    public void addRoot(String rootPath) {
        roots.add(rootPath);
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
}