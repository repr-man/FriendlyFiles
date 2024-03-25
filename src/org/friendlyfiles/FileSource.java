package org.friendlyfiles;

import java.io.*;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Describes all the operations that will be used when interacting with real filesystems,
 * such as the OS's filesystem, or a cloud storage API.
 *
 * These operations actually touch files; deleting or moving things will actually delete
 * or move things on the user's computer (unlike in a `Backend`).  Be careful when
 * implementing and testing them!
 */
public class FileSource {
    /**
     * Changes the name of a directory.
     *
     * @param oldPath the path to the directory to be renamed
     * @param newName the name to change the old name to
     * @throws FileAlreadyExistsException
     * @throws IOException
     */
	//@Override
    public void renameDir(Path oldPath, String newName) {
        try {
            Files.move(oldPath, oldPath.resolveSibling(newName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Changes the name of a file.
     *
     * @param oldPath the path to the file to be renamed
     * @param newName the name to change the old name to
     * @throws DirectoryNotEmptyException
     * @throws FileAlreadyExistsException
     * @throws IOException
     */
    //@Override
    public void renameFile(Path oldPath, String newName) {
        try {
            Path newPath = oldPath.resolveSibling(newName);
            Files.move(oldPath, newPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Deletes a file at the given path.
     *
     * This method assumes that the file exists and that it is not a directory.
     *
     * @param path the path of the file to remove
     * @throws NoSuchFileException
     * @throws IOException
     */
    //@Override
    public void remove(Path path) throws NoSuchFileException, IOException {
        try {
            Files.delete(path);
        } catch (NoSuchFileException e) {
            throw e;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Removes the entire tree of the file system beneath the given path.
     *
     * This method deletes the directory passed in and all the files and
     * directories inside of it.
     * 
     * @param top the directory to remove
     * @throws NoSuchFileException
     * @throws IOException
     */
   // @Override
    public void rmrf(Path top) throws NoSuchFileException, IOException {
        try {
            Files.walkFileTree(top, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (NoSuchFileException e) {
            throw e;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

@FunctionalInterface
interface ParallelFileTreeVisitor {
    ExecutorService exec = Executors.newWorkStealingPool();

    void op(String path);

    default void walk(Path top) {
        LinkedTransferQueue<String> result = new LinkedTransferQueue<>();
        walkUpperTree(result, top);
        try {
            while (true) {
                String res = result.poll(5, TimeUnit.MILLISECONDS);
                if (res == null) break;
                op(res);
            }
        } catch (Exception e) { throw new Error(e); }
    }

    private static void walkUpperTree(LinkedTransferQueue<String> result, Path path) {
        try (Stream<Path> paths = Files.list(path)) {
            paths.forEach(p -> {
                if (Files.isDirectory(p)) {
                    exec.submit(() -> walkLowerTree(result, p));
                } else {
                    result.add(p.toString());
                }
            });
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    private static void walkLowerTree(LinkedTransferQueue<String> result, Path path) {
        if (Files.isDirectory(path)) {
            try {
                Files.walkFileTree(path, new SimpleFileVisitor<>(){
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        result.add(file.toString());
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
        } else {
            result.add(path.toString());
        }
    }
}
