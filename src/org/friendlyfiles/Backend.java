package org.friendlyfiles;


import org.roaringbitmap.RoaringBitmap;

import java.util.stream.Stream;

/**
 * The functions we need to interact with one of our backends.
 * <p>
 * Make sure that you call the corresponding `FileSource` method before you call the `Backend`
 * method.  This ensures that the backend's state will still be correct if the file source's
 * method throws an exception.
 */
public interface Backend extends AutoCloseable {
    /**
     * Changes the name of a file.
     *
     * @param oldPath the path to the file to be renamed
     * @param newName the name to change the old name to
     * @throws Error if the directory is not registered or if the new path already exists
     */
    void renameFile(String oldPath, String newName);

    /**
     * Reads necessary information from the filesystem into the backend in a background process
     * and swaps out the old data with the new data when it is done.
     */
    void generateFromFilesystem(Switchboard switchboard);

    /**
     * Registers a new file or directory at the given path.
     *
     * @param path the path at which to add the new item
     * @param size the size of the item
     */
    void add(String path, long size);

    /**
     * Deletes a file or directory at the given path.
     * <p>
     * This method assumes that the files exists.  These assumptions should be checked in
     * the ui or controller code so that they can display an error message to the user.
     *
     * @param path the path of the file to remove
     * @return true if str is not in the list; false if str was in the list and was removed
     */
    boolean remove(String path);

    /**
     * Moves a file to a different directory.
     *
     * @param source the path of the file to move
     * @param destination the path of the directory to move `source` to
     */
    void moveFile(String source, String destination);

    /**
     * Queries the backend for files.
     *
     * @param query  the string with which to search the backend
     * @param filter filters the query results
     * @return the result of the query
     */
    Stream<String> get(String query, QueryFilter filter);

    /**
     * Gets a list of all the directories beneath all the roots specified in the filter.
     *
     * @param filter the filter containing root directories
     * @return the stream of directories
     */
    Stream<String> getDirectories(QueryFilter filter);

    /**
     * Filters strings in a bit set using normal string searching.
     *
     * @param bitset the set of items to postprocess
     * @param splitQuery the strings to ensure exist in the output
     * @return a stream of file names corresponding to the results of the postprocessing
     */
    RoaringBitmap getPostprocessed(RoaringBitmap bitset, String[] splitQuery);
}
