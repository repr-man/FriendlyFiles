package org.friendlyfiles;

import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Handles and passes all filters except the search query between the UI and the backend.
 */
public final class QueryFilter {
    private final ArrayList<String> roots = new ArrayList<>();
    private long fileSizeLower, fileSizeUpper = Long.MAX_VALUE;

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
    public void addRoot(String rootPath) {
        // We add the 'start of path' character to ensure that the allowed items start with the root.
        // For example, if the root is "/bin", we won't get files starting with "/usr/bin".
        roots.add('\t' + rootPath);
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