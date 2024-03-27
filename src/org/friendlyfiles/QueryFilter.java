package org.friendlyfiles;

public final class QueryFilter {
    // TODO: Add more filters
    long fileSizeLower, fileSizeUpper;

    public QueryFilter addFileSizeRange(long lower, long upper) {
        assert(lower <= upper);
        if (lower > fileSizeLower && lower <= fileSizeUpper) {
            fileSizeLower = lower;
        }
        if (upper >= fileSizeLower && upper < fileSizeUpper) {
            fileSizeUpper = upper;
        }
        return this;
    }
}