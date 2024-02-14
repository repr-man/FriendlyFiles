package org.friendlyfiles;

import java.io.*;
import java.nio.file.*;

/**
 * Describes all the operations that will be used when interacting with real filesystems,
 * such as the OS's filesystem, or a cloud storage API.
 *
 * These operations actually touch files; deleting or moving things will actually delete
 * or move things on the user's computer (unlike in a `Backend`).  Be careful when
 * implementing and testing them!
 */
interface FileSource {
    /**
     * Changes the name of a directory.
     *
     * @param oldPath the path to the directory to be renamed
     * @param newName the name to change the old name to
     * @throws FileAlreadyExistsException
     * @throws IOException
     */
    public void renameDir(Path oldPath, String newName);

    /**
     * Changes the name of a file.
     *
     * @param oldPath the path to the file to be renamed
     * @param newName the name to change the old name to
     * @throws DirectoryNotEmptyException
     * @throws FileAlreadyExistsException
     * @throws IOException
     */
    public void renameFile(Path oldPath, String newName);

    /**
     * Deletes a file at the given path.
     *
     * This method assumes that the file exists and that it is not a directory.
     *
     * @param path the path of the file to remove
     * @throws NoSuchFileException
     * @throws IOException
     */
    public void remove(Path path) throws NoSuchFileException, IOException;

    /**
     * Removes the entire tree of the file system beneath the given path.
     *
     * This method deletes the directory passed in and all the files and
     * directories inside of it.
     * 
     * @param top the directory to remove
     * @throws NoSuchFileException
     * @throws IOException
     */
    public void rmrf(Path top) throws NoSuchFileException, IOException;
}
