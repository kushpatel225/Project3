import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import student.TestCase;

/**
 * The {@code QuicksortTest} class contains unit tests
 * for the {@link Quicksort}
 * class and its associated functionality, such as file generation, sorting,
 * and file checking. The tests validate the behavior of the sorting algorithm
 * using both ASCII and binary data files, and verify that the files are sorted
 * correctly after applying the algorithm.
 * 
 * <p>
 * These tests check the sorting of both small and large datasets, ensuring
 * that the sorting works as expected and produces the correct results. The
 * tests
 * also validate file creation and ensure that the sorting process works on
 * different file types.
 * </p>
 * 
 * @author {Your Name Here}
 * @version {Put Something Here}
 */
public class QuicksortTest extends TestCase {

    /**
     * Sets up the environment for each test, including clearing the output
     * history. This method is called before each individual test.
     * 
     * @throws Exception
     *             if there is an issue during setup
     */
    public void setUp() throws Exception {
        super.setUp();
        systemOut().clearHistory();
    }


    /**
     * Tests the file generation process by creating a file using the
     * {@link FileGenerator} class, checking the file size, and verifying
     * specific key values in the generated ASCII file.
     * 
     * @throws IOException
     *             if an error occurs while generating or reading the file
     */
    public void testFileGen() throws IOException {
        String fname = "threeBlock.txt";
        int blocks = 3;
        FileGenerator fg = new FileGenerator(fname, blocks);
        fg.setSeed(33333333); // Make generation deterministic
        fg.generateFile(FileType.ASCII);

        File f = new File(fname);
        long fileNumBytes = f.length();
        long calcedBytes = blocks * FileGenerator.BYTES_PER_BLOCK;
        assertEquals(calcedBytes, fileNumBytes); // Check file size

        RandomAccessFile raf = new RandomAccessFile(f, "r");
        short firstKey = raf.readShort(); // Read the first key
        assertEquals(8273, firstKey); // Verify first key

        raf.seek(8); // Move to byte 8 (beginning of the third record)
        short thirdKey = raf.readShort();
        assertEquals(8261, thirdKey); // Verify third key

        raf.close();
    }


    /**
     * Tests the file sorting and validation process for ASCII files.
     * This test
     * generates an ASCII file, checks that it is not sorted initially,
     * runs the
     * Quicksort algorithm, and then checks if the file is sorted.
     * 
     * @throws Exception
     *             if an error occurs during file generation, sorting, or
     *             validation
     */
    public void testCheckFile() throws Exception {
        String fname = "checkme.txt";
        FileGenerator fg = new FileGenerator(fname, 1);
        fg.setSeed(42);
        fg.generateFile(FileType.ASCII); // Re-generate the file each time

        assertFalse(CheckFile.check(fname)); // File should not be sorted
                                             // initially

        // Prepare command-line arguments for Quicksort
        String[] args = { fname, "5", "stats.txt" };

        // Call Quicksort's main method to sort the file
        Quicksort.main(args);

        // Check if the stats file was created
        File statFile = new File("stats.txt");
        assertTrue("Stat file should be created", statFile.exists());

        // Verify the file is sorted
        assertTrue(CheckFile.check(fname));
    }


    /**
     * Similar to {@link #testCheckFile()}, but tests with a binary
     * file format.
     * This test generates a binary file, checks that it is not sorted,
     * sorts it
     * using Quicksort, and then checks if the file is sorted.
     * 
     * @throws Exception
     *             if an error occurs during file generation, sorting, or
     *             validation
     */
    public void testCheckFileBin() throws Exception {
        String fname = "checkme.bin";
        FileGenerator fg = new FileGenerator(fname, 1);
        fg.setSeed(42);
        fg.generateFile(FileType.BINARY); // Re-generate the file each time

        assertFalse(CheckFile.check(fname)); // File should not be sorted
                                             // initially

        // Prepare command-line arguments for Quicksort
        String[] args = { fname, "5", "stats.txt" };

        // Call Quicksort's main method to sort the file
        Quicksort.main(args);

        // Check if the stats file was created
        File statFile = new File("stats.txt");
        assertTrue("Stat file should be created", statFile.exists());

        // Verify the file is sorted
        assertTrue(CheckFile.check(fname));
    }


    /**
     * Tests the sorting of a small binary file by generating the file,
     * checking
     * that it is not sorted, and then running the Quicksort algorithm
     * to sort
     * the file. The test then verifies that the file is sorted.
     * 
     * @throws Exception
     *             if an error occurs during file generation, sorting, or
     *             validation
     */
    public void testSorting() throws Exception {
        String fname = "input.bin";
        FileGenerator fg = new FileGenerator(fname, 1);
        fg.generateFile(FileType.BINARY);

        assertFalse(CheckFile.check(fname)); // File should not be sorted
                                             // initially

        String[] args = new String[3];
        args[0] = fname; // The file to be sorted
        args[1] = "1"; // Number of buffers
        args[2] = "stats.txt"; // Filename for sorting stats
        Quicksort.main(args); // Run the sorting algorithm

        // Verify the file is sorted
        assertTrue(CheckFile.check(fname));
    }


    /**
     * Tests sorting of a larger binary file (10 blocks, one buffer). This test
     * checks that the file is not sorted initially and verifies the file after
     * sorting.
     * 
     * @throws Exception
     *             if an error occurs during file generation, sorting, or
     *             validation
     */
    public void testSortingTenBinBlocksOneBuffer() throws Exception {
        String fname = "input.bin";
        FileGenerator fg = new FileGenerator(fname, 10);
        fg.generateFile(FileType.BINARY);

        assertFalse(CheckFile.check(fname)); // File should not be sorted
                                             // initially

        String[] args = new String[3];
        args[0] = fname; // The file to be sorted
        args[1] = "1"; // Number of buffers
        args[2] = "testSortingTenBinBlocksOneBuffer.txt"; // Filename for
                                                          // sorting stats
        Quicksort.main(args); // Run the sorting algorithm

        // Verify the file is sorted
        assertTrue(CheckFile.check(fname));
    }


    /**
     * Tests sorting of a large binary file (1000 blocks, 10 buffers).
     * This test
     * checks sorting performance on a larger dataset and verifies that
     * the file
     * is sorted after applying the Quicksort algorithm.
     * 
     * @throws Exception
     *             if an error occurs during file generation, sorting, or
     *             validation
     */
    public void testSortingOneThousandBinBlocksTenBuffer() throws Exception {
        String fname = "input.bin";
        FileGenerator fg = new FileGenerator(fname, 1000);
        fg.generateFile(FileType.BINARY);

        assertFalse(CheckFile.check(fname)); // File should not be sorted
                                             // initially

        String[] args = new String[3];
        args[0] = fname; // The file to be sorted
        args[1] = "10"; // Number of buffers
        args[2] = "testSortingOneThousandBinBlocksTenBuffer.txt"; // Filename
                                                                  // for sorting
                                                                  // stats
        Quicksort.main(args); // Run the sorting algorithm

        // Verify the file is sorted
        assertTrue(CheckFile.check(fname));
    }


    /**
     * Tests sorting of a large ASCII file (1000 blocks, 10 buffers).
     * This test
     * checks sorting performance on a large ASCII dataset and verifies
     * that
     * the file is sorted after applying the Quicksort algorithm.
     * 
     * @throws Exception
     *             if an error occurs during file generation, sorting, or
     *             validation
     */
    public void testSortingOneThousandAsciiBlocksTenBuffer() throws Exception {
        String fname = "input1.txt";
        FileGenerator fg = new FileGenerator(fname, 1000);
        fg.generateFile(FileType.ASCII);

        assertFalse(CheckFile.check(fname)); // File should not be sorted
                                             // initially

        String[] args = new String[3];
        args[0] = fname; // The file to be sorted
        args[1] = "10"; // Number of buffers
        args[2] = "testSortingOneThousandAsciiBlocksTenBuffer.txt"; // Filename
                                                                    // for
                                                                    // sorting
                                                                    // stats
        Quicksort.main(args); // Run the sorting algorithm

        // Verify the file is sorted
        assertTrue(CheckFile.check(fname));
    }

}
