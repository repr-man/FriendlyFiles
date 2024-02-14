package org.friendlyfiles;

import java.io.*;
import java.sql.*;
import java.nio.file.*;

class BackendTest {
    public static void main(String[] args) throws IOException, SQLException {
        // sqlTest();
        basicTest();
    }

    public static void sqlTest() throws IOException, SQLException {
        Path dbpath = Paths.get("./friendlyTest.db").toAbsolutePath();
        Files.deleteIfExists(dbpath);
        SQLiteBackend.createDatabase(dbpath);
        try(SQLiteBackend backend = new SQLiteBackend(dbpath)) {
            backend.generateAtDir(Paths.get("/home/repr/Desktop").toRealPath());
            boolean res = backend.addAtDir(Paths.get("/home/repr/Desktop/FriendlyFiles/src/org/friendlyfiles").toRealPath());
            System.out.println(res);
        }
    }

    public static void basicTest() throws IOException, SQLException {
        Path backendPath = Paths.get("./friendlyTest.blob").toAbsolutePath();
        Files.deleteIfExists(backendPath);
        Files.createFile(backendPath);
        try (BasicBackend backend = BasicBackend.create(backendPath)) {
            backend.generateAtDir(Paths.get("/home/repr/Desktop").toRealPath());
            System.out.println(backend);
            backend.rmrf(Paths.get("/home/repr/Desktop/Rust").toRealPath());
            System.out.println(backend);
            backend.generateAtDir(Paths.get("/home/repr/Desktop/Ko").toRealPath());
            System.out.println(backend);
            backend.getFilesZtoA(Paths.get("/home/repr/Desktop/Ko/bootstrap")).forEach(item -> System.out.println(item));
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
