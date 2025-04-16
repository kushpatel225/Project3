import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * The {@code Examples} class demonstrates basic operations on byte arrays,
 * file copying, and sorting of records. It includes an example of using
 * {@link ByteBuffer} to manipulate byte data and also demonstrates how to
 * copy files using Java's NIO package.
 * 
 * @author CS3114 Instructors and TAs
 * @version {Put Something Here}
 */
public class Examples {

    /** Constant for the length of a record in bytes. */
    final static int REC_BYTES_LENGTH = 4;

    /**
     * Main method that demonstrates sorting of two records and file copying.
     * It first sorts a pair of records stored in a byte array, then prints the
     * sorted byte array. It also demonstrates copying a file using NIO and
     * handles file copying exceptions.
     *
     * @param args
     *            Command line arguments (not used in this example).
     */
    public static void main(String[] args) {
        byte[] someTwoRecs = { 3, 4, 8, 8, 2, 5, 9, 9 };
        // rec0 key is bytes 3 4 combined into a short; value is 8 and 8
        // rec1 key is bytes 2 5 combined into a short; value is 9 and 9
        sortTwoRecords(someTwoRecs);
        for (byte b : someTwoRecs) {
            System.out.print(b + " ");
        }
        System.out.println();

        // Demo of copying a file....
        int randNum = (int)(System.currentTimeMillis() % 30);
        Path src = Paths.get("src", "Examples.java"); // inside src folder is
                                                      // Examples.java
        Path dest = Paths.get("src", "ExamplesCopy" + randNum + ".java");
        try {
            Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
            System.out.println(
                "Copied myself! Refresh project in eclipse to see...");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Sorts two records in a byte array. The records are assumed to be
     * in a specific format, where each record consists of a 2-byte key
     * followed by 2 bytes of value. The method swaps the records if
     * the second key is smaller than the first key.
     *
     * @param recs
     *            A byte array containing two records to be sorted. Each record
     *            is assumed to be 4 bytes: the first 2 bytes represent the key,
     *            and the last 2 bytes represent the value.
     */
    public static void sortTwoRecords(byte[] recs) {
        final int recLen = REC_BYTES_LENGTH; // just for a shorter name
        assert recs.length == 2 * recLen;

        ByteBuffer bf = ByteBuffer.wrap(recs);
        // ^^ creates a ByteBuffer using an existing byte array.
        // Note that telling bf to do actions will do them to the array,
        // and actions that happen to that array will be visible by bf.
        // bf is an object with a reference to that array.

        short key0 = bf.getShort();
        // record zero's key is at start (byte zero)
        short key1 = bf.getShort(1 * recLen); // key1 begins at byte pos 4

        if (key1 < key0) {
            byte[] swapArea = new byte[recLen]; // a one record swap area
            System.arraycopy(recs, 0 * recLen, swapArea, 0, recLen);
            System.arraycopy(recs, 1 * recLen, recs, 0 * recLen, recLen);
            System.arraycopy(swapArea, 0, recs, 1 * recLen, recLen);
        }
        assert bf.getShort(0) < bf.getShort(1 * recLen); // they are sorted!
    }
}
