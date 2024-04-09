package org.friendlyfiles;

import org.friendlyfiles.ui.UIController;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// Handles all coordination between the ui, backend, and file system.
public class Switchboard {
	private final UIController controller;
    private Backend backend;
    private final FileSource fileSource;
    
    public Switchboard(UIController controller, Backend backend, FileSource fileSource) {
        this.controller = controller;
        this.backend = backend;
        this.fileSource = fileSource;
        backend.generateFromFilesystem(this);
    }

    /**
     * Shuts down the backend.
     */
    public void shutDown() {
        try {
            backend.close();
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    public void swapInBackend(Backend backend) {
        this.backend = backend;
        controller.displayFiles(this);
    }

    public Stream<String> getAllFileNames() {
        return backend.getAllFileNames();
    }

    // This function is going to change a lot as we figure out the best way to build filters and their queries.
    public Stream<String> search(String query) {
        return backend.get(query);
    }

    public Stream<String> search(String query, QueryFilter filter) {
        return backend.get(query, filter);
    }

    // Rename this?
    public Stream<String> search(QueryFilter filter) {
        return backend.get(filter);
    }
}

