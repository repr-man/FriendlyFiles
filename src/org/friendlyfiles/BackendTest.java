package org.friendlyfiles;

import java.io.*;
import java.sql.*;
import java.nio.file.*;

class BackendTest {
    public static void main(String[] args) throws IOException, SQLException {
        // sqlTest();
        basicTest();
    }

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
