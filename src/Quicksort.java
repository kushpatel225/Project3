
/**
 * {Project Description Here}
 */

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * The class containing the main method.
 *
 * @author {Your Name Here}
 * @version {Put Something Here}
 */

// On my honor:
//
// - I have not used source code obtained from another student,
// or any other unauthorized source, either modified or
// unmodified.
//
// - All source code and documentation used in my program is
// either my original work, or was derived by me from the
// source code published in the textbook for this course.
//
// - I have not discussed coding details about this project with
// anyone other than my partner (in the case of a joint
// submission), instructor, ACM/UPE tutors or the TAs assigned
// to this course. I understand that I may discuss the concepts
// of this program with other students, and that another student
// may help me debug my program so long as neither of us writes
// anything during the discussion or modifies any computer file
// during the discussion. I have violated neither the spirit nor
// letter of this restriction.

public class Quicksort {

    /**
     * @param args
     *            Command line parameters. See the project spec!!!
     */
    public static void main(String[] args) {
        // This is the main file for the program.
        if (args.length != 3) {
            System.err.println("Usage: java Quicksort <data-file-name> "
                + "<num-buffers> <stat-file-name>");
            return;
        }

        String dataFile = args[0];
        int numBuffers = Integer.parseInt(args[1]);
        String statFile = args[2];

        try {
            BufferPool pool = new BufferPool(dataFile, numBuffers);
            long fileSize = new File(dataFile).length();
            Sorter sorter = new Sorter(pool, fileSize);

            long start = System.currentTimeMillis();
            sorter.sort();
            long end = System.currentTimeMillis();

            pool.close();

            try (FileWriter fw = new FileWriter(statFile, true)) {
                fw.write("File: " + dataFile + "\n");
                fw.write("Cache Hits: " + pool.getCacheHits() + "\n");
                fw.write("Disk Reads: " + pool.getDiskReads() + "\n");
                fw.write("Disk Writes: " + pool.getDiskWrites() + "\n");
                fw.write("Sort Time (ms): " + (end - start) + "\n\n");
            }

        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
