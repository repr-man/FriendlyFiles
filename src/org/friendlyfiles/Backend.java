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
     * Adds a large number of files and directories from the filesystem into the backend.
     *
     * This only gets the contents from three layers of the file tree.  We will only index
     * files that the user is most likely to look at.The backend data structures will grow
     * over time as the user explores more of the filesystem.
     *
     * @param top the top-level directory to scan
     * @throws Error
     */
    public void generateAtDir(Path top);

    /**
     * Changes the name of a file or directory in the backend.
     *
     * @param oldPath the path to the file or directory to be renamed
     * @param newPath the path to change the old path to
     * @return true if the path was valid and was successfully renamed.
     */
    // TODO: Hammer out the interface.  Do we expect given paths to be valid?
    //       What if it is renamed to an existing path?
    public boolean rename(Path oldPath, Path newPath);

    /**
     * Deletes a file at the given path.
     *
     * This method assumes that the files exists and that it is not a directory.
     * These assumptions should be checked in the ui or controller code so that
     * they can display an error message to the user.
     *
     * @param path the path of the file to remove
     * @throws Error if the file does not exist or if it is a directory
     */
    public void remove(Path path);

    /**
     * Removes the entire tree of the file system beneath the given path.
     * Permanently.
     * 
     * @param top the top of the tree to nuke
     * @throws Error if the file does not exist
     */
    public void rmrf(Path top);
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
    private ArrayList<Integer> freeList = new ArrayList<>();

    public BasicBackend() {
    }
    
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
        return String.format("Number of buckets: %d\nSize of free list: %d", files.size(), freeList.size());
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
        
        // We have the directory already:
        if(idx != null) {
            return files.get(idx);
        }

        // We don't, and need to add it:
        if(freeList.isEmpty()) {
            files.add(new FileBucket());
            directories.put(path, files.size() - 1);
            return files.get(files.size() - 1);
        } else {
            int nextIdx = freeList.get(freeList.size() - 1);
            freeList.remove(freeList.size() - 1);
            files.add(nextIdx, new FileBucket());
            directories.put(path, nextIdx);
            return files.get(nextIdx);
        }
    }


    /**
     * Changes the name of a file or directory.
     *
     * @param oldPath the path to the file or directory to be renamed
     * @param newPath the path to change the old path to
     * @return true if the path was valid and was successfully renamed.
     */
    @Override
    public boolean rename(Path oldPath, Path newPath) {
        throw new Error("Renaming not yet implemented.");
    }

    /**
     * Deletes a file at the given path.
     *
     * This method assumes that the files exists and that it is not a directory.
     * These assumptions should be checked in the ui or controller code so that
     * they can display an error message to the user.
     *
     * @param path the path of the file to remove
     * @throws Error if the file does not exist or if it is a directory
     */
    @Override
    public void remove(Path path) {
        if(Files.exists(path, LinkOption.NOFOLLOW_LINKS) && Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
            throw new Error("Called `remove` on a directory.  Use `rmrf` instead.");
        }
        
        int dir = directories.get(path.getParent().toString());
        files.get(dir).remove(path.getFileName().toString());
    }

    /**
     * Removes the entire tree of the file system beneath the given path.
     *
     * @param top the top of the tree to nuke
     */
    // TODO: Try to make this more efficient (not a linear traversal to find subdirectories).
    @Override
    public void rmrf(Path top) {
        if(!Files.exists(top, LinkOption.NOFOLLOW_LINKS)) {
            throw new Error("Given directory doesn't exist.");
        }
        if(!Files.isDirectory(top, LinkOption.NOFOLLOW_LINKS)) {
            remove(top);
            return;
        }

        String prefix = top.toString();
        Iterator<Map.Entry<String, Integer>> entries = directories.entrySet().iterator();
        while(entries.hasNext()) {
            Map.Entry<String, Integer> dir = entries.next();
            if(dir.getKey().startsWith(prefix)) {
               System.out.println(dir.getKey());
                files.set(dir.getValue(), null);
                freeList.add(dir.getValue());
                entries.remove();
            }
        }
    }
    
    /**
     * A wrapper class for automating some common tasks and providing a fluent
     * interface for the table.
     */
    static class FileBucket implements Serializable {
        HashSet<String> items = new HashSet<>();

        // Warning: can produce a LOT of output!
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
