package org.friendlyfiles;

import java.nio.file.*;
import java.sql.*;
import java.util.*;

import java.io.IOException;

// TODO:
// Figure out how we want to store metadata about the files.
// We might want to have an interface to specify this to make
// it easier to test things.

/**
 * The functions we need to interact with one of our backends.
 */
public interface Backend {
    
    /**
     * Traverses all the directories in the file system beneath `top`
     * and adds them the the storage/indexing system of the backend.
     *
     * This method should be called on the first run of the program
     * before any of the file system has been indexed, or from a user-
     * triggered regeneration in the ui.
     *
     * This method assumes that it receives a valid path to a directory.
     * The ui should be able to prevent the user from selecting a
     * directory that doesn't exist or from selecting a file instead of
     * a directory.  Hence, this method terminates the program if 
     * either of these assumptions are untrue.
     *
     * @param top the top-level directory to scan
     * @throws Error
     */
    public void generateAtDir(Path top);
}

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
            createStmt.addBatch("CREATE TABLE directories(path STRING);");
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

    /**
     * Adds a large number of files and directories from the filesystem into the database.
     *
     * This only gets the contents from three layers of the file tree.  I initially tried
     * getting all the files beneath `top`.  However, experiments on my desktop directory
     * showed that it took about a minute to fully index all the files.  Given that this
     * is only a fraction of the files on my machine, we cannot use this strategy.
     * Instead, we will only index the files that the user is most likely to look at.
     * The database will grow over time as they explore more of the fliesystem.
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
            addDirectoriesToStatement(addDirStmt, Files.newDirectoryStream(topPath));
            addDirStmt.append(';');
            stmt.execute(addDirStmt.toString());
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    /**
     * Calls `addDirectoriesToStatement` with the default number of layers.
     */
    private static void addDirectoriesToStatement(StringBuilder stmt, DirectoryStream<Path> iter) {
        addDirectoriesToStatement(stmt, iter, 2);
    }
    
    /**
     * Recursively walks all the directories provided by `iter`, with a depth limit of `layers`, and
     * adds each path to the SQL statement given by `stmt`.
     *
     * @param stmt the statement to insert file paths into
     * @param iter the directories to walk
     * @param layers the maximum traversal depth
     */
    private static void addDirectoriesToStatement(StringBuilder stmt, DirectoryStream<Path> iter, int layers) {
        try {
            boolean shouldRecurse = layers > 0;
            for (Path item : iter) {
                if (Files.isDirectory(item)) {
                    stmt.append(",('").append(item.toRealPath(LinkOption.NOFOLLOW_LINKS).toString()).append("')");
                    if(!Files.isSymbolicLink(item) && shouldRecurse) {
                        addDirectoriesToStatement(stmt, Files.newDirectoryStream(item), layers - 1);
                    }
                } else {

                }
            }
        } catch (Exception e) {
            throw new Error("Unreachable", e);
        }
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
