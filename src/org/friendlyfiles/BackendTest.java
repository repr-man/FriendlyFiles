package org.friendlyfiles;

import java.io.IOException;
import java.sql.SQLException;

import java.nio.file.*;

class BackendTest {
    public static void main(String[] args) throws IOException, SQLException {
            
            Path dbpath = Paths.get("./friendlyTest.db").toAbsolutePath();
            Files.deleteIfExists(dbpath);
            SQLiteBackend.createDatabase(dbpath);
            try(SQLiteBackend backend = new SQLiteBackend(dbpath)) {
                backend.generateAtDir(Paths.get("/home/repr/Desktop").toRealPath());
            }
    }
}
