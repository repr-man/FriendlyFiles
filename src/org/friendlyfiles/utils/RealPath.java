package org.friendlyfiles.utils;

import java.nio.file.*;
import java.nio.file.WatchEvent.*;
import java.util.*;
import java.util.stream.*;
import java.io.*;
import java.nio.*;
import java.lang.*;
import java.net.URI;
import java.text.*;

/**
 * A type-safe real path.  A real path is an absolute path with all relative operations
 * (e.g. "./" or "../") evaluated.  This class stores a single path and ensures that it
 * is always a real path.
 *
 * This class is able to be used as a normal `Path`.  It forwards all of `Path`'s
 * methods to the `Path` instance within.
 *
 * We are unable to use inheritance because Java erases the types of generics.
 * Conceptually, this class is the following:
 * <pre>final class RealPath<T extends Path> extends T;</pre>
 * However, since the generics get erased, `RealPath` would be extending nothing.
 * Hence, we must manually forward the methods.
 */
public final class RealPath implements Path {
    private final Path path;

    private RealPath(Path path) {
        this.path = path;
    }
    
    public static RealPath create(Path path) {
        try {
            return new RealPath(path.toRealPath());
        } catch (IOException e) {
            throw new Error("Unable to convert a `Path` to a `RealPath`.", e);
        }
    }

    public static RealPath get(String path) {
        try {
            return new RealPath(Paths.get(path).toRealPath());
        } catch (IOException e) {
            throw new Error("Unable to convert a `String` to a `RealPath`.", e);
        }
    }

    public final int compareTo(Path other) {
        return path.compareTo(other);
    }

    public final boolean endsWith(Path other) {
        return path.endsWith(other);
    }

    public final boolean endsWith(String other) {
        return path.endsWith(other);
    }

    public final boolean equals(Object other) {
        return path.equals(other);
    }

    public final Path getFileName() {
        return path.getFileName();
    }

    public final FileSystem getFileSystem() {
        return path.getFileSystem();
    }

    public final Path getName(int index) {
        return path.getName(index);
    }

    public final int getNameCount() {
        return path.getNameCount();
    }

    public final Path getParent() {
        return path.getParent();
    }

    public final Path getRoot() {
        return path.getRoot();
    }

    public final int hashCode() {
        return path.hashCode();
    }

    public final boolean isAbsolute() {
        return path.isAbsolute();
    }

    public final Iterator<Path> iterator() {
        return path.iterator();
    }

    public final Path normalize() {
        return path.normalize();
    }

    public final WatchKey register(WatchService watcher, Kind<?>... events) throws IOException {
        return path.register(watcher, events);
    }

    public final WatchKey register(WatchService watcher, Kind<?>[] events, Modifier... modifiers) throws IOException {
        return path.register(watcher, events, modifiers);
    }

    public final Path relativize(Path other) {
        return path.relativize(other);
    }

    public final Path resolve(Path other) {
        return path.resolve(other);
    }

    public final Path resolve(String other) {
        return path.resolve(other);
    }

    public final Path resolveSibling(Path other) {
        return path.resolveSibling(other);
    }

    public final Path resolveSibling(String other) {
        return path.resolveSibling(other);
    }

    public final boolean startsWith(Path other) {
        return path.startsWith(other);
    }

    public final boolean startsWith(String other) {
        return path.startsWith(other);
    }

    public final Path subpath(int beginIndex, int endIndex) {
        return path.subpath(beginIndex, endIndex);
    }

    public final Path toAbsolutePath() {
        return path.toAbsolutePath();
    }

    public final File toFile() {
        return path.toFile();
    }

    public final Path toRealPath(LinkOption... options) throws IOException {
        return path.toRealPath(options);
    }

    public final String toString() {
        return path.toString();
    }

    public final URI toUri() {
        return path.toUri();
    }

    /**
     * Return a lazily populated Stream, the elements of which are the entries in the directory. The listing is not recursive.
     *
     * @return the Stream describing the content of the directory.
     * @throws NotDirectoryException if the file could not otherwise be opened because it is not a directory
     * @throws IOException if an I/O error occurs when opening the directory
     */
    public final Stream<RealPath> list() throws IOException {
        return Files.list(path).map(p -> new RealPath(p));
    }

    /**
     * Determines if the path is a directory.  Use instead of `Files.isDirectory`.
     *
     * @return whether the path is a directory
     */
    public final boolean isDirectory() {
        return Files.isDirectory(path);
    }

    /**
     * Returns the size in bytes of the file corresponding to the path.  Use instead of `Files.size`.
     *
     * @return the size of the file
     */
    public final long size() {
        try {
            return Files.size(path);
        } catch (IOException e) {
            throw new Error("Unable to retrieve the size of a file from the file system.", e);
        }
    }
}
