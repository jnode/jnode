package org.jnode.fs.hfs;

import org.jnode.util.BigEndian;

/**
 * A HFS master directory block (MDB).
 *
 * @author Luke Quinane
 */
public class MasterDirectoryBlock {
    /**
     * The length of this structure.
     */
    public static final int LENGTH = 162;

    /**
     * The HFS MDB signature 'BD'.
     */
    public static final int HFS_MDB_SIGNATURE = 0x4244;

    /**
     * The HFS+ embedded volume signature 'H+'.
     */
    public static final int HFSPLUS_EMBEDDED_SIGNATURE = 0x482B;

    /**
     * The data.
     */
    private final byte[] data;

    /**
     * Creates a new MDB.
     *
     * @param data the data to read from.
     */
    public MasterDirectoryBlock(byte[] data) {
        this.data = data;
    }

    /**
     * Gets the volume signature (drSigWord)
     *
     * @return the signature.
     */
    public int getSignature() {
        return BigEndian.getUInt16(data, 0);
    }

    /**
     * Gets the allocation block size (drAlBlkSiz)
     *
     * @return the allocation block size.
     */
    public long getAllocationBlockSize() {
        return BigEndian.getUInt32(data, 0x14);
    }

    /**
     * Gets the first allocation block (drAlBlSt)
     *
     * @return the first allocation block.
     */
    public int getAllocationBlockStart() {
        return BigEndian.getUInt16(data, 0x1c);
    }

    /**
     * Gets the embedded signature (drEmbedSigWord)
     *
     * @return the signature.
     */
    public int getEmbeddedSignature() {
        return BigEndian.getUInt16(data, 0x7c);
    }

    /**
     * Gets the embedded volume start block (drEmbedExtent)
     *
     * @return the start block.
     */
    public int getEmbeddedVolumeStartBlock() {
        return BigEndian.getUInt16(data, 0x7e);
    }

    /**
     * Gets the embedded volume block count (drEmbedExtent)
     *
     * @return the block count.
     */
    public int getEmbeddedVolumeBlockCount() {
        return BigEndian.getUInt16(data, 0x80);
    }
}
