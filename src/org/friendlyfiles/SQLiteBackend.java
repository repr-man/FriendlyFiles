package org.friendlyfiles;

import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.io.*;

/**
 * The schema for the database is as follows:
 * ```sql
 * CREATE TABLE directories(path STRING);
 * CREATE TABLE files(dir INTEGER, name STRING);
 * CREATE TABLE tags(tag STRING);
 * ```
 */
class SQLiteBackend implements Backend, AutoCloseable {
    private Connection conn;

    /**
     * Connects to a SQLite database to be used for storing file metadata.
     * The database should be located in the standard location for application
     * data appropriate to the operating system.  Since this location is known
     * ahead of time, this method terminates the program if the db is not found.
     * 
     * @param dbLocation the location of the database
     * @throws Error
     */
    public SQLiteBackend(Path location) {
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:" + location.toAbsolutePath().toString());
            conn.createStatement().execute("PRAGMA synchronous = normal; PRAGMA temp_store = memory;");
        } catch (Exception e) {
            throw new Error("Unable to open the SQLite database at \"" + location.toAbsolutePath() + "\".", e);
        }
    }

    /**
     * Creates the backend database file and creates the needed tables within the file.
     *
     * @param location the location to create the new database
     * @throws FileAlreadyExistsException if the caller has not deleted the old database at `location`
     * @throws IOException if `location` doesn't exist or it can't create the file
     */
    public static void createDatabase(Path location) throws IOException, FileAlreadyExistsException {
        try {
            Files.createFile(location.toAbsolutePath());
            Connection conn = DriverManager.getConnection("jdbc:sqlite:" + location.toAbsolutePath().toString());
            Statement createStmt = conn.createStatement();
            createStmt.addBatch("CREATE TABLE directories(path STRING UNIQUE);");
            createStmt.addBatch("CREATE TABLE files(dir INTEGER, name STRING);");
            createStmt.addBatch("CREATE TABLE tags(tag STRING);");
            createStmt.executeBatch();
            conn.close();
        } catch (SQLException e) {
            // The connection should be able to be created, and all the SQL statements should be correct.
            throw new Error("Unreachable", e);
        }
    }

    /**
     * Implementation of the `AutoClosable` interface so it can be used in a try-with-resources statement.
     */
    @Override
    public void close() throws SQLException {
        conn.createStatement().execute("PRAGMA optimize;");
        conn.close();
    }

    boolean addAtDir(Path top) throws SQLException, IOException {
        ResultSet res = conn.createStatement().executeQuery("SELECT rowid FROM directories WHERE path = \"" + top.toRealPath(LinkOption.NOFOLLOW_LINKS) + "\";");
        return res.next();
    }

    /**
     * Adds a large number of files and directories from the filesystem into the database.
     *
     * This only gets the contents from three layers of the file tree.  I initially tried
     * getting all the files beneath `top`.  However, experiments on my desktop directory
     * showed that it took about a minute to fully index all the files.  Given that this
     * is only a fraction of the files on my machine, we cannot use this strategy.
     * Instead, we will only index the files that the user is most likely to look at.
     * The database will grow over time as they explore more of the filesystem.
     *
     * @param top the root node of the file tree from which to start indexing
     */
    @Override
    public void generateAtDir(Path top) {
        try {
            Path topPath = top.toRealPath(LinkOption.NOFOLLOW_LINKS);
            
            Statement stmt = conn.createStatement();
            StringBuilder addDirStmt = new StringBuilder("INSERT INTO directories(path) VALUES");
            addDirStmt.append("('").append(topPath.toString()).append("')");
            StringBuilder addFileStmt = new StringBuilder("INSERT INTO files(path) VALUES");
            addItemsToStatement(addDirStmt, Files.newDirectoryStream(topPath));
            addDirStmt.append(';');
            stmt.execute(addDirStmt.toString());
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    /**
     * Calls `addItemsToStatement` with the default number of layers.
     */
    private static void addItemsToStatement(StringBuilder stmt, DirectoryStream<Path> iter) {
        addItemsToStatement(stmt, iter, 2);
    }
    
    /**
     * Recursively walks all the directories provided by `iter`, with a depth limit of `layers`, and
     * adds each path to the SQL statement given by `dirStmt`.
     *
     * @param dirStmt the statement to insert directory file paths into
     * @param fileStmt the statement to insert directory file paths into
     * @param iter the directories to walk
     * @param layers the maximum traversal depth
     */
    private static void addItemsToStatement(StringBuilder dirStmt,  DirectoryStream<Path> iter, int layers) {
        try {
            boolean shouldRecurse = layers > 0;
            for (Path item : iter) {
                if (Files.isDirectory(item)) {
                    dirStmt.append(",('").append(item.toRealPath(LinkOption.NOFOLLOW_LINKS).toString()).append("')");
                    if(!Files.isSymbolicLink(item) && shouldRecurse) {
                        addItemsToStatement(dirStmt, Files.newDirectoryStream(item), layers - 1);
                    }
                } else {

                }
            }
        } catch (Exception e) {
            throw new Error("Unreachable", e);
        }
    }

    /**
     * Changes the name of a file or directory.
     *
     * @param oldPath the path to the file or directory to be renamed
     * @param newPath the path to change the old path to
     * @return true if the path was valid and was successfully renamed.
     */
    @Override
    public boolean rename(Path oldPath, Path newPath) {
        throw new Error("Renaming not yet implemented.");
    }

    @Override
    public void remove(Path path) {
        throw new Error("Removing files not yet implemented.");
    }
    
    @Override
    public void rmrf(Path top) {
        throw new Error("rmrf-ing not yet implemented.");
    }

    /**
     * Clears any existing entries in the database.  Use with caution!
     */
    void clear() {
        try {
            // Takes advantage of SQLite's "truncate" optimization.
            Statement deleteStmt = conn.createStatement();
            deleteStmt.addBatch("DELETE FROM directories");
            deleteStmt.addBatch("DELETE FROM tags");
            deleteStmt.executeBatch();
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
