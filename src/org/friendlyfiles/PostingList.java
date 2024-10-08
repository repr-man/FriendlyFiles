package org.friendlyfiles;

import org.friendlyfiles.models.SortStep;
import org.friendlyfiles.ui.UIController;
import org.roaringbitmap.*;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

/**
 * A posting list is a data structure that is the inverse of an array.
 * <p>
 * An array maps integers to some type of content:
 * <pre>
 *              +-------+
 *         0 -> | "foo" |
 *              +-------+
 *         1 -> | "baz" |
 *              +-------+
 *         2 -> | "bar" |
 *              +-------+
 * </pre>
 * ... while a posting list maps content to sets of integers:
 * <pre>
 *         +-------+
 *         |       |    +---+---+---+
 *         | "foo" | -> | 4 | 1 | 7 |
 *         |       |    +---+---+---+
 *         +-------+
 *         |       |    +---+
 *         | "bar" | -> | 2 |
 *         |       |    +---+
 *         +-------+
 *         |       |    +---+---+---+---+
 *         | "baz" | -> | 3 | 1 | 9 | 4 |
 *         |       |    +---+---+---+---+
 *         +-------+
 * </pre>
 * <p>
 * We can use this to search a large number of strings very quickly.  We start by storing all the strings we want to
 * search (the haystack) in an array.  Then, we break all of them up into small chunks.  The chunks are used to index
 * the posting list.  (They are in the left column of the diagram above.)  They each get mapped to a set of integers
 * that are array indices of the haystack.  To find all the strings that contain a search query (the needle), we break
 * it up into chunks in the same way that we broke up the strings in the haystack.  Then, we find the intersection of
 * all the sets corresponding to the chunks of the needle.  The resulting set contains the indices in the haystack of
 * all the strings that have all of the chunks of the needle.  For example, if we tried to query the posting list in
 * the diagram above, and our query was broken up into the chunks "foo" and "baz", the result of the query would be
 * the set {1, 4}.
 *
 * <h4>Implementation Notes</h4>
 * Trigrams:
 * <p>
 * When we break up strings, we make chunks of three characters called trigrams.  (This is inspired by the Unix tool
 * "plocate".)  Strings are broken into every possible sequence of three adjacent characters.
 * <pre>
 *             "Hello"
 *             /  |  \
 *            /   |   \
 *        "Hel" "ell" "llo"
 * </pre>
 * This, however, allows for a massive number of trigrams.  It would not be practical (or even feasible on some
 * machines) to store this many trigrams as indices in the posting list.  To fix this problem, we do two things.
 * <p>
 * First, we map all the characters to a special encoding that reduces redundancy and only contains the most commonly
 * used characters in file paths.  For example, we map all characters outside the ASCII range to a single value, and
 * we map the corresponding upper- and lowercase letters to the same value.  This leaves us with 64 possible values
 * for each character in a trigram.
 * <p>
 * Second, we sort the values of the trigram.  This decreases the number of possible trigrams by an order of magnitude.
 * A reader might find this troubling.  Would not the queries "hello" and "ehllo" produce the same results?  To see
 * that they produce different results, let us break them into sorted trigrams.  "hello" has the trigrams {"ehl", "ell",
 * "llo"}, while "ehllo" has the trigrams {"ehl", "hll", "llo}.
 * <p>
 * To extend the example above, the full process of breaking a string into trigrams is shown below (the custom character
 * encoding is not shown to improve readability):
 * <pre>
 *             "Hello"
 *             /  |  \
 *            /   |   \
 *        "Hel" "ell" "llo"
 *          |     |     |
 *        "hel" "ell" "llo"
 *          |     |     |
 *        "ehl" "ell" "llo"
 * </pre>
 * Now that we have a manageable number of trigrams, we need to associate each one with a slot in the posting list.
 * While it would be possible to do this with a hash table, this approach would introduce a large performance penalty.
 * Thankfully, since we know the exact number of possible trigrams and the pattern of the numbers that make them up,
 * the trigrams, we can make a formula to directly compute a unique index for each trigram in the posting list (see
 * the methods `mapFirstTrigramChar` and `mapSecondTrigramChar`).
 * <pre></pre>
 * <p>
 * Sets:
 * <p>
 * To store the sets of integers, we could use a traditional hash set.  This, however, would have large time and space
 * costs.  It would also be difficult and expensive to perform the intersection operation on a large number of them.
 * Our solution is to use bit sets.  If the nth bit is set to true, it means that the string at the nth index in the
 * haystack contains the trigram corresponding to the set.  Bit sets are space-efficient, have a very cheap intersection
 * operation, and are highly compressible.  This makes them a perfect fit for our use case.
 * <p>
 * We use a library for our bit sets called RoaringBitmaps, worked on by the excellent Dr. Daniel Lemire.  We use this
 * library, as opposed to implementing our own, to ensure the correctness of the algorithms (especially the compression
 * algorithm), and to meet the time constraints of this project.
 * <pre></pre>
 * <p>
 * Serialization:
 * <p>
 * Posting lists can be written to a file to allow for querying a set of strings at a later time.  We do NOT do this
 * with Java's default serialization.  There are two reasons for this.
 * <p>
 * First, RoaringBitmaps use their own form of serialization.  To interact with them properly, we also need to do custom
 * serialization.
 * <p>
 * Second, and more importantly, we only want to serialize posting lists via memory mapped files.  Posting lists can
 * often be large (hundreds of megabytes).  Reading them with normal file io would be prohibitively expensive.  Memory
 * mapped files use operating system magic to avoid this overhead.
 */
public final class PostingList {
    private RoaringBitmap stage1Cache;
    private final String plFileLocation;
    private final List<RoaringBitmap> lists;
    private ArrayList<String> paths;
    private ArrayList<Long> sizes;
    private ArrayList<Long> timestamps;
    private long totalPathsSize = 0;
    private byte numHoles = 0;

    /**
     * Sets up an empty posting list at a given location.
     * @param fileLocation the location to store the database file
     */
    public PostingList(Path fileLocation) {
        this.plFileLocation = fileLocation.toString();
        ArrayList<RoaringBitmap> tmpLists = new ArrayList<>(45760);
        for (int i = 0; i < 45760; i++) {
            tmpLists.add(new RoaringBitmap());
        }
        lists = Collections.unmodifiableList(tmpLists);
        paths = new ArrayList<>();
        sizes = new ArrayList<>();
        timestamps = new ArrayList<>();
    }

    /**
     * Serializes the posting list to the list's file location.
     * @throws Exception if it cannot write the file
     */
    public void close() throws Exception {
        serializeTo(plFileLocation);
    }

    /**
     * Compresses and writes the posting list to a file.
     * @param filename the name of the file to write the posting list to
     * @throws IOException if there is an error writing to the file
     */
    public void serializeTo(String filename) throws IOException {
        long listsSerializedSize = lists.parallelStream().mapToLong(item -> {
            item.runOptimize();
            return item.serializedSizeInBytes();
        }).sum();
        try (RandomAccessFile file = new RandomAccessFile(filename, "rw")) {
            MappedByteBuffer mbb = file.getChannel().map(
                    FileChannel.MapMode.READ_WRITE,
                    0,
                    // listsSerializedSize: Size of all the serialized lists
                    // totalStringsSize: Number of bytes needed to represent all the strings
                    // strings.size() * 4: Integers representing the size of the strings
                    // sizes.size() * 8: Size of the array of longs representing file sizes
                    // timestamps.size() * 8: Size of the array of longs representing file dates
                    // 4: Integer representing the number of strings
                    // 1: Byte representing the number of holes
                    //
                    // 16: There is no good reason for this number to be here.
                    //     Due to the way memory mapping works, the OS doesn't always give us a file of the exact size
                    //     we are looking for.  Every time this function gets called, we could be given a file of a
                    //     slightly different size.  To prevent buffer overflows, we need to ask for a little more
                    //     memory than we actually need.  16 seems to be a good size that makes the function work
                    //     consistently.
                    listsSerializedSize + (totalPathsSize + paths.size() * 4L) + (sizes.size() * 8L) + (timestamps.size() * 8L) + 4 + 1 + 16
            );
            lists.forEach(item -> {
                item.serialize(mbb);
            });
            mbb.putInt(paths.size());
            mbb.put(numHoles);
            paths.forEach(item -> {
                mbb.putInt(item.getBytes().length);
                mbb.put(item.getBytes());
            });
            sizes.forEach(mbb::putLong);
            timestamps.forEach(mbb::putLong);
        }
    }

    /**
     * Creates a new posting list by reading it from a file.
     * @param filename the name of the file to read from
     * @return a new posting list
     * @throws IOException if the file can't be read
     */
    public static PostingList deserializeFrom(Path filename) throws IOException {
        PostingList pl = new PostingList(filename);
        try (RandomAccessFile file = new RandomAccessFile(filename.toString(), "r")) {
            MappedByteBuffer mbb = file.getChannel().map(
                    FileChannel.MapMode.READ_ONLY,
                    0,
                    file.length() - 12
            );
            for (int i = 0; i < 45760; i++) {
                RoaringBitmap item = pl.lists.get(i);
                item.deserialize(mbb);
                mbb.position(mbb.position() + item.serializedSizeInBytes());
            }
            int numStrings = mbb.getInt();
            pl.paths.ensureCapacity(numStrings);
            pl.sizes.ensureCapacity(numStrings);
            pl.timestamps.ensureCapacity(numStrings);
            pl.numHoles = mbb.get();
            for (int i = 0; i < numStrings; ++i) {
                int strSize = mbb.getInt();
                if (strSize > 0) {
                    pl.totalPathsSize += strSize;
                    byte[] bytes = new byte[strSize];
                    mbb.get(bytes, 0, strSize);
                    pl.paths.add(new String(bytes));
                } else {
                    pl.paths.add("");
                }
            }
            for (int i = 0; i < numStrings; ++i) {
                pl.sizes.add(mbb.getLong());
            }
            for (int i = 0; i < numStrings; ++i) {
                pl.timestamps.add(mbb.getLong());
            }
        }
        return pl;
    }

    /**
     * Reads necessary information from the filesystem into the backend in a background process
     * and swaps out the old data with the new data when it is done.
     */
    public void generateFromFilesystem(Switchboard switchboard) {
        Executors.newSingleThreadExecutor().submit(() -> {
            PostingList pl = new PostingList(Paths.get(plFileLocation));
            ParallelFileTreeVisitor walker = pl::add;
            walker.walk(Paths.get(System.getProperty("user.dir")).getRoot());
            
            System.err.println("Finished Walking");
            
            switchboard.swapInBackend(pl);
        });
    }

    /**
     * Registers a new file or directory at the given path.
     * @param path the path at which to add the new item
     * @param size the size of the item
     */
    public void add(String path, long size, long timestamp) {
        addString(path);
        sizes.add(size);
        timestamps.add(timestamp);
    }

    /**
     * Breaks a string into trigrams, adds the string to the list of potential strings, and adds the trigrams
     * to the posting list.
     * @param str the string to add to the posting list
     */
    private void addString(String str) {
        if (str.isEmpty()) return;

        int index = paths.size();
        paths.add(str);
        totalPathsSize += str.getBytes().length;

        // If str.length() < 3, we are not able to search for them with trigrams, so we don't add them to the
        // posting list.
        if (str.length() >= 3) {
            int a = 60, b = mapChar(str.charAt(0)), c = mapChar(str.charAt(1));
            lists.get(mapTrigramToIndex(a, b, c)).add(index);
            for (int i = 2; i < str.length(); ++i) {
                a = b;
                b = c;
                c = mapChar(str.charAt(i));
                lists.get(mapTrigramToIndex(a, b, c)).add(index);
            }
        }
    }

    /**
     * Deletes a file or directory at the given path.
     * <p>
     * This method assumes that the files exists.  These assumptions should be checked in
     * the ui or controller code so that they can display an error message to the user.
     * @param path the path of the file to remove
     * @return true if str is not in the list; false if str was in the list and was removed
     */
    public boolean remove(String path) {
        return removeItem(path) == null;
    }

    /**
     * Ditto.
     * This function is used internally in both `remove` and `rename`.
     * @param path the path of the file to remove
     * @return -1 if str is not in the list; otherwise, the size of the removed item
     */
    private FileModel removeItem(String path) {
        int idx = removeString(path);
        if (idx < 0) return null;
        long sizeResult = sizes.get(idx);
        sizes.set(idx, Long.MIN_VALUE);
        long timestampResult = sizes.get(idx);
        timestamps.set(idx, Long.MIN_VALUE);
        // Compact the haystack.
        if (numHoles > 127) {
            paths = (ArrayList<String>) paths.parallelStream()
                                                .filter(String::isEmpty)
                                                .collect(Collectors.toList());
            sizes = (ArrayList<Long>) sizes.parallelStream()
                                              .filter(size -> size > Long.MIN_VALUE)
                                              .collect(Collectors.toList());
            timestamps = (ArrayList<Long>) timestamps.parallelStream()
                                                   .filter(time -> time > Long.MIN_VALUE)
                                                   .collect(Collectors.toList());
            lists.parallelStream().forEach(RoaringBitmap::clear);
            IntStream.range(0, paths.size()).parallel().forEach(i -> {
                if (paths.get(i).length() >= 3) {
                    int a, b = mapChar(paths.get(i).charAt(0)), c = mapChar(paths.get(i).charAt(1));
                    for (int j = 2; j < paths.get(i).length(); ++j) {
                        a = b;
                        b = c;
                        c = mapChar(paths.get(i).charAt(i));
                        lists.get(mapTrigramToIndex(a, b, c)).add(i);
                    }
                }
            });
            numHoles = 0;
        }

        return new FileModel("", sizeResult, timestampResult);
    }

    /**
     * Removes a string from the posting list and haystack if they contain the string.
     * @param str the string to remove
     * @return -1 if str is not in the list; otherwise, the index of the removed item
     */
    private int removeString(String str) {
        if (str.isEmpty()) return -1;

        int index = paths.indexOf(str);
        if (index == -1) return index;
        paths.set(index, "");
        totalPathsSize -= str.getBytes().length;
        ++numHoles;

        // If str.length() < 3, it is not in the posting list.
        if (str.length() >= 3) {
            int a, b = mapChar(str.charAt(0)), c = mapChar(str.charAt(1));
            for (int i = 2; i < str.length(); ++i) {
                a = b;
                b = c;
                c = mapChar(str.charAt(i));
                lists.get(mapTrigramToIndex(a, b, c)).remove(index);
            }
        }

        return index;
    }

    /**
     * Moves a file to a different directory.
     * @param source the path of the file to move
     * @param destination the path of the directory to move `source` to
     */
    public void moveFile(String source, String destination) {
        getStrings(source).stream().parallel().filter(i -> paths.get(i).equals(source)).findAny().ifPresent(srcIdx -> {
            if (srcIdx >= 0) {
                String destPath = destination + source.substring(source.lastIndexOf(File.separatorChar));
                FileModel fileInfo = removeItem(source);
                assert fileInfo != null;
                add(destPath, fileInfo.size, fileInfo.timestamp);
            }
        });
    }

    /**
     * Queries the backend for files.
     * @param filter filters the query results
     * @return a stream of file paths corresponding to the results of the query
     */
    public Stream<String> get(QueryFilter filter) {
        // Start searching numeric arrays.
        ForkJoinTask<RoaringBitmap> fileSizeQueryTask = ForkJoinPool.commonPool().submit(() -> IntStream.range(0, sizes.size())
            .filter(i -> filter.isInFileSizeRange(sizes.get(i)))
                .collect(RoaringBitmap::new, RoaringBitmap::add, ParallelAggregation::or));
        ForkJoinTask<RoaringBitmap> dateQueryTask = ForkJoinPool.commonPool().submit(() -> IntStream.range(0, timestamps.size())
            .filter(i -> filter.isInFileDateRange(timestamps.get(i)))
                .collect(RoaringBitmap::new, RoaringBitmap::add, ParallelAggregation::or));

        // Search text-related things.
        String[] splitQuery = filter.getQuery().split("\\s");
        ForkJoinTask<RoaringBitmap> searchQueryTask = ForkJoinPool.commonPool().submit(() ->
            Arrays.stream(splitQuery).parallel()
                    .map(this::getStrings)
                    .reduce(RoaringBitmap.bitmapOfRange(0, paths.size()), (acc, item) -> RoaringBitmap.and(acc, item)));
        ForkJoinTask<RoaringBitmap> rootsQueryTask = ForkJoinPool.commonPool().submit(() ->
             filter.getRoots().parallelStream()
                     .map(this::getStrings)
                     .reduce(new RoaringBitmap(), ParallelAggregation::or));
        ForkJoinTask<RoaringBitmap> additiveSearchQueryTask = ForkJoinPool.commonPool().submit(() -> {
            if (filter.getTextSearchTerms().isEmpty()) return RoaringBitmap.bitmapOfRange(0, 0x100000000L);
            return filter.getTextSearchTerms().parallelStream()
                           .map(this::getStrings)
                           .reduce(RoaringBitmap.bitmapOfRange(0, paths.size()), ParallelAggregation::or);
        });
        ForkJoinTask<RoaringBitmap> extensionQueryTask = ForkJoinPool.commonPool().submit(() -> {
            if (filter.getExtSearchTerms().isEmpty()) return RoaringBitmap.bitmapOfRange(0, 0x100000000L);
            return filter.getTextSearchTerms().parallelStream()
                           .map(this::getStrings)
                           .reduce(RoaringBitmap.bitmapOfRange(0, paths.size()), (acc, item) -> RoaringBitmap.and(acc, item));
        });

        // Combine all the bitmaps.
        stage1Cache = new RoaringBitmap(filter.getVisibleItems().toMutableRoaringBitmap());
        stage1Cache.and(searchQueryTask.join());
        stage1Cache.and(rootsQueryTask.join());
        stage1Cache.and(additiveSearchQueryTask.join());
        stage1Cache.and(extensionQueryTask.join());
        stage1Cache.and(fileSizeQueryTask.join());
        stage1Cache.and(dateQueryTask.join());

        return getPostprocessed(filter, splitQuery);
    }

    /**
     * Postprocesses and potentially sorts the paths associated to a bit set.
     * @param filter the parameters for filtering and sorting
     * @param splitQuery the query segments to ensure are included
     * @return a stream of file paths ready to be given to the UI
     */
    private Stream<String> getPostprocessed(QueryFilter filter, String[] splitQuery) {
        IntStream outStream = stage1Cache.stream().parallel()
                                      .filter(i -> {
                                          if (filter.getExtSearchTerms().isEmpty()) return true;
                                          for (String extension : filter.getExtSearchTerms()) {
                                              if (paths.get(i).endsWith(extension)) return true;
                                          }
                                          return false;
                                      })
                                      .filter(i -> filter.getRoots().parallelStream().anyMatch(paths.get(i)::startsWith))
                                      .filter(i -> Arrays.stream(splitQuery).parallel().allMatch(paths.get(i)::contains))
                                      .filter(i -> {
                                          if (filter.getTextSearchTerms().isEmpty()) return true;
                                          return filter.getTextSearchTerms().parallelStream().anyMatch(paths.get(i)::contains);
                                      });

        if (filter.getSortSteps().isEmpty()) {
            return outStream.mapToObj(paths::get);
        }
        // Sorting
        Comparator<Integer> comparator = filter.getSortSteps().stream()
                                                 .map(this::getComparatorForSortStep)
                                                 .reduce(Comparator::thenComparing).get();

        return outStream.boxed().sorted(comparator).map(paths::get);
    }

    private final Comparator<Integer> nameComparator = (path1, path2) -> getFileName(paths.get(path1)).compareTo(getFileName(paths.get(path2)));
    private final Comparator<Integer> extensionComparator = (path1, path2) -> getFileExtension(paths.get(path1)).compareTo(getFileExtension(paths.get(path2)));
    private final Comparator<Integer> sizeComparator = (size1, size2) -> Long.compare(sizes.get(size1), sizes.get(size2));
    private final Comparator<Integer> timestampComparator = (time1, time2) -> Long.compare(timestamps.get(time1), timestamps.get(time2));

    /**
     * Maps a SortStep into a Comparator.  This has to be done in the PostingList class because the comparators rely on
     * private fields of the posting list.
     * @param step the step to map
     * @return the associated comparator
     */
    private Comparator<Integer> getComparatorForSortStep(SortStep step) {
        switch (step.getType()) {
            case NAME:
                return step.getOrder() == SortStep.OrderType.DESCENDING ? nameComparator.reversed() : nameComparator;
            case EXTENSION:
                return step.getOrder() == SortStep.OrderType.DESCENDING ? extensionComparator.reversed() : extensionComparator;
            case DATE_EDITED:
                return step.getOrder() == SortStep.OrderType.DESCENDING ? timestampComparator.reversed() : timestampComparator;
            case FILESIZE:
                return step.getOrder() == SortStep.OrderType.DESCENDING ? sizeComparator.reversed() : sizeComparator;
        }
        throw new Error("Unreachable");
    }

    /**
     * Gets the file name (the portion between the last separator and the last dot).
     * @param path the file to retrive the name from
     * @return the name
     */
    private static String getFileName(String path) {
        int lastDotIdx = path.lastIndexOf('.');
        int lastSeparatorIdx = path.lastIndexOf(File.separatorChar);
        if (lastDotIdx > lastSeparatorIdx) {
            return path.substring(lastSeparatorIdx + 1, lastDotIdx);
        } else {
            return path.substring(lastSeparatorIdx);
        }
    }

    /**
     * @param path the file to retrive the extension from
     * @return the extension, or "" if there is none
     */
    private static String getFileExtension(String path) {
        int lastDotIdx = path.lastIndexOf('.');
        int lastSeparatorIdx = path.lastIndexOf(File.separatorChar);
        if (lastDotIdx > lastSeparatorIdx) {
            return path.substring(lastDotIdx);
        } else {
            return "";
        }
    }

    /**
     * Retrieves all the strings corresponding to a query string.
     * @param query the string to search for
     * @return a bitset of indexes of strings containing the result of the query
     */
    private RoaringBitmap getStrings(String query) {
        if (query.length() < 3) {
            return IntStream.range(0, paths.size())
                           .filter(i -> paths.get(i).contains(query))
                           .collect(RoaringBitmap::new, RoaringBitmap::add, ParallelAggregation::or);
        } else {
            int a = mapChar(query.charAt(0)), b = mapChar(query.charAt(1)), c = mapChar(query.charAt(2));
            RoaringBitmap bitset = lists.get(mapTrigramToIndex(a, b, c));
            for (int i = 3; i < query.length(); ++i) {
                a = b;
                b = c;
                c = mapChar(query.charAt(i));
                bitset = RoaringBitmap.and(bitset, lists.get(mapTrigramToIndex(a, b, c)));
            }
            return bitset;
        }
    }

    /**
     * Changes the name of a file.
     * @param oldPath the path to the file to be renamed
     * @param newName the name to change the old name to
     */
    public void renameFile(String oldPath, String newName) {
        FileModel fileInfo = removeItem(oldPath);
        if (fileInfo != null) {
            add(Paths.get(oldPath).resolveSibling(newName).toString(), fileInfo.size, fileInfo.timestamp);
        }
    }

    /**
     * Gets a list of all the directories beneath all the roots specified in the filter.
     * @param filter the filter containing root directories
     * @return the stream of directories
     */
    public Stream<String> getDirectories(QueryFilter filter) {
        RoaringBitmap dirs = new RoaringBitmap();
        for (int i = 0; i < sizes.size(); ++i){
            if (sizes.get(i) == -1) dirs.add(i);
        }
        RoaringBitmap rootPaths = filter.getRoots().parallelStream()
                                          .map(this::getStrings)
                                          .reduce(RoaringBitmap.bitmapOf(), ParallelAggregation::or);
        rootPaths.and(dirs);
        return rootPaths.stream().parallel()
                       .mapToObj(paths::get)
                       .filter(s -> filter.getRoots().stream().anyMatch(s::startsWith));
    }

    /**
     * Gets the paths associated with the query, except for the ones starting with `dirPath`.
     * @param filter the filter with the visible item bit set
     * @param dirPath the path to disallow
     * @return a stream of file names corresponding to the results of the operation
     */
    public Stream<String> disallowFilesInDirectory(QueryFilter filter, String dirPath) {
        RoaringBitmap toggleBitset = getStrings(dirPath + UIController.fileSeparator).stream()
                                             .filter(i -> paths.get(i).startsWith(dirPath + UIController.fileSeparator))
                                             .collect(RoaringBitmap::new, RoaringBitmap::add, ParallelAggregation::or);
        toggleBitset.flip(0, 0x100000000L);
        filter.getVisibleItems().and(toggleBitset);
        return get(filter);
    }

    /**
     * Gets the paths associated with the query, toggling the visibility of the ones starting with `dirPath`.
     * @param filter the filter with the visible item bit set
     * @param dirPath the path to toggle
     * @return a stream of file names corresponding to the results of the operation
     */
    public Stream<String> toggleVisibleFiles(QueryFilter filter, String dirPath) {
        RoaringBitmap toggleBitset = getStrings(dirPath + UIController.fileSeparator).stream()
                                             .filter(i -> paths.get(i).startsWith(dirPath + UIController.fileSeparator))
                                             .collect(RoaringBitmap::new, RoaringBitmap::add, ParallelAggregation::or);
        filter.getVisibleItems().xor(toggleBitset);
        return get(filter);
    }

    /**
     * Finds all the files associated with a root and adds them to the filter's visible items.
     * @param topDirectory the root to add
     * @param filter the filter to ad the root to
     */
    public void addRootToFilter(String topDirectory, QueryFilter filter) {
        // It may seem like this is redundant because of the postprocessing step, but this is necessary for
        // having multiple roots in the directory tree.
        RoaringBitmap newFiles = getStrings(topDirectory + UIController.fileSeparator).stream().parallel()
                                         .filter(i -> paths.get(i).startsWith(topDirectory + UIController.fileSeparator))
                                         .collect(RoaringBitmap::new, RoaringBitmap::add, ParallelAggregation::or);
        filter.getVisibleItems().or(newFiles);
    }

    /**
     * Maps the three mapped characters of a trigram to a posting list index.
     * @param a the first mapped character to map
     * @param b the second mapped character to map
     * @param c the third mapped character to map
     * @return the index of the trigram in the posting list
     */
    private static int mapTrigramToIndex(int a, int b, int c) {
        // Sort the characters of the trigram.
        int a1 = a, b1 = b, c1 = c;
        if (b1 < a1) {
            int tmp = a1;
            a1 = b1;
            b1 = tmp;
        }
        if (c1 < b1) {
            int tmp = b1;
            b1 = c1;
            c1 = tmp;
            if (b1 < a1) {
                tmp = a1;
                a1 = b1;
                b1 = tmp;
            }
        }
        return mapFirstTrigramChar(a1) + mapSecondTrigramChar(b1) + c1;
    }

    /**
     * A lookup table of values needed for mapping the first character of a trigram
     * to a posting list index.
     * It contains the results of the following formula for x in [0, 64):
     * <p>
     * f(x) = (\sum_{n = 65 - x}^{64} \frac{n(n-1)}{2}) - (\sum_{n = 65 - x}^{64} n)
     * @param n an integer in the range [0, 64)
     * @return the result of the function above
     */
    private static int mapFirstTrigramChar(int n) {
        switch (n) {
            case 0: return  0;
            case 1: return  2016;
            case 2: return  3969;
            case 3: return  5860;
            case 4: return  7690;
            case 5: return  9460;
            case 6: return  11171;
            case 7: return  12824;
            case 8: return  14420;
            case 9: return  15960;
            case 10: return 17445;
            case 11: return 18876;
            case 12: return 20254;
            case 13: return 21580;
            case 14: return 22855;
            case 15: return 24080;
            case 16: return 25256;
            case 17: return 26384;
            case 18: return 27465;
            case 19: return 28500;
            case 20: return 29490;
            case 21: return 30436;
            case 22: return 31339;
            case 23: return 32200;
            case 24: return 33020;
            case 25: return 33800;
            case 26: return 34541;
            case 27: return 35244;
            case 28: return 35910;
            case 29: return 36540;
            case 30: return 37135;
            case 31: return 37696;
            case 32: return 38224;
            case 33: return 38720;
            case 34: return 39185;
            case 35: return 39620;
            case 36: return 40026;
            case 37: return 40404;
            case 38: return 40755;
            case 39: return 41080;
            case 40: return 41380;
            case 41: return 41656;
            case 42: return 41909;
            case 43: return 42140;
            case 44: return 42350;
            case 45: return 42540;
            case 46: return 42711;
            case 47: return 42864;
            case 48: return 43000;
            case 49: return 43120;
            case 50: return 43225;
            case 51: return 43316;
            case 52: return 43394;
            case 53: return 43460;
            case 54: return 43515;
            case 55: return 43560;
            case 56: return 43596;
            case 57: return 43624;
            case 58: return 43645;
            case 59: return 43660;
            case 60: return 43670;
            case 61: return 43676;
            case 62: return 43679;
            case 63: return 43680;
            default: throw new Error("Invalid mapped character value " + n + " when looking up first trigram index value");
        }
    }

    /**
     * A lookup table of values needed for mapping the second character of a trigram
     * to a posting list index.
     * It contains the results of the following formula for x in [0, 64):
     * <p>
     * f(x) = (\sum_{n = 65 - x}^{64} n) - x
     * @param n an integer in the range [0, 64)
     * @return the result of the function above
     */
    private static int mapSecondTrigramChar(int n) {
        switch (n) {
            case 0:  return 0;
            case 1:  return 63;
            case 2:  return 125;
            case 3:  return 186;
            case 4:  return 246;
            case 5:  return 305;
            case 6:  return 363;
            case 7:  return 420;
            case 8:  return 476;
            case 9:  return 531;
            case 10: return 585;
            case 11: return 638;
            case 12: return 690;
            case 13: return 741;
            case 14: return 791;
            case 15: return 840;
            case 16: return 888;
            case 17: return 935;
            case 18: return 981;
            case 19: return 1026;
            case 20: return 1070;
            case 21: return 1113;
            case 22: return 1155;
            case 23: return 1196;
            case 24: return 1236;
            case 25: return 1275;
            case 26: return 1313;
            case 27: return 1350;
            case 28: return 1386;
            case 29: return 1421;
            case 30: return 1455;
            case 31: return 1488;
            case 32: return 1520;
            case 33: return 1551;
            case 34: return 1581;
            case 35: return 1610;
            case 36: return 1638;
            case 37: return 1665;
            case 38: return 1691;
            case 39: return 1716;
            case 40: return 1740;
            case 41: return 1763;
            case 42: return 1785;
            case 43: return 1806;
            case 44: return 1826;
            case 45: return 1845;
            case 46: return 1863;
            case 47: return 1880;
            case 48: return 1896;
            case 49: return 1911;
            case 50: return 1925;
            case 51: return 1938;
            case 52: return 1950;
            case 53: return 1961;
            case 54: return 1971;
            case 55: return 1980;
            case 56: return 1988;
            case 57: return 1995;
            case 58: return 2001;
            case 59: return 2006;
            case 60: return 2010;
            case 61: return 2013;
            case 62: return 2015;
            case 63: return 2016;
            default: throw new Error("Invalid mapped character value " + n + " when looking up second trigram index value");
        }
    }

    /**
     * Maps Unicode characters into the range [0, 64) to reduce the number of possible trigrams.
     * Since most Unicode characters are never used in file names, it doesn't pay to make
     * trigrams for them and increase the size of the posting list.
     * <p>
     * The mappings are as follows:
     * <ul>
     * <li>ASCII control characters [0, 32) -> 61</li>
     * <li>Punctuation characters and numbers [32, 38], [40, 64] -> Subtract 32</li>
     * <li>', ` -> " -> 2</li>
     * <li>Uppercase and lowercase letters -> [32, 58], respectively</li>
     * <li>| -> 7</li>
     * <li>[, { -> ( -> 8</li>
     * <li>], } -> ) -> 9</li>
     * <li>\ -> 15</li>
     * <li>~ -> 59</li>
     * <li>^ -> 62</li>
     * <li>_ -> 63</li>
     * <li>All other characters -> 61</li>
     * </ul>
     * @param c the character to map
     * @return the mapped character
     */
    private static byte mapChar(char c) {
        switch (c) {
            case 32:  return 0;
            case 33:  return 1;
            case 34:  return 2;
            case 35:  return 3;
            case 36:  return 4;
            case 37:  return 5;
            case 38:  return 6;
            case 39:  return 2;
            case 40:  return 8;
            case 41:  return 9;
            case 42:  return 10;
            case 43:  return 11;
            case 44:  return 12;
            case 45:  return 13;
            case 46:  return 14;
            case 47:  return 15;
            case 48:  return 16;
            case 49:  return 17;
            case 50:  return 18;
            case 51:  return 19;
            case 52:  return 20;
            case 53:  return 21;
            case 54:  return 22;
            case 55:  return 23;
            case 56:  return 24;
            case 57:  return 25;
            case 58:  return 26;
            case 59:  return 27;
            case 60:  return 28;
            case 61:  return 29;
            case 62:  return 30;
            case 63:  return 31;
            case 64:  return 32;
            case 65:  return 33;
            case 66:  return 34;
            case 67:  return 35;
            case 68:  return 36;
            case 69:  return 37;
            case 70:  return 38;
            case 71:  return 39;
            case 72:  return 40;
            case 73:  return 41;
            case 74:  return 42;
            case 75:  return 43;
            case 76:  return 44;
            case 77:  return 45;
            case 78:  return 46;
            case 79:  return 47;
            case 80:  return 48;
            case 81:  return 49;
            case 82:  return 50;
            case 83:  return 51;
            case 84:  return 52;
            case 85:  return 53;
            case 86:  return 54;
            case 87:  return 55;
            case 88:  return 56;
            case 89:  return 57;
            case 90:  return 58;
            case 91:  return 8;
            case 92:  return 15;
            case 93:  return 9;
            case 94:  return 62;
            case 95:  return 63;
            case 96:  return 2;
            case 97:  return 33;
            case 98:  return 34;
            case 99:  return 35;
            case 100: return 36;
            case 101: return 37;
            case 102: return 38;
            case 103: return 39;
            case 104: return 40;
            case 105: return 41;
            case 106: return 42;
            case 107: return 43;
            case 108: return 44;
            case 109: return 45;
            case 110: return 46;
            case 111: return 47;
            case 112: return 48;
            case 113: return 49;
            case 114: return 50;
            case 115: return 51;
            case 116: return 52;
            case 117: return 53;
            case 118: return 54;
            case 119: return 55;
            case 120: return 56;
            case 121: return 57;
            case 122: return 58;
            case 123: return 8;
            case 124: return 7;
            case 125: return 9;
            case 126: return 59;
            case 127: return 61;
            default:  return 61;
        }
    }

    /**
     * Turns a mapped character into a normal character.
     * @param b an integer in the range [0, 64) to decode
     * @return the unmapped character
     */
    private static char unmapChar(int b) {
        switch (b) {
            case 0: return  ' ';
            case 1: return  '!';
            case 2: return  '"';
            case 3: return  '#';
            case 4: return  '$';
            case 5: return  '%';
            case 6: return  '&';
            case 7: return  '|';
            case 8: return  '(';
            case 9: return  ')';
            case 10: return '*';
            case 11: return '+';
            case 12: return ',';
            case 13: return '-';
            case 14: return '.';
            case 15: return '/';
            case 16: return '0';
            case 17: return '1';
            case 18: return '2';
            case 19: return '3';
            case 20: return '4';
            case 21: return '5';
            case 22: return '6';
            case 23: return '7';
            case 24: return '8';
            case 25: return '9';
            case 26: return ':';
            case 27: return ';';
            case 28: return '<';
            case 29: return '=';
            case 30: return '>';
            case 31: return '?';
            case 32: return '@';
            case 33: return 'a';
            case 34: return 'b';
            case 35: return 'c';
            case 36: return 'd';
            case 37: return 'e';
            case 38: return 'f';
            case 39: return 'g';
            case 40: return 'h';
            case 41: return 'i';
            case 42: return 'j';
            case 43: return 'k';
            case 44: return 'l';
            case 45: return 'm';
            case 46: return 'n';
            case 47: return 'o';
            case 48: return 'p';
            case 49: return 'q';
            case 50: return 'r';
            case 51: return 's';
            case 52: return 't';
            case 53: return 'u';
            case 54: return 'v';
            case 55: return 'w';
            case 56: return 'x';
            case 57: return 'y';
            case 58: return 'z';
            case 59: return '~';
            case 61: return '{';   // Placeholder character
            case 62: return '^';
            case 63: return '_';
            default: throw new Error("Unrecognized encoded character!");
        }
    }

}




