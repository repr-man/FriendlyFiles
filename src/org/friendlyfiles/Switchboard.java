package org.friendlyfiles;

import java.util.stream.Stream;

// Handles all coordination between the ui, backend, and file system.
public class Switchboard {
	
    private Backend backend;
    // TODO: Change this to an array so we can have multiple file sources at once?
    private final FileSource fileSource;
    
    public Switchboard(Backend backend, FileSource fileSource) {
        this.backend = backend;
        this.fileSource = fileSource;
        backend.generateFromFilesystem(this);
    }

    public void setBackend(Backend backend) {
        this.backend = backend;
    }

    public Stream<String> getAllFileNames() {
        return backend.getAllFileNames();
    }

    // This function is going to change a lot as we figure out the best way to build filters and their queries.
    public Stream<FileModel> search(String query) {
        return backend.get(query);
    }

    public Stream<FileModel> search(String query, QueryFilter filter) {
        return backend.get(query, filter);
    }

    // Rename this?
    public Stream<FileModel> search(QueryFilter filter) {
        return backend.get(filter);
    }
}

