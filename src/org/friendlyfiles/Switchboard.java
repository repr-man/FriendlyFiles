package org.friendlyfiles;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import org.friendlyfiles.ui.UIController;

import java.io.IOException;
import java.nio.file.*;
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
        fileSource.openFile(Paths.get(filePath));
    }

    public void delete(ObservableList<String> selectedItems) {
        selectedItems.forEach(item -> {
            try {
                fileSource.remove(Paths.get(item));
                backend.remove(item);
            } catch (NoSuchFileException e) {
                controller.showErrorDialog(String.format("File `%s` does not exist.\n\nWe will remove it from the file view.", item));
                backend.remove(item);
            } catch (IOException e) {
                controller.showErrorDialog(String.format("We were unable to delete file `%s` for unknown reasons.\n\n"
                        + "Do you have permission to delete this file?", item));
            }
        });
    }

    public void rename(ObservableList<String> selectedItems, String newName) {
        selectedItems.forEach(item -> {
            try {
                fileSource.renameFile(Paths.get(item), newName);
                backend.renameFile(item, newName);
            } catch (NoSuchFileException e) {
                controller.showErrorDialog(String.format("File `%s` does not exist.\n\nWe will remove it from the file view.", item));
                backend.remove(item);
            } catch (IOException e) {
                controller.showErrorDialog(String.format("We were unable to rename file `%s` for unknown reasons.\n\n"
                                                                 + "Do you have permission to rename this file?", item));
            }
        });
    }

    public void move(ObservableList<String> selectedItems, String destination) {
        selectedItems.forEach(item -> {
            try {
                Path itemPath = Paths.get(item);
                String itemName = itemPath.getFileName().toString();
                Path destPath = Paths.get(destination, itemName);
                fileSource.moveFile(itemPath, destPath);
                backend.moveFile(item, destination);
            } catch (NoSuchFileException e) {
                controller.showErrorDialog(String.format("File `%s` does not exist.\n\nWe will remove it from the file view.", item));
                backend.remove(item);
            } catch (IOException e) {
                controller.showErrorDialog(String.format("We were unable to move file `%s` for unknown reasons.\n\n"
                                                                 + "Do you have permission to move this file?", item));
            }
        });
    }

    /**
     * Shows an error dialog box via the UI controller.
     *
     * @param message the error message to show to the user
     */
    public void showErrorDialog(String message) {
        controller.showErrorDialog(message);
    }
}

