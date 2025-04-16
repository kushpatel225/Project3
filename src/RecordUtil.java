import java.nio.ByteBuffer;

/**
 * Utility class for working with records in byte arrays. Each record is
 * assumed
 * to be 4 bytes in length: the first 2 bytes represent a key (short),
 * and the
 * last 2 bytes represent a value (short).
 * 
 * <p>
 * This class provides two main methods: one for extracting the key from a
 * record and another for constructing a record from a key and value.
 * </p>
 * 
 * @author {Your Name Here}
 * @version {Put Something Here}
 */
class RecordUtil {

    /**
     * Extracts the key (first 2 bytes) from a record.
     * 
     * <p>
     * The record is assumed to be a byte array of length 4, where the first
     * 2 bytes represent the key as a {@code short}.
     * </p>
     *
     * @param record
     *            The byte array representing the record.
     * @return The key extracted from the record as a {@code short}.
     * @throws java.nio.BufferUnderflowException
     *             if the byte array is smaller
     *             than the required length to hold the key.
     */
    public static short getKey(byte[] record) {
        return ByteBuffer.wrap(record).getShort();
    }


    /**
     * Constructs a record from a given key and value.
     * 
     * <p>
     * The record is created by combining the key and value into a byte array
     * of length 4: the first 2 bytes represent the key as a {@code short}, and
     * the last 2 bytes represent the value as a {@code short}.
     * </p>
     * 
     * @param key
     *            The key to include in the record.
     * @param value
     *            The value to include in the record.
     * @return A byte array representing the constructed record.
     */
    public static byte[] makeRecord(short key, short value) {
        ByteBuffer buf = ByteBuffer.allocate(4);
        buf.putShort(key);
        buf.putShort(value);
        return buf.array();
    }
}
