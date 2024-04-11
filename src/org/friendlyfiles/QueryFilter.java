package org.friendlyfiles;

public final class QueryFilter {
    private final String root;
    private long fileSizeLower, fileSizeUpper;

    public QueryFilter(String root) {
        this.root = root;
    }

    public String getRoot() {
        return root;
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