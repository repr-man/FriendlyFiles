package org.friendlyfiles;

import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.*;

/**
 * Describes all the operations that will be used when interacting with real filesystems,
 * such as the OS's filesystem, or a cloud storage API.
 * <p>
 * These operations actually touch files; deleting or moving things will actually delete
 * or move things on the user's computer (unlike in a `Backend`).  Be careful when
 * implementing and testing them!
 */
public class FileSource {
    private Switchboard switchboard;

    /**
     * Sets the switchboard so that the FileSource can send error messages to the user.
     *
     * @see #openFile(Path)
     * @param switchboard the switchboard used for error handling
     */
    // We are not able to set this in the constructor because the FileSource is one of the parameters of
    // Switchboard's constructor.  In other words, FileSources get created before the Switchboard, so they
    // have to be linked up to the Switchboard after it is created.
    public void setSwitchboard(Switchboard switchboard) {
        this.switchboard = switchboard;
    }

    /**
     * Changes the name of a file.
     *
     * @param oldPath the path to the file to be renamed
     * @param newName the name to change the old name to
     * @throws DirectoryNotEmptyException if directory is not empty
     * @throws FileAlreadyExistsException if the new file name already exists
     * @throws IOException                if an I/O error occurs
     */
    //@Override
    public void renameFile(Path oldPath, String newName) throws DirectoryIteratorException, FileAlreadyExistsException, IOException {
        Path newPath = oldPath.resolveSibling(newName);
        Files.move(oldPath, newPath);
    }

    /**
     * Deletes a file at the given path.
     * <p>
     * This method assumes that the file exists and that it is not a directory.
     *
     * @param path the path of the file to remove
     * @throws NoSuchFileException if the given file does not exist
     * @throws IOException         if other I/O errors occur
     */
    //@Override
    public void remove(Path path) throws NoSuchFileException, IOException {
        Files.delete(path);
    }

    /**
     * Moves a file from one location to another.
     *
     * @param source the file to move
     * @param destination the location to move `source` to
     * @throws IOException if the operation is not possible
     */
    public void moveFile(Path source, Path destination) throws IOException {
        Files.move(source, destination);
    }

    /**
     * Opens the file using the system's default program for the file's type.
     * This method uses {@link #switchboard} for logging exceptions.
     *
     * @param path the path of the file to open
     */
    public void openFile(Path path) {
        Desktop desktop;
        try {
            desktop = Desktop.getDesktop();
            File file = path.toFile();

            // Because this gets run in a different thread, we can't catch any exceptions it throws.  Thus, we set
            // its exception handler to throw errors to the switchboard.
            Thread thread = Executors.defaultThreadFactory().newThread(() -> {
                try {
                    desktop.open(file);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            thread.setUncaughtExceptionHandler((ignored, exception) -> {
                Exception ex = (Exception) exception.getCause();
                if (ex == null)
                    switchboard.showErrorDialog("An unknown error ocurred when trying to open a file.");
                else if (ex instanceof IllegalArgumentException)
                    switchboard.showErrorDialog(String.format("The file `%s` does not exist.", path));
                else if (ex instanceof UnsupportedOperationException)
                    switchboard.showErrorDialog("Your platform does not have the ability to open files.");
                else if (ex instanceof SecurityException)
                    switchboard.showErrorDialog(String.format("You do not have permission to open file `%s`.", path));
                else if (ex instanceof IOException)
                    switchboard.showErrorDialog("Your system does not have a default application for opening files of this type.");
                else
                    switchboard.showErrorDialog("An unknown error ocurred when trying to open a file.");
            });
            thread.start();
        } catch (UnsupportedOperationException e) {
            switchboard.showErrorDialog("Your platform does not support Desktop operations.");
        }
    }
}

