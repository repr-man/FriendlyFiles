package org.friendlyfiles;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import org.friendlyfiles.ui.UIController;

import java.io.IOException;
import java.nio.file.*;
import java.text.*;
import java.util.regex.*;
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
    static final Pattern sedSegmentPattern = Pattern.compile("(?<!\\\\)\\/");
    private final UIController controller;
    private PostingList backend;
    private final FileSource fileSource;

    /**
     * Creates a switchboard and starts the background process that re-indexes the file system.
     */
    public Switchboard(UIController controller, PostingList backend, FileSource fileSource) {
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
    public void swapInBackend(PostingList backend) {
        this.backend = backend;
        Platform.runLater(controller::notifyBackendSwapCompleted);
    }

    /**
     * Queries the backend with only a filter.
     *
     * @param filter the query filter
     * @return the results of the query
     */
    public Stream<String> search(QueryFilter filter) {
        return backend.get(filter);
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

    public Stream<String> allowFilesInDirectory(QueryFilter filter, String dirPath) {

        return null;
    }

    public Stream<String> toggleVisibleFiles(QueryFilter filter, String dirPath) {
        return null;
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
     * Renames each of the selected items in the file source and the backend.  It also allows one to use sed-like
     * substitutions for bulk renaming files.
     *
     * @param selectedItems the items to rename
     * @param newName the new name to give the items
     */
    public void rename(ObservableList<String> selectedItems, String newName) {
        selectedItems.forEach(item -> {
            try {
                String finalName;
                Path itemPath = Paths.get(item);
                if (newName.startsWith("s/")) {
                    if (newName.length() < 4) {
                        showErrorDialog("Invalid substitution");
                        finalName = itemPath.getFileName().toString();
                    } else {
                        finalName = miniSed(item, newName.substring(2));
                        if (finalName == null) {
                            showErrorDialog("Invalid substitution");
                            return;
                        }
                    }
                } else {
                    finalName = newName;
                }
                fileSource.renameFile(itemPath, finalName);
                backend.renameFile(item, finalName);
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


    /**
     * Provides a simple sed-like regex-based substitution.  It is used for modifying large numbers of file names
     * with a similar structure.  The string matching regex uses standard Java regex syntax.  The substitution
     * section uses standard 1-indexed backreferences.  Backslashes in this section only escape integers used for
     * backreferences.  They leave all other characters unchanged.  The only exception is for slashes and backslashes.
     * These characters cause the function to fail, as it is invalid for a name to have a path separator character
     * in it.  Flags (like the 'g' global flag) are not allowed.
     *
     * @param input the string to modify
     * @param pattern a sed-like substitution command with the "s/" and suffix flags removed
     * @return the updated string, or null if the pattern was invalid
     */
    private static String miniSed(String input, String pattern) {
        String[] sedSegments = sedSegmentPattern.split(pattern, 3);
        Matcher inputMatcher = Pattern.compile(sedSegments[0]).matcher(input.substring(input.lastIndexOf(UIController.fileSeparator) + 1));
        if (!inputMatcher.find()) {
            return null;
        }

        StringBuilder output = new StringBuilder();
        // I spent 3 hours trying to write this with a regex.  Sometimes, the simpler solution is better.
        StringCharacterIterator outputPattern = new StringCharacterIterator(sedSegments[1]);
        while (outputPattern.current() != CharacterIterator.DONE) {
            if (outputPattern.current() == '\\') {
                if (outputPattern.next() != CharacterIterator.DONE && Character.isDigit(outputPattern.current())) {
                    int start = outputPattern.getIndex();
                    int end = outputPattern.getIndex();
                    do {
                        ++end;
                    } while (outputPattern.next() != CharacterIterator.DONE && Character.isDigit(outputPattern.current()));
                    int backref = Integer.parseInt(sedSegments[1].substring(start, end));
                    try {
                        output.append(inputMatcher.group(backref));
                    } catch (IndexOutOfBoundsException ignored) {
                        return null;
                    }
                } else if (outputPattern.current() == '\\') {
                    return null;
                }
            } else if (outputPattern.current() == '/'){
                return null;
            } else {
                output.append(outputPattern.current());
                outputPattern.next();
            }
        }
        return output.toString();
    }
}

