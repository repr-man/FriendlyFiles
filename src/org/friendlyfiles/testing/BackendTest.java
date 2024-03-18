package org.friendlyfiles.testing;

import java.io.*;
import java.sql.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.friendlyfiles.FileTrie;
import org.friendlyfiles.utils.RealPath;

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
}
