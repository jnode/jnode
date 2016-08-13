package org.jnode.fs.ext2.xattr;

import org.jnode.util.LittleEndian;

/**
 * An extended attribute header.
 *
 * @author Luke Quinane
 */
public class XAttrHeader {

    /**
     * The magic number for a xattr header.
     */
    public static final long MAGIC = 0xEA020000L;

    /**
     * The size of the header structure.
     */
    public static final int SIZE = 32;

    /**
     * The data for the attribute header.
     */
    private final byte[] data;

    /**
     * Creates a new header with the given data.
     *
     * @param data the data.
     */
    public XAttrHeader(byte[] data) {
        this.data = data;
    }

    /**
     * Gets the magic number.
     *
     * @return the magic number.
     */
    public long getMagic() {
        return LittleEndian.getUInt32(data, 0);
    }

    /**
     * Gets the reference count.
     *
     * @return the reference count.
     */
    public long getRefCount() {
        return LittleEndian.getUInt32(data, 0x4);
    }

    /**
     * Gets the number of blocks used.
     *
     * @return the number of blocks used.
     */
    public long getBlocks() {
        return LittleEndian.getUInt32(data, 0x8);
    }

    /**
     * Gets the hash value of all the attributes.
     *
     * @return the hash value of all the attributes.
     */
    public long getHash() {
        return LittleEndian.getUInt32(data, 0xc);
    }

    /**
     * Gets a checksum for the extended attribute block.
     *
     * @return the checksum.
     */
    public long getChecksum() {
        return LittleEndian.getUInt32(data, 0x10);
    }
}
