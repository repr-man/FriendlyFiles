package org.friendlyfiles.testing;

import java.io.*;
import java.sql.*;
<<<<<<< HEAD:src/org/friendlyfiles/testing/BackendTest.java
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.friendlyfiles.FileTrie;
import org.friendlyfiles.utils.RealPath;

=======
import java.util.*;
import java.util.stream.*;
>>>>>>> f01777a (Simplified imports):src/org/friendlyfiles/BackendTest.java
import java.nio.file.*;
import java.nio.file.attribute.*;

class BackendTest {
    public static void main(String[] args) throws IOException, SQLException {
        FileTrie trie = new FileTrie();
        RealPath.get("/home/repr/Desktop").list().forEach(path -> {
            try {
                trie.add(path);
            } catch(Exception e) {
                trie.log();
                throw new Error(e);
            }
        });
        RealPath.get("/home/repr/Desktop").list()
            .filter(path -> path.isDirectory())
            .forEach(path -> {
                trie.remove(path);
            });
        trie.log();
    }

    public static void walkTest() {
        try(BasicBackend backend = new BasicBackend(Paths.get("/home/repr/Desktop/FriendlyFiles/bin/stuff.blob"))) {
            //try (Stream<Path> dirs = Files.list(Paths.get("/home/repr/Desktop"))) {
            //    dirs.forEach(path -> {
            //        try (Stream<Path> dirst = Files.walk(path, 3)) {
            //            dirst
            //                .filter(item -> !Files.isDirectory(item))
            //                .forEach(item -> {
            //                        backend.addFile(item);
            //                });
            //        } catch (Exception e) {
            //            //throw new Error(e);
            //            // TODO: handle exception
            //        }
            //    });
            //} catch (Exception e) {
            //    //throw new Error(e);
            //    // TODO: handle exception
            //}
            System.out.println(backend);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

//    public static void walkerTest() {
//        try(BasicBackend backend = BasicBackend.create(Paths.get("/home/repr/Desktop/FriendlyFiles/bin/stuff.blob"))) {
//            Files.walkFileTree(Paths.get("/"), new FileVisitor<Path>() {
//                public FileVisitResult visitFile(Path path, java.nio.file.attribute.BasicFileAttributes arg1) throws IOException {
//                    try {
//                        backend.addFile(path);
//                        System.out.println(path);
//                        return FileVisitResult.CONTINUE;
//                    } catch (Exception e) {
//                        return FileVisitResult.SKIP_SUBTREE;
//                    }
//                };
//
//                @Override
//                public FileVisitResult postVisitDirectory(Path arg0, IOException arg1) throws IOException {
//                    return FileVisitResult.CONTINUE;
//                }
//
//                @Override
//                public FileVisitResult visitFileFailed(Path arg0, IOException arg1) throws IOException {
//                    return FileVisitResult.SKIP_SUBTREE;
//                }
//
//                @Override
//                public FileVisitResult preVisitDirectory(Path arg0, BasicFileAttributes arg1) throws IOException {
//                    return FileVisitResult.CONTINUE;
//                }
//            });
//            backend.log();
//        } catch (Exception e) {
//            throw new Error(e);
//        }
//
//    }
    
//    public static void sqlTest() throws IOException, SQLException {
//        Path dbpath = Paths.get("./friendlyTest.db").toAbsolutePath();
//        Files.deleteIfExists(dbpath);
//        SQLiteBackend.createDatabase(dbpath);
//        try(SQLiteBackend backend = new SQLiteBackend(dbpath)) {
//            backend.generateAtDirectory(Paths.get("/home/repr/Desktop").toRealPath());
//            boolean res = backend.addDirectory(Paths.get("/home/repr/Desktop/FriendlyFiles/src/org/friendlyfiles").toRealPath());
//            System.out.println(res);
//        }
//    }

    public static void basicTest() throws IOException, SQLException {
        Path backendPath = Paths.get("./friendlyTest.blob").toAbsolutePath();
        Files.deleteIfExists(backendPath);
        Files.createFile(backendPath);
        try (BasicBackend backend = BasicBackend.create(backendPath)) {
            backend.generateAtDirectory(Paths.get("/home/repr/Desktop").toRealPath());
            //System.out.println(backend);
            backend.removeDirectory(Paths.get("/home/repr/Desktop/Rust").toRealPath());
            //System.out.println(backend);
            backend.generateAtDirectory(Paths.get("/home/repr/Desktop/Ko").toRealPath());
            //System.out.println(backend);
            //backend.getFilesZtoA(Paths.get("/home/repr/Desktop/Ko/bootstrap")).forEach(item -> System.out.println(item));
            Path buf = Paths.get("/");
            for(Path item : Paths.get("/home/repr/Desktop/Ko/bootstrap/src")) {
                buf = buf.resolve(item);
                System.out.println(buf);
            }
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
