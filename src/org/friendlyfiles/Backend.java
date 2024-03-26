package org.friendlyfiles;

import org.friendlyfiles.utils.RealPath;
import org.friendlyfiles.Switchboard.*;
import java.util.stream.Stream;

/**
 * The functions we need to interact with one of our backends.
 *
 * Make sure that you call the corresponding `FileSource` method before you call the `Backend`
 * method.  This ensures that the backend's state will still be correct if the file source's
 * method throws an exception.
 */
public interface Backend extends AutoCloseable {
    // TODO: Replace `generateAtDirectory`.  We need a function that takes some kind of iterator
    // over `RealPath`s and is generic over `FileSource`s.
    
    /**
     * Changes the name of a directory.
     *
     * @param oldPath the path to the directory to be renamed
     * @param newName the name to change the old name to
     * @throws Error if the directory is not registered or if the new path already exists
     */
    void renameDirectory(RealPath oldPath, String newName);

    /**
     * Changes the name of a file.
     *
     * @param oldPath the path to the file to be renamed
     * @param newName the name to change the old name to
     * @throws Error if the directory is not registered or if the new path already exists
     */
    void renameFile(RealPath oldPath, String newName);

    /**
     * Registers a new file or directory at the given path.  It creates any directories
     * in the path that don't exist in order to create the new item.
     *
     * @param path the path at which to add the new item
     */
    void add(RealPath path);

    /**
     * Deletes a file or directory at the given path.
     *
     * This method assumes that the files exists.  These assumptions should be checked in
     * the ui or controller code so that they can display an error message to the user.
     *
     * @param path the path of the file to remove
     */
    void remove(RealPath path);

    /**
     * Queries the backend for files.
     *
     * @param query the string with which to search the backend
     * @return the result of the query
     */
    Stream<FileModel> get(String query);
}
