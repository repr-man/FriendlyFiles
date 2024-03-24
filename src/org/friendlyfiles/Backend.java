package org.friendlyfiles;

import java.io.*;
import java.nio.file.*;
import java.nio.*;
import java.sql.*;
import java.util.*;
import java.util.stream.*;

import org.friendlyfiles.utils.RealPath;

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
    public void renameDirectory(RealPath oldPath, String newName);

    /**
     * Changes the name of a file.
     *
     * @param oldPath the path to the file to be renamed
     * @param newName the name to change the old name to
     * @throws Error if the directory is not registered or if the new path already exists
     */
    public void renameFile(RealPath oldPath, String newName);

    /**
     * Registers a new file or directory at the given path.  It creates any directories
     * in the path that don't exist in order to create the new item.
     *
     * @param path the path at which to add the new item
     */
    public void add(RealPath path);

    /**
     * Deletes a file or directory at the given path.
     *
     * This method assumes that the files exists.  These assumptions should be checked in
     * the ui or controller code so that they can display an error message to the user.
     *
     * @param path the path of the file to remove
     */
    public void remove(RealPath path);

    // TODO: Figure out the type we need to return from these.  We want them to be maximally efficient for the UI.
    // /**
    //  * Retrieves the names of the files in a directory and orders them alphabetically.
    //  *
    //  * @param directory the directory to retrieve items from
    //  * @return an alphabetically sorted list of items in the directory
    //  * @throws Error if the directory is not registered
    //  */
    // public ArrayList<String> getFilesAtoZ(RealPath directory);

    // /**
    //  * Retrieves the names of the files in a directory and orders them in reverse alphabetic order.
    //  *
    //  * @param directory the directory to retrieve items from
    //  * @return a reverse alphabetically sorted list of items in the directory
    //  * @throws Error if the directory is not registered
    //  */
    // public ArrayList<String> getFilesZtoA(RealPath directory);

    // /**
    //  * Retrieves the names of the directories in a directory and orders them alphabetically.
    //  *
    //  * @param directory the directory to retrieve items from
    //  * @return an alphabetically sorted list of items in the directory
    //  * @throws Error if the directory is not registered
    //  */
    // public ArrayList<String> getDirectoriesAtoZ(RealPath directory);

    // /**
    //  * Retrieves the names of the directories in a directory and orders them in reverse alphabetic order.
    //  *
    //  * @param directory the directory to retrieve items from
    //  * @return a reverse alphabetically sorted list of items in the directory
    //  * @throws Error if the directory is not registered
    //  */
    // public ArrayList<String> getDirectoriesZtoA(RealPath directory);
}
