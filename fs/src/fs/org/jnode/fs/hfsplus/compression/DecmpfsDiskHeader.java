package org.jnode.fs.hfsplus.compression;

import org.jnode.util.LittleEndian;

/**
 * The header for HFS+ compressed data (decmpfs_disk_header), stored as little endian on disk.
 *
 * @author Luke Quinane
 */
public class DecmpfsDiskHeader {

    /**
     * The length of the structure.
     */
    public static final int LENGTH = 16;

    /**
     * 'Type1' - no compression.
     */
    public static final long COMPRESSION_TYPE1 = 1;

    /**
     * ZLIB compression.
     */
    public static final long COMPRESSION_TYPE_ZLIB = 3;

    /**
     * The ZLIB compressed data is stored in the resource fork.
     */
    public static final long COMPRESSION_TYPE_ZLIB_FORK = 4;

    // 9, and 10 also seem to be LZVN: http://reverseengineering.stackexchange.com/a/8233/3294

    /**
     * The LZVN compressed.
     */
    public static final long COMPRESSION_TYPE_LZVN = 7;

    /**
     * The LZVN compressed data is stored in the resource fork.
     */
    public static final long COMPRESSION_TYPE_LZVN_FORK = 8;

    /**
     * The name of the attribute which holds the compressed data.
     */
    public static final String DECMPFS_ATTRIBUTE_NAME = "com.apple.decmpfs";

    /**
     * The magic, 'cmpf'.
     */
    public static final long DECMPFS_MAGIC = 0x636d7066;

    /**
     * The magic number.
     */
    private long magic;

    /**
     * The compression type.
     */
    private long type;

    /**
     * The uncompressed size.
     */
    private long uncompressedSize;

    /**
     * Reads in an decmpfs disk header.
     *
     * @param source the source buffer.
     * @param offset the offset to read from.
     */
    public DecmpfsDiskHeader(byte[] source, int offset) {
        magic = LittleEndian.getUInt32(source, offset);
        type = LittleEndian.getUInt32(source, offset + 4);
        uncompressedSize = LittleEndian.getInt64(source, offset + 8);
    }

    /**
     * Gets the uncompressed size.
     *
     * @return the size.
     */
    public long getUncompressedSize() {
        return uncompressedSize;
    }

    /**
     * Gets the compression type.
     *
     * @return the type.
     */
    public long getType() {
        return type;
    }

    @Override
    public String toString() {
        return String.format("decmpfs-disk-header:[type:%d, length:%d]", type, uncompressedSize);
    }
}