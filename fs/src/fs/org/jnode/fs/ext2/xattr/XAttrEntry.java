package org.jnode.fs.ext2.xattr;

import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import org.jnode.fs.ext2.Ext2FileSystem;
import org.jnode.util.LittleEndian;

/**
 * An extended attribute entry.
 *
 * @author Luke Quinane
 */
public class XAttrEntry {

    /**
     * The logger.
     */
    private final Logger log = Logger.getLogger(getClass());

    /**
     * The minimum size of the entry structure.
     */
    public static final int MINIMUM_SIZE = 16;

    /**
     * The map of name index to well-known name prefixes.
     */
    private static final Map<Integer, String> NAME_INDEX_PREFIX_MAP = new HashMap<Integer, String>();

    static {
        NAME_INDEX_PREFIX_MAP.put(1, "user.");
        NAME_INDEX_PREFIX_MAP.put(2, "system.posix_acl_access");
        NAME_INDEX_PREFIX_MAP.put(3, "system.posix_acl_default");
        NAME_INDEX_PREFIX_MAP.put(4, "trusted.");
        NAME_INDEX_PREFIX_MAP.put(5, "security.");
        NAME_INDEX_PREFIX_MAP.put(7, "system.");
        NAME_INDEX_PREFIX_MAP.put(8, "system.richacl");
    }

    /**
     * The data for the attribute entry.
     */
    private final byte[] data;

    /**
     * The offset into the data.
     */
    private final int offset;

    /**
     * Creates a new entry with the given data.
     *
     * @param data the data.
     * @param offset the offset into the entry.
     */
    public XAttrEntry(byte[] data, int offset) {
        this.data = data;
        this.offset = offset;
    }

    /**
     * Gets the name length.
     *
     * @return the name length.
     */
    public int getNameLength() {
        return LittleEndian.getUInt8(data, offset + 0);
    }

    /**
     * Gets the name prefix index, 0 means no prefix.
     *
     * @return the name prefix index.
     */
    public int getNameIndex() {
        return LittleEndian.getUInt8(data, offset + 1);
    }

    /**
     * Gets the offset to the value on the disk block where it is stored. For an inode attribute this value is relative
     * to the start of the first entry; for a block this value is relative to the start of the block (i.e. the header).
     *
     * @return the value offset.
     */
    public int getValueOffset() {
        return LittleEndian.getUInt16(data, offset + 2);
    }

    /**
     * Gets the disk block where the value is stored. Zero indicates the value is in the same block as this entry.
     *
     * @return the value block.
     */
    public long getValueBlock() {
        return LittleEndian.getUInt32(data, offset + 4);
    }

    /**
     * Gets the value size.
     *
     * @return the value size.
     */
    public long getValueSize() {
        return LittleEndian.getUInt32(data, offset + 8);
    }

    /**
     * Gets the hash.
     *
     * @return the hash.
     */
    public long getHash() {
        return LittleEndian.getUInt32(data, offset + 0xc);
    }

    /**
     * Gets the attribute name.
     *
     * @return the attribute name.
     */
    public String getName() {
        String name = new String(data, offset + 0x10, getNameLength(), Ext2FileSystem.ENTRY_NAME_CHARSET);
        int prefixIndex = getNameIndex();

        if (prefixIndex != 0) {
            String prefix = NAME_INDEX_PREFIX_MAP.get(prefixIndex);

            if (prefix != null) {
                name = prefix + name;
            } else {
                log.warn("Unknown xattr prefix index: " + prefixIndex);
            }
        }

        return name;
    }

    /**
     * Gets the attribute value.
     *
     * @return the value.
     */
    public byte[] getValue() {
        long valueBlockNumber = getValueBlock();

        if (valueBlockNumber != 0) {
            throw new UnsupportedOperationException("Value is stored in another block and that is not implemented yet");
        }

        byte[] value = new byte[(int) getValueSize()];
        System.arraycopy(data, getValueOffset(), value, 0, value.length);
        return value;
    }
}
