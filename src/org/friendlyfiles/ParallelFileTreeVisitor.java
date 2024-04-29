package org.friendlyfiles;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.*;
import java.util.stream.Stream;

/**
 * Defines an object that walks the file tree in parallel; it retrieves file information and passes it to a method.
 * It is defined as a functional interface for convenience.  (e.g. {@link PostingList#generateFromFilesystem(Switchboard)
 * PostingList.generateFromFilesystem})
 * <p>
 * Although {@link #walk(Path)} is a default method, it should NOT be overridden.  It is defined as such to allow this
 * interface to be a functional interface.
 */
@FunctionalInterface
public interface ParallelFileTreeVisitor {
    ExecutorService exec = Executors.newWorkStealingPool();

    /**
     * The operation to be performed on every file that is visited.
     *
     * @param path the path of the file being visited
     * @param size the size of the file being visited
     */
    void op(String path, long size, long timestamp);

    /**
     * Starts the walker after it has been defined.
     *
     * @implNote collates the results and calls {@link #op} on each of them
     * @param topPath the path of the top of the file tree to walk
     */
    default void walk(Path topPath) {
        LinkedTransferQueue<FileModel> result = new LinkedTransferQueue<>();
        walkUpperTree(result, topPath);
        try {
            while (true) {
                FileModel res = result.poll(500, TimeUnit.MILLISECONDS);
                if (res == null) break;
                op(res.path, res.size, res.timestamp);
            }
        } catch (InterruptedException e) {
            throw new Error(e);
        }
    }

    /**
     * Divides the directories to walk among threads and starts walking the file tree.
     *
     * @param result the queue into which to put each file's information
     * @param topPath the path of the top of the file tree to walk
     */
    static void walkUpperTree(LinkedTransferQueue<FileModel> result, Path topPath) {
        try (Stream<Path> paths = Files.list(topPath)) {
            Stream<Path> pathStream;
            // We don't want to index the running processes because they are volatile and not useful to the user.
            if (System.getProperty("os.name").equals("Linux")) {
                pathStream = paths.filter(p -> !p.equals(Paths.get("/proc")));
            } else {
                pathStream = paths;
            }
            // Assigns one thread to each direct child directory beneath `topPath`.
            pathStream.forEach(p -> {
                try {
                    if (Files.isDirectory(p) && !Files.isSymbolicLink(p)) {
                        exec.submit(() -> walkLowerTree(result, p));
                        result.add(new FileModel(p.toString(), -1L, Files.getLastModifiedTime(p).toInstant().getEpochSecond()));
                    } else {
                        result.add(new FileModel(p.toString(), Files.size(p), Files.getLastModifiedTime(p).toInstant().getEpochSecond()));
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    /**
     * The walker task for each thread.
     * 
     * @param result the queue into which to put each file's information
     * @param topPath the path of the top of the file tree to walk
     */
    static void walkLowerTree(LinkedTransferQueue<FileModel> result, Path topPath) {
        try {
            Files.walkFileTree(topPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    return attrs.isSymbolicLink() ? FileVisitResult.SKIP_SUBTREE : FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (attrs.isRegularFile()) {
                        try {
                            result.add(new FileModel(file.toString(), Files.size(file), Files.getLastModifiedTime(file).toInstant().getEpochSecond()));
                        } catch (IOException e) {
                            throw new Error(e);
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                    try {
                        result.add(new FileModel(dir.toString(), -1L, Files.getLastModifiedTime(dir).toInstant().getEpochSecond()));
                    } catch (IOException e) {
                        throw new Error(e);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new Error(e);
        }
    }
}
