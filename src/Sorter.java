/**
 * Sorter class that performs a hybrid quicksort on records stored in a binary
 * file.
 * Uses a 3-way partitioned quicksort for large partitions and switches to
 * insertion sort for small partitions to improve overall performance.
 * Interacts with the file through a BufferPool to minimize disk I/O operations.
 * 
 * @author Your Name
 * @version 1.0
 */
class Sorter {
    /** Reference to the buffer pool for file access */
    private final BufferPool bufferPool;

    /** Total number of records in the file */
    private final long totalRecords;

    /**
     * Threshold for switching to insertion sort.
     * Small partitions are more efficiently sorted with insertion sort due to
     * lower overhead compared to the quicksort algorithm.
     */
    private static final int INSERTION_SORT_THRESHOLD = 16;

    /** Reusable buffer for record reads to reduce memory allocations */
    private byte[] tempBuffer = new byte[4];

    /**
     * Constructor for the Sorter class.
     * Initializes the sorting environment with the provided buffer pool
     * and calculates the total number of records based on file size.
     * 
     * @param pool
     *            BufferPool instance for file access
     * @param fileSize
     *            Size of the file in bytes
     */
    public Sorter(BufferPool pool, long fileSize) {
        this.bufferPool = pool;
        this.totalRecords = fileSize / 4;
    }


    /**
     * Starts the quicksort algorithm on the entire array.
     * This is the public entry point to sort all records in the file.
     * It delegates to the quicksort method with the full range of indices.
     */
    public void sort() {
        quicksort(0, totalRecords - 1);
    }


    /**
     * Reads a record's key at the specified index.
     * Uses a reusable buffer to minimize memory allocations.
     * The key is extracted from the 4-byte record using RecordUtil.
     *
     * @param index
     *            Index of the record to read
     * @return The key value from the record
     */
    private short readKeyAt(long index) {
        bufferPool.getbytes(tempBuffer, 4, (int)index * 4);
        return RecordUtil.getKey(tempBuffer);
    }


    /**
     * Finds the median of three values.
     * Used for improved pivot selection in the quicksort algorithm.
     * This helps avoid worst-case performance on already sorted or nearly
     * sorted data.
     *
     * @param a
     *            First value
     * @param b
     *            Second value
     * @param c
     *            Third value
     * @return The median value among the three inputs
     */
    private short median(short a, short b, short c) {
        if ((a <= b && b <= c) || (c <= b && b <= a))
            return b;
        if ((b <= a && a <= c) || (c <= a && a <= b))
            return a;
        return c;
    }


    /**
     * Recursive quicksort method using 3-way partitioning.
     * Switches to insertion sort for small partitions.
     * Uses median-of-three for pivot selection to avoid worst-case scenarios.
     * The 3-way partitioning approach (< pivot, = pivot, > pivot) is especially
     * efficient when there are many duplicate keys in the data.
     *
     * @param low
     *            Start index of the partition to sort
     * @param high
     *            End index of the partition to sort
     */
    private void quicksort(long low, long high) {
        // Use insertion sort for small arrays - significant performance
        // improvement
        if (high - low <= INSERTION_SORT_THRESHOLD) {
            insertionSort(low, high);
            return;
        }

        if (low >= high)
            return;

        // Improved pivot selection using median-of-3 strategy
        // This helps avoid worst-case performance on already sorted or reverse
        // sorted data
        long mid = low + (high - low) / 2;
        short valLow = readKeyAt(low);
        short valMid = readKeyAt(mid);
        short valHigh = readKeyAt(high);
        short pivot = median(valLow, valMid, valHigh);

        // 3-way partitioning (< pivot, = pivot, > pivot)
        // This performs better than standard quicksort when there are many
        // duplicates
        long lt = low;
        long gt = high;
        long i = low;
        byte[] currentRec = new byte[4]; // Reuse this buffer for current record

        while (i <= gt) {
            bufferPool.getbytes(currentRec, 4, (int)i * 4);
            short key = RecordUtil.getKey(currentRec);

            if (key < pivot) {
                swap(lt++, i++);
            }
            else if (key > pivot) {
                swap(i, gt--);
            }
            else {
                i++;
            }
        }

        // Recursive calls for the < and > partitions
        // Note: the = partition is already in place and doesn't need sorting
        quicksort(low, lt - 1);
        quicksort(gt + 1, high);
    }


    /**
     * Insertion sort implementation for small partitions.
     * More efficient than quicksort for small arrays due to lower overhead.
     * This method sorts records in-place by repeatedly inserting each element
     * at its correct position in the already sorted section of the array.
     *
     * @param low
     *            Start index of the partition to sort
     * @param high
     *            End index of the partition to sort
     */
    private void insertionSort(long low, long high) {
        for (long i = low + 1; i <= high; i++) {
            byte[] key = new byte[4];
            bufferPool.getbytes(key, 4, (int)i * 4);

            long j = i - 1;
            while (j >= low) {
                byte[] current = new byte[4];
                bufferPool.getbytes(current, 4, (int)j * 4);

                if (RecordUtil.getKey(current) <= RecordUtil.getKey(key)) {
                    break;
                }

                // Move record right
                bufferPool.insert(current, 4, (int)(j + 1) * 4);
                j--;
            }

            // Only insert if the position has changed
            if (j + 1 != i) {
                bufferPool.insert(key, 4, (int)(j + 1) * 4);
            }
        }
    }


    /**
     * Swaps two records in the file.
     * Uses buffer pool to read and write the records.
     * This operation reads both records and then writes them back at each
     * other's
     * position, effectively swapping them in the file.
     *
     * @param i
     *            Index of first record
     * @param j
     *            Index of second record
     */
    private void swap(long i, long j) {
        if (i == j)
            return;

        byte[] iRec = new byte[4];
        byte[] jRec = new byte[4];

        bufferPool.getbytes(iRec, 4, (int)i * 4);
        bufferPool.getbytes(jRec, 4, (int)j * 4);

        bufferPool.insert(jRec, 4, (int)i * 4);
        bufferPool.insert(iRec, 4, (int)j * 4);
    }
}
