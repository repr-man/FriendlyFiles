package org.friendlyfiles;

import javafx.util.Pair;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.*;
import java.util.stream.Stream;

@FunctionalInterface
public interface ParallelFileTreeVisitor {
    ExecutorService exec = Executors.newWorkStealingPool();

    void op(String path, long size);

    default void walk(Path top) {
        LinkedTransferQueue<Pair<String, Long>> result = new LinkedTransferQueue<>();
        walkUpperTree(result, top);
        try {
            while (true) {
                Pair<String, Long> res = result.poll(10, TimeUnit.MILLISECONDS);
                if (res == null) break;
                op(res.getKey(), res.getValue());
            }
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    default void walkUpperTree(LinkedTransferQueue<Pair<String, Long>> result, Path path) {
        try (Stream<Path> paths = Files.list(path)) {
            Stream<Path> pathStream;
            // We don't want to index the running processes because they are volatile and not useful to the user.
            if (System.getProperty("os.name").equals("Linux")) {
                pathStream = paths.filter(p -> !p.equals(Paths.get("/proc")));
            } else {
                pathStream = paths;
            }
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

    default void walkLowerTree(LinkedTransferQueue<Pair<String, Long>> result, Path path) {
        if (Files.isDirectory(path)) {
            try {
                Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
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
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
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
}
