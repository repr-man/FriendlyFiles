package org.friendlyfiles;

import java.util.ArrayList;

public final class QueryFilter {
    private final ArrayList<String> roots = new ArrayList<>();
    private long fileSizeLower, fileSizeUpper = Long.MAX_VALUE;

    public ArrayList<String> getRoots() {
        return roots;
    }

    public void addRoot(String root) {
        roots.add(root);
    }

    public void removeRoot(String root) {
        roots.remove(root);
    }

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

    public boolean isInFileSizeRange(long fileSize) {
        return fileSize >= fileSizeLower && fileSize <= fileSizeUpper;
    }
}