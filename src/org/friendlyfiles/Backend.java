package org.friendlyfiles;

import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.util.*;

// TODO:
// Figure out how we want to store metadata about the files.

/**
 * The functions we need to interact with one of our backends.
 *
 * Make sure that you call the corresponding `FileSource` method before you call the `Backend`
 * method.  This ensures that the backend's state will still be correct if the file source's
 * method throws an exception.
 */
public interface Backend {
    
    /**
     * Adds a large number of files and directories from the filesystem into the backend.
     *
     * This only gets the contents from three layers of the file tree.  We will only index
     * 
     * files that the user is most likely to look at.The backend data structures will grow
     * over time as the user explores more of the filesystem.
     *
     * @param top the top-level directory to scan
     * @throws Error
     */
    public void generateAtDirectory(Path top);

    /**
     * Changes the name of a directory.
     *
     * @param oldPath the path to the directory to be renamed
     * @param newName the name to change the old name to
     * @throws Error if the directory is not registered or if the new path already exists
     */
    public void renameDirectory(Path oldPath, String newName);

    /**
     * Changes the name of a file.
     *
     * @param oldPath the path to the file to be renamed
     * @param newName the name to change the old name to
     * @throws Error if the directory is not registered or if the new path already exists
     */
    public void renameFile(Path oldPath, String newName);

    /**
     * Registers a new file at the given path.  It creates any directories
     * in the path that don't exist in order to create the file.
     *
     * @param path the path at which to add the new file
     */
    public void addFile(Path path);

    /**
     * Registers a new directory at the given path.  It creates any directories
     * in the path that don't exist in order to create the directory.
     *
     * @param path the path at which to add the new directory
     */
    public void addDirectory(Path path);

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
    public void removeFile(Path path);

    /**
     * Removes the entire tree of the file system beneath the given path.
     * Permanently.
     *
     * Use this only to delete directories.  It cannot delete files.  It also
     * cannot tell if the path you pass in is a file or a directory.  It is
     * the caller's responsibility to use this correctly.
     * 
     * @param top the top of the tree to nuke
     * @throws Error if the file does not exist
     */
    public void removeDirectory(Path top);

    /**
     * Retrieves the names of the files in a directory and orders them alphabetically.
     *
     * @param directory the directory to retrieve items from
     * @return an alphabetically sorted list of items in the directory
     * @throws Error if the directory is not registered
     */
    public ArrayList<String> getFilesAtoZ(Path directory);

    /**
     * Retrieves the names of the files in a directory and orders them in reverse alphabetic order.
     *
     * @param directory the directory to retrieve items from
     * @return a reverse alphabetically sorted list of items in the directory
     * @throws Error if the directory is not registered
     */
    public ArrayList<String> getFilesZtoA(Path directory);

    /**
     * Retrieves the names of the directories in a directory and orders them alphabetically.
     *
     * @param directory the directory to retrieve items from
     * @return an alphabetically sorted list of items in the directory
     * @throws Error if the directory is not registered
     */
    public ArrayList<String> getDirectoriesAtoZ(Path directory);

    /**
     * Retrieves the names of the directories in a directory and orders them in reverse alphabetic order.
     *
     * @param directory the directory to retrieve items from
     * @return a reverse alphabetically sorted list of items in the directory
     * @throws Error if the directory is not registered
     */
    public ArrayList<String> getDirectoriesZtoA(Path directory);
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
    private TreeMap<String, Integer> directories = new TreeMap<String, Integer>();
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
            freeList = tmp.freeList;
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
    public void generateAtDirectory(Path top) {
        try {
            generateAtDirectory(top, 3);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    /**
     * Recursive helper function overload for `generateAtDirectory(Path)`.
     */
    private void generateAtDirectory(Path top_, int levels) throws IOException {
        if(levels <= 0) return;
        
        Path top = top_.toRealPath(LinkOption.NOFOLLOW_LINKS);
        FileBucket topBucket = addDir(top.toString());
        
        try(DirectoryStream<Path> paths = Files.newDirectoryStream(top)) {
            for(Path path : paths) {
                if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
                    addDir(path.toString());
                    generateAtDirectory(path, levels - 1);
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
    private FileBucket addDir(String path) {
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
     * Registers a new directory at the given path.  It creates any directories
     * in the path that don't exist in order to create the directory.
     *
     * @param path the path at which to add the new directory
     */
    @Override
    public void addDirectory(Path path) {
        Path dir = path.toAbsolutePath().normalize();
        addDir(dir.getParent().toString());
    }

    /**
     * Registers a new file at the given path.  It creates any directories
     * in the path that don't exist in order to create the file.
     *
     * @param path the path at which to add the new file
     * @throws Error if the path already exists
     */
    @Override
    public void addFile(Path path) {
        Path fullPath = path.toAbsolutePath().normalize();
        FileBucket bucket = addDir(fullPath.getParent().toString());
        if(bucket.contains(fullPath)) {
            throw new Error("Tried to add a file that already exists.");
        }
        bucket.add(fullPath);
    }


    /**
     * Changes the name of a directory.
     *
     * @param oldPath the path to the directory to be renamed
     * @param newName the name to change the old name to
     */
    @Override
    public void renameDirectory(Path oldPath, String newName) {
        Path old = oldPath.toAbsolutePath().normalize();
        String oldStr = old.toString();
        Integer oldDir = directories.get(oldStr);
        if(oldDir == null) {
            throw new Error("Tried to rename a directory that does not exist.");
        }

        Path newPath = Paths.get(old.getParent().toString(), newName);
        String newStr = newPath.toString();
        if(directories.containsKey(newStr)) {
            throw new Error("Tried to give a directory a name that another directory has.");
        }
        if(files.get(oldDir).contains(newName)) {
            throw new Error("Tried to give a directory a name that another file has.");
        }

        directories.put(newStr, oldDir);
        directories.remove(oldStr);
    }

    /**
     * Changes the name of a directory.
     *
     * @param oldPath the path to the file to be renamed
     * @param newName the name to change the old name to
     */
    @Override
    public void renameFile(Path oldPath, String newName) {
        Path dir = oldPath.toAbsolutePath().normalize().getParent();
        String dirStr = dir.toString();
        Integer dirIdx = directories.get(dirStr);
        if(dirIdx == null) {
            throw new Error("Tried to rename a file in a directory that does not exist.");
        }

        Path newPath = Paths.get(dirStr, newName);
        FileBucket bucket = files.get(dirIdx);
        if(directories.containsKey(newPath.toString())) {
            throw new Error("Tried to give a file a name that another directory has.");
        }
        if(bucket.contains(newName)) {
            throw new Error("Tried to give a file a name that another file has.");
        }

        bucket.add(newName);
        bucket.remove(dir.getFileName().toString());
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
    public void removeFile(Path path) {
        if(directories.containsKey(path.toAbsolutePath().normalize().toString())) {
            throw new Error("Called `removeFile` on a directory.  Use `removeDirectory` instead.");
        }
        
        Integer dir = directories.get(path.getParent().toString());
        if(dir == null) {
            throw new Error("Tried to remove a file from a directory that doesn't exist.");
        }
        files.get(dir).remove(path.getFileName().toString());
    }

    /**
     * Removes the entire tree of the file system beneath the given path.
     *
     * Use this only to delete directories.  It cannot delete files.  It also
     * cannot tell if the path you pass in is a file or a directory.  It is
     * the caller's responsibility to use this correctly.
     * 
     * @param top the top of the tree to nuke
     */
    // TODO: Try to make this more efficient (not a linear traversal to find subdirectories).
    @Override
    public void removeDirectory(Path top) {
        String prefix = top.toString();
        if(!directories.containsKey(prefix)) {
            throw new Error("Given directory doesn't exist.");
        }

        Iterator<Map.Entry<String, Integer>> entries = directories.entrySet().iterator();
        while(entries.hasNext()) {
            Map.Entry<String, Integer> dir = entries.next();
            if(dir.getKey().startsWith(prefix)) {
                files.set(dir.getValue(), null);
                freeList.add(dir.getValue());
                entries.remove();
            }
        }
    }

    @Override
    public ArrayList<String> getFilesAtoZ(Path directory) {
        return getFiles(directory, (String s1, String s2) -> s1.compareTo(s2));
    }

    @Override
    public ArrayList<String> getFilesZtoA(Path directory) {
        return getFiles(directory, (String s1, String s2) -> -s1.compareTo(s2));
    }

    @Override
    public ArrayList<String> getDirectoriesAtoZ(Path directory) {
        return getDirectories(directory, (String s1, String s2) -> s1.compareTo(s2));
    }

    @Override
    public ArrayList<String> getDirectoriesZtoA(Path directory) {
        return getDirectories(directory, (String s1, String s2) -> -s1.compareTo(s2));
    }

    private ArrayList<String> getFiles(Path directory, Comparator<String> comp) {
        String dirStr = directory.toAbsolutePath().normalize().toString();
        Integer dir = directories.get(dirStr);
        if(dir == null) {
            throw new Error("Tried to retrieve files from a directory that doesn't exist.");
        }
        ArrayList<String> list = new ArrayList<>();
        list.addAll(files.get(dir).items);
        list.sort(comp);
        return list;
    }

    private ArrayList<String> getDirectories(Path directory, Comparator<String> comp) {
        String dirStr = directory.toAbsolutePath().normalize().toString();
        Integer dir = directories.get(dirStr);
        if(dir == null) {
            throw new Error("Tried to retrieve directories from a directory that doesn't exist.");
        }
        ArrayList<String> list = new ArrayList<>();
        dirStr += File.separator;
        for(String item : directories.keySet()) {
            if(item.startsWith(dirStr)) {
                list.add(item);
            }
        }
        list.sort(comp);
        return list;
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

        /**
         * Tests whether the bucket has a certain file name within.
         * 
         * @param name the name of the file to look for
         */
        boolean contains(String name) {
            return items.contains(name);
        }

        /**
         * Tests whether the bucket has a certain file name within.
         * 
         * @param path the path of the file to look for
         */
        boolean contains(Path path) {
            return items.contains(path.getFileName().toString());
        }
    }
}
