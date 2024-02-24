package org.friendlyfiles;

import java.io.*;
import java.nio.file.*;
import java.nio.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
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

/**
 * Represents the filesystem with a trie.
 */
public class TrieBackend implements Backend, AutoCloseable, Serializable {
    private static final long serialVersionUID = 8;
    private transient String location;
    private FileTrie directories = new FileTrie();
    
    public TrieBackend() {}

    /**
     * Opens the given file and deserializes it into a `TrieBackend`.
     *
     * @param location the location of the serialized blob file
     */
    public TrieBackend(Path location) throws FileNotFoundException, IOException {
        this.location = location.toRealPath(LinkOption.NOFOLLOW_LINKS).toString();
        try(ObjectInputStream is = new ObjectInputStream(new FileInputStream(this.location))) {
            TrieBackend tmp = (TrieBackend) is.readObject();
            directories = tmp.directories;
        } catch (ClassNotFoundException e) {
            throw new Error(e);
        }
    }

    /**
     * Creates an empty `TrieBackend`.
     *
     * @param location the location of the serialized blob file
     * @return a new empty `BasicBackend`
     */
    public static TrieBackend create(Path location) throws IOException {
        TrieBackend tmp = new TrieBackend();
        tmp.location = location.toRealPath(LinkOption.NOFOLLOW_LINKS).toString();
        return tmp;
    }

    /**
     * Implements the `AutoCloseable` interface so this can be used in a try-with-resources.
     * It serializes and writes the file to the location from which it was constructed.
     */
    @Override
    public void close() throws FileNotFoundException, IOException {
        try(ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(location))) {
            os.writeObject(this);
        }
    }

    /**
     * Returns the backing storage file as a `Path`.
     * 
     * @return the path to the backend storage file
     */
    public Path getLocation() {
        try {
            return Paths.get(location);
        } catch (Exception e) {
            throw new Error(e);
        }
    }
    
    /**
     * Changes the name of a file or directory.
     *
     * We assume that the paths are valid because if they weren't, the `FileSource` would
     * have produced an error.
     *
     * @param oldPath the path to be renamed
     * @param newPath the path to change the old path to
     */
    public void rename(RealPath oldPath, RealPath newPath) {
        directories.move(oldPath, newPath);
    }

    /**
     * Registers a new file or directory at the given path.  It creates any directories
     * in the path that don't exist in order to create the new item.
     *
     * @param path the path at which to add the new item
     */
    public void add(RealPath path) {
        directories.add(path);
    }

    /**
     * Deletes a file or directory at the given path.
     *
     * This method assumes that the files exists.  These assumptions should be checked in
     * the ui or controller code so that they can display an error message to the user.
     *
     * @param path the path of the file to remove
     */
    public void remove(RealPath path) {
        directories.remove(path);
    }

}
