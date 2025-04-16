
/**
 * Interface representing the abstract data type for a buffer pool.
 * The buffer pool provides methods for reading and writing data to and from a
 * file
 * using a pool of memory buffers. Implementations of this interface are
 * expected
 * to manage data retrieval and storage efficiently, potentially utilizing
 * caching
 * mechanisms such as Least Recently Used (LRU) eviction policy.
 * @author {Your Name Here}
 * @version {Put Something Here}
 */
public interface BufferPoolADT {

    /**
     * Inserts data into the buffer pool at the specified position.
     * The data will be written to the appropriate buffer based on the position.
     * The method ensures that the data is stored correctly in the file's
     * buffer.
     *
     * @param space
     *            The byte array containing the data to be written.
     * @param sz
     *            The number of bytes to write from the byte array.
     * @param pos
     *            The position in the file at which to begin writing the data.
     *            This will be used to determine the correct buffer and offset.
     */
    void insert(byte[] space, int sz, int pos); 


    /**
     * Retrieves data from the buffer pool at the specified position.
     * The method reads the appropriate block from the pool or the file if it is
     * not cached.
     * The retrieved data is copied into the provided byte array.
     *
     * @param space
     *            The byte array where the data will be copied to.
     * @param sz
     *            The number of bytes to read into the byte array.
     * @param pos
     *            The position in the file from which to begin reading the data.
     *            This will be used to determine the correct buffer and offset.
     */
    void getbytes(byte[] space, int sz, int pos); 

}
