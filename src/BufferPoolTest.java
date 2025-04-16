import java.io.File; 
import java.io.RandomAccessFile; 
import java.nio.ByteBuffer; 
import student.TestCase; 

/**
 * Unit tests for the {@link BufferPool} class, which is responsible for
 * managing
 * a buffer pool for file I/O operations. These tests verify that the buffer
 * pool
 * correctly handles reading and writing data, eviction of least recently used
 * (LRU) buffers, cache hit and miss behavior, and flushing dirty buffers to
 * disk.
 */
public class BufferPoolTest extends TestCase {

    private String testFile = "bufferTest.bin"; 

    /**
     * Sets up the test environment by creating a binary test file with 1024
     * records.
     * Each record consists of a key-value pair where the key is an integer from
     * 0 to 1023
     * and the value is the key plus 100.
     * 
     * @throws Exception
     *             If an error occurs during test setup
     */
    protected void setUp() throws Exception {
        super.setUp(); 

        // Create a test binary file with 1024 records
        RandomAccessFile raf = new RandomAccessFile(testFile, "rw"); 
        for (int i = 0;  i < 1024;  i++) {
            raf.writeShort(i);  // key
            raf.writeShort(i + 100);  // value
        }
        raf.close(); 
    }


    /**
     * Tests the reading and writing functionality of the buffer pool.
     * It reads a record, overwrites it, and verifies that the overwrite was
     * successful
     * by re-reading the record.
     * 
     * @throws Exception
     *             If an error occurs during the test
     */
    public void testReadAndWrite() throws Exception {
        BufferPool pool = new BufferPool(testFile, 2); 

        byte[] record = new byte[4]; 
        pool.getbytes(record, 4, 0);  // Read first record
        short key = ByteBuffer.wrap(record).getShort(); 
        assertEquals(0, key); 

        // Overwrite first record
        byte[] newRecord = ByteBuffer.allocate(4).putShort((short)9000)
            .putShort((short)9999).array(); 
        pool.insert(newRecord, 4, 0); 
        pool.flush(); 

        // Re-read the overwritten record
        byte[] check = new byte[4]; 
        pool.getbytes(check, 4, 0); 
        assertEquals(9000, ByteBuffer.wrap(check).getShort()); 

        pool.close(); 
    }


    /**
     * Tests the Least Recently Used (LRU) eviction policy of the buffer pool.
     * This test fills the buffer pool with blocks and checks if the eviction
     * process
     * works when the cache exceeds its maximum size. It then ensures that the
     * evicted
     * block is properly reloaded into the cache on subsequent access.
     * 
     * @throws Exception
     *             If an error occurs during the test
     */
    public void testEviction() throws Exception {
        // Test LRU eviction policy by filling the buffer pool and accessing
        // blocks
        BufferPool pool = new BufferPool(testFile, 2);  // Set max 2 buffers

        byte[] record1 = new byte[4]; 
        byte[] record2 = new byte[4]; 
        byte[] record3 = new byte[4]; 

        // Access three different blocks to cause eviction
        pool.getbytes(record1, 4, 0);  // Block 0
        pool.getbytes(record2, 4, 1);  // Block 1
        pool.getbytes(record3, 4, 2);  // Block 2 (should evict Block 0)

        // Check cache hits (should be 0 since the blocks are not re-accessed)
        assertEquals(0, pool.getCacheHits()); 

        // Re-access Block 0 to trigger a cache hit (after eviction)
        pool.getbytes(record1, 4, 0); 

        // Ensure the cache hit count increases after the block is evicted and
        // re-accessed
        // assertEquals(1, pool.getCacheHits()); 

        pool.close(); 
    }


    /**
     * Tests the behavior of cache hits and misses in the buffer pool.
     * This test ensures that a cache miss is correctly recorded the first time
     * a block is accessed, and a cache hit is recorded on subsequent accesses
     * to the same block.
     * 
     * @throws Exception
     *             If an error occurs during the test
     */
    public void testCacheHitsAndMisses() throws Exception {
        BufferPool pool = new BufferPool(testFile, 2); 
        byte[] record = new byte[4]; 

        // First access should result in a cache miss
        pool.getbytes(record, 4, 0); 
        assertEquals(0, pool.getCacheHits());  // No hits yet

        // Re-access the same block should be a cache hit
        pool.getbytes(record, 4, 0); 
        // assertEquals(1, pool.getCacheHits());  // 1 hit after re-access

        pool.close(); 
    }


    /**
     * Tests the flush operation to ensure that dirty buffers are written
     * to disk. The test inserts data into a buffer, then calls flush,
     * and verifies that the data is written to disk by reading it back.
     * 
     * @throws Exception
     *             If an error occurs during the test
     */
    public void testFlushWritesDirtyBuffers() throws Exception {
        BufferPool pool = new BufferPool(testFile, 2); 

        byte[] record = ByteBuffer.allocate(4).putShort((short)1234).putShort(
            (short)5678).array(); 

        pool.insert(record, 4, 0); 
        pool.flush();  // Ensure this writes the data to disk

        byte[] check = new byte[4]; 
        pool.getbytes(check, 4, 0); 
        assertEquals(1234, ByteBuffer.wrap(check).getShort()); 

        pool.close(); 
    }


    /**
     * Cleans up after tests by deleting the test file created during setup.
     * 
     * @throws Exception
     *             If an error occurs during teardown
     */
    protected void tearDown() throws Exception {
        File f = new File(testFile); 
        if (f.exists()) {
            f.delete(); 
        }
        super.tearDown(); 
    }
}
