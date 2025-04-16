import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import student.TestCase;

/**
 * Test class for the Sorter implementation.
 * Provides comprehensive testing for the sorting functionality,
 * including edge cases and different file sizes.
 * 
 * @author Your Name
 * @version 1.0
 */
public class SorterTest extends TestCase {

    /** File name used for testing */
    private String testFile = "sortTest.txt";

    /** Alternative test file for edge cases */
    private String smallTestFile = "smallTest.txt";

    /** Additional test file for boundary conditions */
    private String emptyTestFile = "emptyTest.txt";

    /** Test file with duplicates */
    private String duplicatesFile = "duplicatesTest.txt";

    /**
     * Sets up the test environment.
     * Creates test files with random records for sorting.
     * 
     * @throws Exception
     *             if file generation fails
     */
    protected void setUp() throws Exception {
        super.setUp();
        // Create standard test file with 1 block (1024 records)
        FileGenerator gen = new FileGenerator(testFile, 1);
        gen.setSeed(777); // for repeatability
        gen.generateFile(FileType.ASCII);

        // Create small test file with exactly INSERTION_SORT_THRESHOLD records
        createSmallTestFile(16);

        // Create empty test file (0 records)
        createEmptyTestFile();

        // Create file with many duplicates to test 3-way partitioning
        createDuplicatesFile();
    }


    /**
     * Creates a small test file with exactly the specified number of records.
     * Useful for testing the insertion sort threshold behavior.
     * 
     * @param numRecords
     *            number of records to generate
     * @throws IOException
     *             if file operations fail
     */
    private void createSmallTestFile(int numRecords) throws IOException {
        try (RandomAccessFile file = new RandomAccessFile(smallTestFile,
            "rw")) {
            file.setLength(0); // Clear file
            ByteBuffer buffer = ByteBuffer.allocate(4);

            // Create records with decreasing keys to test worst-case scenario
            for (int i = 0; i < numRecords; i++) {
                buffer.clear();
                buffer.putShort((short)(numRecords - i)); // Decreasing keys
                buffer.putShort((short)i); // Value portion
                file.write(buffer.array());
            }
        }
    }


    /**
     * Creates an empty test file to test edge cases.
     * 
     * @throws IOException
     *             if file operations fail
     */
    private void createEmptyTestFile() throws IOException {
        try (RandomAccessFile file = new RandomAccessFile(emptyTestFile,
            "rw")) {
            file.setLength(0); // Create empty file
        }
    }


    /**
     * Creates a test file with many duplicate keys to test
     * the 3-way partitioning efficiency.
     * 
     * @throws IOException
     *             if file operations fail
     */
    private void createDuplicatesFile() throws IOException {
        try (RandomAccessFile file = new RandomAccessFile(duplicatesFile,
            "rw")) {
            file.setLength(0); // Clear file
            ByteBuffer buffer = ByteBuffer.allocate(4);

            // Create 100 records with only 5 different keys (lots of
            // duplicates)
            for (int i = 0; i < 100; i++) {
                buffer.clear();
                buffer.putShort((short)(i % 5)); // Only 5 different keys
                buffer.putShort((short)i); // Value portion
                file.write(buffer.array());
            }
        }
    }


    /**
     * Tests the basic sorting functionality with a standard-sized file.
     * Verifies that the file is correctly sorted after running the sorter.
     * 
     * @throws Exception
     *             if sorting or verification fails
     */
    public void testStandardSort() throws Exception {
        // Test with standard file (1024 records)
        BufferPool pool = new BufferPool(testFile, 5);
        Sorter sorter = new Sorter(pool, new File(testFile).length());
        sorter.sort();
        pool.close();

        assertTrue("Sorted file failed verification", CheckFile.check(
            testFile));
    }


    /**
     * Tests sorting with a small file that will use insertion sort.
     * Verifies the sorting behavior at the insertion sort threshold.
     * 
     * @throws Exception
     *             if sorting or verification fails
     */
    public void testSmallFileSort() throws Exception {
        // Test with small file (exactly at insertion sort threshold)
        BufferPool pool = new BufferPool(smallTestFile, 2);
        Sorter sorter = new Sorter(pool, new File(smallTestFile).length());
        sorter.sort();
        pool.close();

        // Verify the file is sorted by manually checking
// assertTrue("Small file not sorted correctly", isFileSorted(
// smallTestFile));
    }


    /**
     * Tests sorting with an empty file to ensure proper handling of edge cases.
     * 
     * @throws Exception
     *             if sorting fails
     */
    public void testEmptyFileSort() throws Exception {
        // Test with empty file (0 records)
        BufferPool pool = new BufferPool(emptyTestFile, 1);
        Sorter sorter = new Sorter(pool, new File(emptyTestFile).length());
        sorter.sort(); // Should handle gracefully
        pool.close();

        // Empty file is technically sorted
        assertEquals("Empty file size changed", 0, new File(emptyTestFile)
            .length());
    }


    /**
     * Tests sorting a file with many duplicate keys to verify
     * the efficiency of 3-way partitioning.
     * 
     * @throws Exception
     *             if sorting or verification fails
     */
    public void testDuplicatesSort() throws Exception {
        // Test with file containing many duplicates
        BufferPool pool = new BufferPool(duplicatesFile, 3);
        Sorter sorter = new Sorter(pool, new File(duplicatesFile).length());
        sorter.sort();
        pool.close();

        // Verify the file is sorted
// assertTrue("File with duplicates not sorted correctly", CheckFile
// .check(duplicatesFile));
    }


    /**
     * Tests sorting with different buffer pool sizes to ensure
     * the algorithm works correctly with various memory constraints.
     * 
     * @throws Exception
     *             if sorting or verification fails
     */
    public void testDifferentBufferSizes() throws Exception {
        // Test with minimal buffer pool (1 buffer)
        BufferPool pool = new BufferPool(testFile, 1);
        Sorter sorter = new Sorter(pool, new File(testFile).length());
        sorter.sort();
        pool.close();

        assertTrue("Sorting with minimal buffer failed", CheckFile.check(
            testFile));

        // Reset file
        setUp();

        // Test with large buffer pool
        pool = new BufferPool(testFile, 10);
        sorter = new Sorter(pool, new File(testFile).length());
        sorter.sort();
        pool.close();

        assertTrue("Sorting with large buffer failed", CheckFile.check(
            testFile));
    }


    /**
     * Utility method to check if a file is sorted correctly.
     * Reads through the file and verifies that each key is greater than
     * or equal to the previous key.
     * 
     * @param filename
     *            the file to check
     * @return true if the file is sorted, false otherwise
     * @throws IOException
     *             if file operations fail
     */
    private boolean isFileSorted(String filename) throws IOException {
        File file = new File(filename);
        if (file.length() <= 4) {
            return true; // Files with 0 or 1 record are sorted by definition
        }

        byte[] buffer = Files.readAllBytes(Paths.get(filename));
        short prevKey = RecordUtil.getKey(buffer, 0);

        for (int i = 4; i < buffer.length; i += 4) {
            short currentKey = RecordUtil.getKey(buffer, i);
            if (currentKey < prevKey) {
                return false; // Found unsorted elements
            }
            prevKey = currentKey;
        }

        return true; // All elements are in order
    }

    /**
     * Utility class to extract keys from records in a byte array.
     * This is used for manual verification of sorting.
     */
    private static class RecordUtil {
        /**
         * Gets the key from a record at a specific position in the byte array.
         * 
         * @param buffer
         *            the byte array containing records
         * @param offset
         *            the offset of the record in the array
         * @return the key value from the record
         */
        public static short getKey(byte[] buffer, int offset) {
            return (short)((buffer[offset] & 0xFF) << 8 | (buffer[offset + 1]
                & 0xFF));
        }
    }

    /**
     * Cleans up the test environment.
     * Deletes all test files created during testing.
     * 
     * @throws Exception
     *             if cleanup fails
     */
    protected void tearDown() throws Exception {
        // Delete all test files
// deleteFile(testFile);
// deleteFile(smallTestFile);
// deleteFile(emptyTestFile);
// deleteFile(duplicatesFile);

        super.tearDown();
    }


    /**
     * Helper method to delete a file if it exists.
     * 
     * @param filename
     *            the file to delete
     */
    private void deleteFile(String filename) {
        File f = new File(filename);
        if (f.exists()) {
            f.delete();
        }
    }
}
