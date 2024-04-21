package org.friendlyfiles;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import org.friendlyfiles.ui.UIController;

import java.io.IOException;
import java.nio.file.*;
import java.util.stream.Stream;

/**
 * Handles all coordination between the UI, backend, and file system.  This includes passing error messages for the
 * user to the controller and swapping the backend when it finishes indexing the filesystem.
 * <p>
 * Because the backend swap involves a number of threads, the process is a little convoluted:
 * <ol>
 * <li> {@link UIController#initialize} creates a PostingList and constructs a new Switchboard. </li>
 * <li> {@link Switchboard#Switchboard} calls {@link Backend#generateFromFilesystem} to start the indexing. </li>
 * <li> {@link PostingList#generateFromFilesystem} starts a new background thread for indexing. </li>
 * <li> When the background task is done indexing, it calls {@link Switchboard#swapInBackend} with the newly
 *      constructed PostingList. </li>
 * <li> {@link Switchboard#swapInBackend} calls {@link UIController#notifyBackendSwapCompleted} to tell the
 *      controller to close the waiting dialog box if it is open. </li>
 * </ol>
 */
public class Switchboard {
    private final UIController controller;
    private Backend backend;
    private final FileSource fileSource;

    /**
     * Creates a switchboard and starts the background process that re-indexes the file system.
     */
    public Switchboard(UIController controller, Backend backend, FileSource fileSource) {
        this.controller = controller;
        this.backend = backend;
        this.fileSource = fileSource;
        this.fileSource.setSwitchboard(this);
        backend.generateFromFilesystem(this);
    }

    /**
     * Shuts down the backend.
     */
    public void shutDown() {
        try {
            backend.close();
        } catch (Exception e) {
            controller.showErrorDialog("Unable to write the PostingList to a file.");
        }
    }

    /**
     * Swaps out the old backend for the new one and notifies the controller.
     *
     * @implNote It uses {@link Platform#runLater} because the controller needs to update the UI thread.  Calling it
     * without this causes an exception.
     * @param backend the new backend to swap in
     */
    public void swapInBackend(Backend backend) {
        this.backend = backend;
        Platform.runLater(controller::notifyBackendSwapCompleted);
    }

    /**
     * Queries the backend with search string and a filter.
     *
     * @param query the search query
     * @param filter the query filter
     * @return the results of the query
     */
    public Stream<String> search(String query, QueryFilter filter) {
        return backend.get(query, filter);
    }

    /**
     * Queries the backend with only a filter.
     *
     * @param filter the query filter
     * @return the results of the query
     */
    public Stream<String> search(QueryFilter filter) {
        return backend.get("/", filter);
    }

    /**
     * Gets a list of all the directories beneath all the roots specified in the filter.
     *
     * @param filter the filter containing root directories
     * @return the stream of directories
     */
    public Stream<String> getDirectories(QueryFilter filter) {
        return backend.getDirectories(filter);
    }

    /**
     * Opens the file using the system's default program for the file's type.
     *
     * @param filePath the path of the file to open
     */
    public void openFile(String filePath) {
        fileSource.openFile(Paths.get(filePath));
    }

    /**
     * Deletes each of the selected items from the file source and the backend.
     *
     * @param selectedItems the items to delete
     */
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

    /**
     * Renames each of the selected items in the file source and the backend.
     *
     * @param selectedItems the items to rename
     * @param newName the new name to give the items
     */
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

    /**
     * Moves each of the selected items to a new directory in the file source and the backend.
     *
     * @param selectedItems the items to move
     * @param destinationPath the directory to move the items to
     */
    public void move(ObservableList<String> selectedItems, String destinationPath) {
        selectedItems.forEach(item -> {
            try {
                Path itemPath = Paths.get(item);
                String itemName = itemPath.getFileName().toString();
                Path destPath = Paths.get(destinationPath, itemName);
                fileSource.moveFile(itemPath, destPath);
                backend.moveFile(item, destinationPath);
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

