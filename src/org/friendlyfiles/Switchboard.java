package org.friendlyfiles;

import java.util.stream.Stream;

// Handles all coordination between the ui, backend, and file system.
public class Switchboard {
	
    private final Backend backend;
    // TODO: Change this to an array so we can have multiple file sources at once?
    private final FileSource fileSource;
    
    public Switchboard(Backend backend, FileSource fileSource) {
        this.backend = backend;
        this.fileSource = fileSource;
    }

    public Stream<org.friendlyfiles.models.FileModel> getAllFiles() {
        return backend.getAllFiles();
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

