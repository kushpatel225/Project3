import java.io.IOException; 
import java.io.RandomAccessFile; 
import java.util.HashMap; 
import java.util.LinkedList; 
import java.util.Map; 

/**
 * Buffer pool implementation that manages a cache of blocks from a disk file.
 * Implements a Least Recently Used (LRU) eviction policy for buffer management.
 * This class serves as an abstraction layer between the sorting algorithm and
 * the
 * physical file storage, providing efficient data access and minimizing disk
 * I/O.
 * @author {Your Name Here}
 * @version {Put Something Here}
 */
class BufferPool implements BufferPoolADT {
    /** Standard buffer size in bytes (4KB) */
    private final int bufferSize = 4096; 

    /** Maximum number of buffers to keep in memory */
    private final int numBuffers; 

    /** Random access file for reading and writing */
    private final RandomAccessFile file; 

    /**
     * Buffer cache mapped by block ID for O(1) lookups
     * Separated from LRU tracking for improved performance
     */
    private final Map<Integer, Buffer> cache; 

    /** Tracks the order of buffer usage for LRU eviction policy */
    private final LinkedList<Integer> lruList; 

    /**
     * Caches the most recently accessed buffer to optimize sequential access
     * patterns
     * This is a significant optimization for algorithms with locality of
     * reference
     */
    private Buffer lastAccessedBuffer = null; 

    /** Statistics counters */
    private long cacheHits = 0; 
    private long diskReads = 0; 
    private long diskWrites = 0; 

    /**
     * Constructs a new buffer pool for the specified file.
     *
     * @param filename
     *            Name of the file to open
     * @param numBuffers
     *            Maximum number of buffers to maintain in memory
     * @throws IOException
     *             If the file cannot be opened or accessed
     */
    public BufferPool(String filename, int numBuffers) throws IOException {
        this.file = new RandomAccessFile(filename, "rw"); 
        this.numBuffers = numBuffers; 
        this.cache = new HashMap<>(numBuffers); 
        this.lruList = new LinkedList<>(); 
    }


    /**
     * Reads bytes from the file into the provided byte array.
     * Optimized for sequential and repeated access patterns.
     *
     * @param space
     *            Destination byte array
     * @param sz
     *            Number of bytes to read
     * @param pos
     *            Position in the file to start reading from
     */
    @Override
    public void getbytes(byte[] space, int sz, int pos) {
        // Calculate which block contains the data
        int blockId = pos / bufferSize; 
        int offset = pos % bufferSize; 

        // Optimization: Check if this access is to the same block as last time
        // This significantly improves performance for sequential access
        // patterns
        if (lastAccessedBuffer != null && lastAccessedBuffer
            .getBlockId() == blockId) {
            System.arraycopy(lastAccessedBuffer.getData(), offset, space, 0,
                sz); 
            return; 
        }

        Buffer buffer = getBlock(blockId); 
        lastAccessedBuffer = buffer; 
        System.arraycopy(buffer.getData(), offset, space, 0, sz); 
    }


    /**
     * Writes bytes from the provided byte array into the file.
     * Optimized for sequential and repeated access patterns.
     *
     * @param space
     *            Source byte array
     * @param sz
     *            Number of bytes to write
     * @param pos
     *            Position in the file to start writing to
     */
    @Override
    public void insert(byte[] space, int sz, int pos) {
        int blockId = pos / bufferSize; 
        int offset = pos % bufferSize; 

        // Optimization: Check if this access is to the same block as last time
        // This significantly improves performance for sequential writes
        if (lastAccessedBuffer != null && lastAccessedBuffer
            .getBlockId() == blockId) {
            System.arraycopy(space, 0, lastAccessedBuffer.getData(), offset,
                sz); 
            lastAccessedBuffer.setDirty(true); 
            return; 
        }

        Buffer buffer = getBlock(blockId); 
        lastAccessedBuffer = buffer; 
        System.arraycopy(space, 0, buffer.getData(), offset, sz); 
        buffer.setDirty(true); 
    }


    /**
     * Retrieves a block from the cache or loads it from disk if not present.
     * Implements the LRU policy for buffer management.
     *
     * @param blockId
     *            The ID of the block to retrieve
     * @return The buffer containing the requested block
     */
    private Buffer getBlock(int blockId) {
        // Check if block is already in cache
        if (cache.containsKey(blockId)) {
            cacheHits++; 
            // Update LRU status by moving this block to the end of the list
            lruList.remove((Integer)blockId); 
            lruList.addLast(blockId); 
            return cache.get(blockId); 
        }

        // If cache is full, evict least recently used block
        if (cache.size() >= numBuffers) {
            evictLRU(); 
        }

        // Load block from disk
        Buffer buffer = new Buffer(blockId); 
        try {
            file.seek((long)blockId * bufferSize); 
            int bytesRead = file.read(buffer.getData()); 
            diskReads++; 

            // Handle case where file might be smaller than a full block
            if (bytesRead < bufferSize && bytesRead > 0) {
                // Zero-fill the rest of the buffer if partial read
                for (int i = bytesRead;  i < bufferSize;  i++) {
                    buffer.getData()[i] = 0; 
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace(); 
        }

        // Add to cache and update LRU
        cache.put(blockId, buffer); 
        lruList.addLast(blockId); 
        return buffer; 
    }


    /**
     * Evicts the least recently used buffer from the cache.
     * Writes the buffer back to disk if it's been modified.
     */
    private void evictLRU() {
        if (lruList.isEmpty())
            return; 

        Integer blockId = lruList.removeFirst(); 
        Buffer buf = cache.get(blockId); 

        // Write back dirty blocks to persist changes
        if (buf.isDirty()) {
            try {
                file.seek((long)buf.getBlockId() * bufferSize); 
                file.write(buf.getData()); 
                diskWrites++; 
            }
            catch (IOException e) {
                e.printStackTrace(); 
            }
        }

        cache.remove(blockId); 

        // If the evicted buffer was the last accessed one, clear that reference
        if (lastAccessedBuffer != null && lastAccessedBuffer
            .getBlockId() == blockId) {
            lastAccessedBuffer = null; 
        }
    }


    /**
     * Flushes all modified buffers to disk.
     * Should be called before closing the file to ensure all changes are
     * persisted.
     */
    public void flush() {
        for (Buffer buf : cache.values()) {
            if (buf.isDirty()) {
                try {
                    file.seek((long)buf.getBlockId() * bufferSize); 
                    file.write(buf.getData()); 
                    diskWrites++; 
                }
                catch (IOException e) {
                    e.printStackTrace(); 
                }
                buf.setDirty(false); 
            }
        }
    }


    /**
     * Closes the buffer pool and the underlying file.
     * Flushes all modified buffers to disk before closing.
     *
     * @throws IOException
     *             If there is an error closing the file
     */
    public void close() throws IOException {
        flush(); 
        file.close(); 
    }


    /**
     * Returns the number of cache hits.
     *
     * @return The number of times a requested block was found in the cache
     */
    public long getCacheHits() {
        return cacheHits; 
    }


    /**
     * Returns the number of disk reads.
     *
     * @return The number of times a block had to be read from disk
     */
    public long getDiskReads() {
        return diskReads; 
    }


    /**
     * Returns the number of disk writes.
     *
     * @return The number of times a block had to be written to disk
     */
    public long getDiskWrites() {
        return diskWrites; 
    }
}




/**
 * Represents a single buffer in the buffer pool.
 * Contains the block data and metadata about the buffer state.
 */
class Buffer {
    /** ID of the block this buffer represents */
    private final int blockId; 

    /** The actual data of the block */
    private final byte[] data = new byte[4096]; 

    /**
     * Flag indicating whether the buffer has been modified since being loaded
     */
    private boolean dirty = false; 

    /**
     * Constructs a new buffer for the specified block.
     *
     * @param blockId
     *            The ID of the block this buffer will represent
     */
    public Buffer(int blockId) {
        this.blockId = blockId; 
    }


    /**
     * Gets the data array for this buffer.
     *
     * @return The byte array containing this buffer's data
     */
    public byte[] getData() {
        return data; 
    }


    /**
     * Checks if this buffer has been modified.
     *
     * @return true if the buffer has been modified, false otherwise
     */
    public boolean isDirty() {
        return dirty; 
    }


    /**
     * Sets the dirty flag for this buffer.
     *
     * @param dirty
     *            true to mark the buffer as modified, false otherwise
     */
    public void setDirty(boolean dirty) {
        this.dirty = dirty; 
    }


    /**
     * Gets the ID of the block this buffer represents.
     *
     * @return The block ID
     */
    public int getBlockId() {
        return blockId; 
    }
}
