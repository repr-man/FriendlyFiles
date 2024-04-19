package org.friendlyfiles;

import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.Executors;

/**
 * Describes all the operations that will be used when interacting with real filesystems,
 * such as the OS's filesystem, or a cloud storage API.
 * <p>
 * These operations actually touch files; deleting or moving things will actually delete
 * or move things on the user's computer (unlike in a `Backend`).  Be careful when
 * implementing and testing them!
 */
public class FileSource {
    /**
     * Changes the name of a directory.
     *
     * @param oldPath the path to the directory to be renamed
     * @param newName the name to change the old name to
     * @throws FileAlreadyExistsException if the new file name already exists
     * @throws IOException                if an I/O error occurs
     */
    //@Override
    public void renameDir(Path oldPath, String newName) throws FileAlreadyExistsException, IOException {
        Files.move(oldPath, oldPath.resolveSibling(newName));
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
     * Removes the entire tree of the file system beneath the given path.
     * <p>
     * This method deletes the directory passed in and all the files and
     * directories inside of it.
     *
     * @param top the directory to remove
     * @throws IOException if an I/O error occurs
     */
    // @Override
    public void rmrf(Path top) throws IOException {
        Files.walkFileTree(top, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public void openFile(Path path) throws IOException {
        if (!Desktop.isDesktopSupported()) {
            System.err.println("Desktop operations are not supported on this platform.");
            return;
        }

        Desktop desktop = Desktop.getDesktop();
        if (!desktop.isSupported(Desktop.Action.OPEN)) {
            System.err.println("Open action is not supported on this platform.");
            return;
        }

        File file = path.toFile();
        if (file.exists()) {
            Executors.defaultThreadFactory().newThread(() -> {
                try {
                    desktop.open(file);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        } else {
            System.err.println("File does not exist: " + file.getAbsolutePath());
        }
    }
}

