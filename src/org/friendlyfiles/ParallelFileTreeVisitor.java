package org.friendlyfiles;

import javafx.util.Pair;

import java.io.*;
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
    void op(String path, long size);

    /**
     * Starts the walker after it has been defined.
     *
     * @implNote collates the results and calls {@link #op} on each of them
     * @param topPath the path of the top of the file tree to walk
     */
    default void walk(Path topPath) {
        LinkedTransferQueue<Pair<String, Long>> result = new LinkedTransferQueue<>();
        walkUpperTree(result, topPath);
        try {
            while (true) {
                Pair<String, Long> res = result.poll(10, TimeUnit.MILLISECONDS);
                if (res == null) break;
                op(res.getKey(), res.getValue());
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
    static void walkUpperTree(LinkedTransferQueue<Pair<String, Long>> result, Path topPath) {
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
                if (Files.isDirectory(p) && !Files.isSymbolicLink(p)) {
                    exec.submit(() -> walkLowerTree(result, p));
                    result.add(new Pair<>(p.toString(), -1L));
                } else {
                    try {
                        result.add(new Pair<>(p.toString(), Files.size(p)));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
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
    static void walkLowerTree(LinkedTransferQueue<Pair<String, Long>> result, Path topPath) {
        try {
            Files.walkFileTree(topPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    return attrs.isSymbolicLink() ? FileVisitResult.SKIP_SUBTREE : FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (attrs.isRegularFile()) {
                        result.add(new Pair<>(file.toString(), attrs.size()));
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                    result.add(new Pair<>(dir.toString(), -1L));
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
