package org.friendlyfiles;

import java.nio.file.*;

class BackendTest {
    public static void main(String[] args) {
        try {
            Path dbpath = Paths.get("./friendlyTest.db").toRealPath();
            Files.deleteIfExists(dbpath);
            SQLiteBackend.createDatabase(dbpath);
            try(SQLiteBackend backend = new SQLiteBackend(dbpath)) {
                backend.generateAtDir(Paths.get("/home/repr/Desktop").toRealPath());
            }
        } catch (Exception e) {}
    }
}
