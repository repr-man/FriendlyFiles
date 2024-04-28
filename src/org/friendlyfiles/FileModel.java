package org.friendlyfiles;

/**
 * A class for passing around information about files.
 */
public class FileModel {
    public String path;
    public long size;
    public long timestamp;

    public FileModel(String path, long size, long timestamp) {
        this.path = path;
        this.size = size;
        this.timestamp = timestamp;
    }
}
