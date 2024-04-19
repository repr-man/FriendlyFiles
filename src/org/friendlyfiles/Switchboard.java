package org.friendlyfiles;

import javafx.application.Platform;
import org.friendlyfiles.ui.UIController;

import java.io.IOException;
import java.nio.file.Paths;
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
        Platform.runLater(controller::notifyBackendSwapCompleted);
    }

    // This function is going to change a lot as we figure out the best way to build filters and their queries.
    public Stream<String> search(String query) {
        return backend.get(query);
    }

    public Stream<String> search(String query, QueryFilter filter) {
        return backend.get(query, filter);
    }

    public Stream<String> search(QueryFilter filter) {
        return filter == null ? Stream.empty() : backend.get(filter);
    }

    public Stream<String> getDirectories(QueryFilter filter) {
        return backend.getDirectories(filter);
    }

    public void openFile(String filePath) {
        try {
            fileSource.openFile(Paths.get(filePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

