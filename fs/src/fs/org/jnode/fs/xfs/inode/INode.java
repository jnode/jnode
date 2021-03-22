package org.jnode.fs.xfs.inode;

import org.jnode.fs.xfs.XfsObject;

/**
 * A XFS inode ('xfs_dinode_core').
 *
 * @author Luke Quinane
 */
public class INode extends XfsObject {

    /**
     * The magic number ('IN').
     */
    public static final int MAGIC = 0x494e;

    /**
     * The offset to the inode data.
     */
    public static final int DATA_OFFSET = 0x64;

    /**
     * The offset to the v3 inode data.
     */
    public static final int V3_DATA_OFFSET = 0xb0;

    /**
     * The inode number.
     */
    private final long inodeNr;

    /**
     * Creates a new inode.
     *
     * @param inodeNr the number.
     * @param data the data.
     * @param offset the offset to this inode in the data.
     */
    public INode(long inodeNr, byte[] data, int offset) {
        super(data, offset);

        this.inodeNr = inodeNr;

        if (getMagic() != MAGIC) {
            throw new IllegalStateException("Invalid inode magic: " + getMagic() + " for inode: " + inodeNr);
        }

        if (getVersion() >= 3) {
            if (getV3INodeNumber() != inodeNr) {
                throw new IllegalStateException("Stored inode (" + getV3INodeNumber() +
                    ") does not match passed in number:" + inodeNr);
            }
        }
    }

    /**
     * Gets the inode number.
     *
     * @return the inode number.
     */
    public long getINodeNr() {
        return inodeNr;
    }

    /**
     * Gets the magic number stored in this inode.
     *
     * @return the magic.
     */
    public int getMagic() {
        return getUInt16(0);
    }

    /**
     * Gets the mode stored in this inode.
     *
     * @return the mode.
     */
    public int getMode() {
        return getUInt16(0x2);
    }

    /**
     * Gets the version.
     *
     * @return the version.
     */
    public int getVersion() {
        return getUInt8(0x4);
    }

    /**
     * Gets the format stored in this inode.
     *
     * @return the format.
     */
    public int getFormat() {
        return getUInt8(0x5);
    }

    /**
     * Gets the number of hard links to this inode.
     *
     * @return the link count.
     */
    public long getLinkCount() {
        if (getVersion() == 1) {
            // Link count stored in di_onlink
            return getUInt16(0x6);
        } else {
            // Link count stored in di_nlink
            return getUInt32(0x10);
        }
    }

    /**
     * Gets the user-id of the owner.
     *
     * @return the user-id.
     */
    public long getUid() {
        return getUInt32(0x8);
    }

    /**
     * Gets the group-id of the owner.
     *
     * @return the group-id.
     */
    public long getGid() {
        return getUInt32(0xc);
    }

    /**
     * Gets the access time.
     *
     * @return the access time.
     */
    public long getAccessTime() {
        return getInt64(0x20);
    }

    /**
     * Gets the modified time.
     *
     * @return the modified time.
     */
    public long getModifiedTime() {
        return getInt64(0x28);
    }

    /**
     * Gets the created time.
     *
     * @return the created time.
     */
    public long getCreatedTime() {
        return getInt64(0x30);
    }

    /**
     * Gets the size.
     *
     * @return the size.
     */
    public long getSize() {
        return getInt64(0x38);
    }

    /**
     * Gets the number of blocks.
     *
     * @return the number of blocks.
     */
    public long getNumberOfBlocks() {
        return getUInt32(0x40);
    }

    /**
     * Gets the extent length.
     *
     * @return the extent length.
     */
    public long getExtentLength() {
        return getUInt32(0x48);
    }

    /**
     * Gets the number of extents.
     *
     * @return the extent count.
     */
    public long getExtentCount() {
        return getUInt32(0x4c);
    }

    /**
     * Gets the time the inode was created for a v3 inode.
     *
     * @return the created time.
     */
    public long getV3CreatedTime() {
        return getInt64(0x98);
    }

    /**
     * Gets the stored inode number if this is a v3 inode.
     *
     * @return the number.
     */
    public long getV3INodeNumber() {
        return getInt64(0x98);
    }

    /**
     * Gets the v3 inode file system UUID.
     *
     * @return the UUID.
     */
    public long getV3Uuid() {
        return getInt64(0xa0);
    }

    @Override
    public String toString() {
        return String.format(
            "inode:[%d version:%d format:%d size:%d uid:%d gid:%d]",
            inodeNr, getVersion(), getFormat(), getSize(), getUid(), getGid());
    }
}
