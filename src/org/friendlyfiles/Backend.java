package org.friendlyfiles;

import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.util.*;

// TODO:
// Figure out how we want to store metadata about the files.

/**
 * The functions we need to interact with one of our backends.
 */
public interface Backend {
    
    /**
     * Traverses all the directories in the file system beneath `top`
     * and adds them the the storage/indexing system of the backend.
     *
     * This method should be called on the first run of the program
     * before any of the file system has been indexed, or from a user-
     * triggered regeneration in the ui.
     *
     * This method assumes that it receives a valid path to a directory.
     * The ui should be able to prevent the user from selecting a
     * directory that doesn't exist or from selecting a file instead of
     * a directory.  Hence, this method terminates the program if 
     * either of these assumptions are untrue.
     *
     * @param top the top-level directory to scan
     * @throws Error
     */
    public void generateAtDir(Path top);
}

/**
 * This backend is completely Java-based.  It exists so that we can test and iterate
 * on ideas more quickly, without having to worry about writing SQL or debugging
 * database problems.  
 *
 * It's not very pretty right now, but we could probably use it as our primary
 * backend if we needed.
 *
 * Right now, this seems to be slightly faster than the SQLite backend.  We'll
 * have to see if this continues as the application grows...
 */
class BasicBackend implements Backend, Serializable, AutoCloseable {
    private transient String location;
    private HashMap<String, Integer> directories = new HashMap<>();
    private ArrayList<FileBucket> files = new ArrayList<>();

    public BasicBackend() {}
    
    /**
     * Opens the given file and deserializes it into a `BasicBackend`.
     *
     * @param location the location of the serialized blob file
     */
    public BasicBackend(Path location) throws FileNotFoundException, IOException {
        this.location = location.toRealPath(LinkOption.NOFOLLOW_LINKS).toString();
        try(ObjectInputStream is = new ObjectInputStream(new FileInputStream(this.location))) {
            BasicBackend tmp = (BasicBackend) is.readObject();
            directories = tmp.directories;
            files = tmp.files;
        } catch (ClassNotFoundException e) {
            throw new Error(e);
        }
    }

    /**
     * Creates an empty `BasicBackend`.
     *
     * TODO:
     * This should probably be added to the `Backend` interface so there is a generic way to create all
     * the backends.
     *
     * @param location the location of the serialized blob file
     * @return a new empty `BasicBackend`
     */
    public static BasicBackend create(Path location) throws IOException {
        BasicBackend tmp = new BasicBackend();
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
     * Right now, this just provided basic debugging info about the object.
     */
    @Override
    public String toString() {
        //// This is kind of broken...
        //StringBuilder sb = new StringBuilder()
        //    .append("BasicBackend[").append(location).append("]");
        //for (Map.Entry<String, Integer> entry : directories.entrySet()) {
        //    sb.append("\n    ").append(entry.getKey());
        //    for(FileBucket b : files) {
        //        sb.append(b);
        //    }
        //}
        //return sb.toString();
        return String.format("Number of buckets: %d", files.size());
    }

    /**
     * Adds a large number of files and directories from the filesystem into the backend.
     *
     * This only gets the contents from three layers of the file tree.  We will only index
     * files that the user is most likely to look at.The backend data structures will grow
     * over time as the user explores more of the filesystem.
     *
     * @param top the root node of the file tree from which to start indexing
     */
    @Override
    public void generateAtDir(Path top) {
        try {
        generateAtDir(top, 3);
        } catch (Exception e) {
            throw new Error(e);
        }
    }
    
    /**
     * Recursive helper function overload for `generateAtDir(Path)`.
     */
    private void generateAtDir(Path top_, int levels) throws IOException {
        if(levels <= 0) return;
        
        Path top = top_.toRealPath(LinkOption.NOFOLLOW_LINKS);
        FileBucket topBucket = addDirectory(top.toString());
        
        try(DirectoryStream<Path> paths = Files.newDirectoryStream(top)) {
            for(Path path : paths) {
                if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
                    addDirectory(path.toString());
                    generateAtDir(path, levels - 1);
                } else {
                    topBucket.add(path);
                }
            }
        }
    }

    /**
     * Adds a new file path to the `directories` table and a new bucket to the
     * `files` array if the given path has not yet been registered, or returns
     * the existing one if it has been registered.
     *
     * @param path the path to add to the directory
     */
    private FileBucket addDirectory(String path) {
        Integer idx = directories.get(path);
        if(idx == null) {
            directories.put(path, files.size());
            files.add(new FileBucket());
            return files.get(files.size() - 1);
        }
        return files.get(idx);
    }

    /**
     * A wrapper class for automating some common tasks and providing a fluent
     * interface for the table.
     */
    static class FileBucket implements Serializable {
        HashSet<String> items = new HashSet<>();

        // Warning: produces a LOT of output!
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (String string : items) {
                sb.append("\n        ").append(string);
            }
            return sb.toString();
        }

        /**
         * Removes all the items from the bucket.
         */
        FileBucket reset() {
            items.clear();
            return this;
        }

        /**
         * Adds a file to the bucket.
         *
         * @param name the name of the file to add
         */
        FileBucket add(String name) {
            items.add(name);
            return this;
        }

        /**
         * Adds the file pointed to by a `Path` to the bucket.
         * Be sure not to pass in a path to a directory.
         *
         * @param name the path to the file to add
         */
        FileBucket add(Path name) {
            items.add(name.getFileName().toString());
            return this;
        }

        /**
         * Removes a file name from the bucket.
         *
         * @param name the name of the file to remove
         */
        FileBucket remove(String name) {
            items.remove(name);
            return this;
        }

    }
}
