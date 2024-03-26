package org.friendlyfiles;

import java.nio.file.*;
import java.util.*;

import org.friendlyfiles.utils.RealPath;

import java.io.*;
import java.lang.*;

/**
 * A container for organizing metadata about files in a single directory.  It keeps
 * the metadata sorted in multiple ways at once to allow for efficient querying and
 * retrieval.  This is implemented with indices and free lists.
 */
class FileBucket implements Serializable {
    private static final long serialVersionUID = 7;

    // When a file is removed, its information will be removed from all the arrays
    // below at the same index.  This means that we only need one free list for
    // all these arrays.  We use the `sizes` array because it contains integers.
    private final ArrayList<String> names = new ArrayList<>();
    private final ArrayList<Long> sizes = new ArrayList<>();
    private int freeData = Integer.MIN_VALUE;

    private final ArrayList<Integer> namesIndex = new ArrayList<>();
    private final ArrayList<Integer> sizesIndex = new ArrayList<>();

    /**
     * Logs debug info about the bucket.
     *
     * @param prefix a string representation of the directory corresponding to the bucket
     * @return the string to log
     */
    public final String log(String prefix) {
        StringBuilder s = new StringBuilder();
        return log(s, prefix);
    }
    
    /**
     * Logs debug info to a `StringBuilder`.
     *
     * @param s the builder into which the data is logged
     * @param prefix a string representation of the directory corresponding to the bucket
     * @return the string to log
     */
    public final String log(StringBuilder s, String prefix) {
        for(int i = 0; i < names.size(); ++i) {
            int idx = namesIndex.get(i);

            String name = names.get(idx);
            if(name != null) {
                s.append(String.format("%10d || ", sizes.get(idx)))
                    .append(prefix)
                    .append(name)
                    .append('\n');
            }
        }
        return s.toString();
    }

    /**
     * Logs debug info about the indices to a `StringBuilder`.
     *
     * @param s the builder into which the data is logged
     * @return the fully built string
     */
    public final String logIdxs(StringBuilder s) {
        s.append('\n');
        for (int i : namesIndex) {
            s.append(String.format("%2d, ", i));
        }
        return s.toString();
    }

    /**
     * Adds a file to the bucket.
     *
     * @param name path to the file to add
     * @return the bucket
     */
    FileBucket add(RealPath path) {
        String name = path.getFileName().toString();
        long fileSize = 0;

        int searchResult = binarySearchWithIndex(names, namesIndex, name);
        if(searchResult >= 0) return this;

        try {
            fileSize = path.size();
        } catch (Exception e) {
            throw new Error(e);
        }

        int dataInsertionIdx;
        if(freeData == Integer.MIN_VALUE) {
            dataInsertionIdx = names.size();
            names.add(name);
            sizes.add(fileSize);
        } else {
            dataInsertionIdx = freeData;
            names.set(dataInsertionIdx, name);
            sizes.set(dataInsertionIdx, fileSize);
            freeData = (int)(long) sizes.get(freeData);
        }

        namesIndex.add(-(searchResult + 1), dataInsertionIdx);
        searchResult = binarySearchWithIndex(sizes, sizesIndex, fileSize);
        searchResult = searchResult >= 0 ? searchResult : -(searchResult + 1);
        sizesIndex.add(searchResult, dataInsertionIdx);
        return this;
    }

    /**
     * Removes a file from the bucket.
     *
     * @param name the name of the file to remove
     * @return the bucket
     */
    FileBucket remove(String name) {
        // Naming things is hard...
        int searchResult = binarySearchWithIndex(names, namesIndex, name);

        int dataRemovalIdx = namesIndex.get(searchResult);
        names.set(dataRemovalIdx, null);
        sizes.set(dataRemovalIdx, (long) freeData);
        freeData = dataRemovalIdx;

        namesIndex.remove(searchResult);
        searchResult = binarySearchWithIndex(sizes, sizesIndex, sizes.get(dataRemovalIdx));
        sizesIndex.remove(searchResult);

        return this;
    }

    /**
     * Performs a binary search on the names of the files using the
     * sorted name index array.
     *
     * @param name the name of the file to search for
     */
    private static <T extends Comparable<T>>
    int binarySearchWithIndex(ArrayList<T> dataArr, ArrayList<Integer> indexArr, T val) {
        int start = 0;
        int end = indexArr.size();
        int i = (end - start) >> 1;
        while (true) {
            if (end - start <= 0) {
                return -i - 1;
            }
            T curr = dataArr.get(indexArr.get(i));
            int result = val.compareTo(curr);
            if(result == 0) {
                return i;
            } else if(result < 0) {
                end = i;
                i = ((end - start) >> 1) + start;
            } else {
                start = i + 1;
                i = ((end - start) >> 1) + start;
            }
        }
    }
    
}

/**
 * A backend that represents the file system with a trie.
 */
class FileTrie implements Serializable {
    private static final long serialVersionUID = 5;
    public final TreeMap<String, FileTrie> directories = new TreeMap<>();
    FileBucket files = new FileBucket();

    FileTrie() {}

    /**
     * Logs debug info to stderr.
     */
    public final void log() {
        StringBuilder s = new StringBuilder();
        log(s, "");
        System.err.println(s);
    }

    /**
     * Helper for `log`.
     */
    private final void log(StringBuilder s, String prefix) {
        directories.forEach((name, trie) -> {
        	String fullName = prefix + '/' + name;
            s.append(fullName).append('\n');
            trie.log(s, fullName);
        });
        files.log(s, prefix + "/");
        files.logIdxs(s);
    }

    /**
     * Moves a file or directory to another file or directory, 
     */
    public final void move(RealPath oldPath, RealPath newPath) {
        if(oldPath.isDirectory() && newPath.isDirectory()) {
            Iterator<Path> oldIter = oldPath.iterator();
            Iterator<Path> newIter = newPath.iterator();
        } else if(!oldPath.isDirectory() && !newPath.isDirectory()) {
            Iterator<Path> oldIter = oldPath.getParent().iterator();
            Iterator<Path> newIter = newPath.getParent().iterator();
            FileBucket oldBucket = add(oldIter);
            FileBucket newBucket = add(newIter);
            // TODO: Figure out how to retrieve the file metadata from the bucket.
        } else {
            throw new Error("Tried to move a directory to a file, or vice-versa.  Either way, this scenerio should have caused an error sooner.");
        }
    }

    /**
     * Converts a string to a `RealPath` and adds it to the trie.
     *
     * @param str a string representation of the path to add
     * @throws IOException if `str` cannot be converted to a real path
     */
    @Deprecated
    public final void add(String str) throws IOException {
        add(RealPath.create(Paths.get(str)));
    }

    /**
     * Adds a `RealPath` to the file trie.
     *
     * @param path the path to add
     */
    public final void add(RealPath path) {
        if(path.isDirectory()) {
            Iterator<Path> iter = path.iterator();
            add(iter);
        } else {
            Iterator<Path> iter = path.getParent().iterator();
            FileBucket bucket = add(iter);
            bucket.add(path);
        }
    }

    /**
     * Adds the directory portion of a path to the trie.
     *
     * @param iter an iterator over the path segments to add
     */
    private final FileBucket add(Iterator<Path> iter) {
        if(!iter.hasNext())
            return files;

        String next = iter.next().toString();
        FileTrie nextTrie = directories.get(next);
        if(nextTrie == null) {
            nextTrie = new FileTrie();
            directories.put(next, nextTrie);
        }
        return nextTrie.add(iter);
    }

    /**
     * Removes a `RealPath` from the file trie.
     *
     * @param path the path to remove
     */
    public final void remove(RealPath path) {
        if(path.isDirectory()) {
            Iterator<Path> iter = path.getParent().iterator();
            TreeMap<String, FileTrie> trie = directories;
            while (trie != null && iter.hasNext()) {
                trie = trie.get(iter.next().toString()).directories;
            }
            if(!iter.hasNext()) {
                trie.remove(path.getFileName().toString());
            }
        } else {
            Iterator<Path> iter = path.getParent().iterator();
            FileTrie trie = this;
            while (trie != null && iter.hasNext()) {
                trie = trie.directories.get(iter.next().toString());
            }
            if(!iter.hasNext()) {
                trie.files.remove(path.getFileName().toString());
            }
        }
    }

}


/**
 * Represents the filesystem with a trie.  Probably deprecated.
 */
public class TrieBackend implements Backend, AutoCloseable, Serializable {
    private static final long serialVersionUID = 8;
    private transient String location;
    private final FileTrie directories = new FileTrie();
    
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
    public final void close() throws FileNotFoundException, IOException {
        try(ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(location))) {
            os.writeObject(this);
        }
    }

    /**
     * Returns the backing storage file as a `Path`.
     * 
     * @return the path to the backend storage file
     */
    public final Path getLocation() {
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
    public final void rename(RealPath oldPath, RealPath newPath) {
        directories.move(oldPath, newPath);
    }

    /**
     * Registers a new file or directory at the given path.  It creates any directories
     * in the path that don't exist in order to create the new item.
     *
     * @param path the path at which to add the new item
     */
    public final void add(RealPath path) {
        directories.add(path);
    }

    /**
     * Deletes a file or directory at the given path.
     * <p>
     * This method assumes that the files exists.  These assumptions should be checked in
     * the ui or controller code so that they can display an error message to the user.
     *
     * @param path the path of the file to remove
     * @return
     */
    public final boolean remove(RealPath path) {
        directories.remove(path);
        return false;
    }

}
