package org.jnode.fs.hfsplus.attributes;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.jnode.fs.hfsplus.HfsPlusFileSystem;

/**
 * A base class for attribute data.
 *
 * @author Luke Quinane
 */
public abstract class AttributeData {

    /**
     * The constant for 'kHFSPlusAttrInlineData'.
     */
    public static final long ATTRIBUTE_INLINE_DATA = 0x10;

    /**
     * The constant for 'kHFSPlusAttrForkData'.
     */
    public static final long ATTRIBUTE_FORK_DATA = 0x20;

    /**
     * The constant for 'kHFSPlusAttrExtents'.
     */
    public static final long ATTRIBUTE_EXTENTS = 0x30;

    /**
     * The type of record for the attribute.
     */
    protected long recordType;

    /**
     * Gets the record type for the attribute.
     *
     * @return the record type.
     */
    public long getRecordType() {
        return recordType;
    }

    /**
     * Gets the size of the attribute.
     *
     * @return the attribute size.
     */
    public abstract long getSize();

    /**
     * Reads data from the attribute.
     *
     * @param fs the file system.
     * @param fileOffset the offset to read from.
     * @param dest the buffer to read into.
     * @throws IOException if an error occurs.
     */
    public abstract void read(HfsPlusFileSystem fs, long fileOffset, ByteBuffer dest) throws IOException;
}
