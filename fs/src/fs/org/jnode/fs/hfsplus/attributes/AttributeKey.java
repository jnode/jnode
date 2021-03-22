package org.jnode.fs.hfsplus.attributes;

import org.jnode.fs.hfsplus.HfsUnicodeString;
import org.jnode.fs.hfsplus.catalog.CatalogNodeId;
import org.jnode.fs.hfsplus.tree.AbstractKey;
import org.jnode.fs.hfsplus.tree.Key;
import org.jnode.util.BigEndian;

/**
 * An attributes file key (HFSPlusAttrKey).
 *
 * @author Luke Quinane
 */
public class AttributeKey extends AbstractKey {

    /**
     * The key length.
     */
    public static final int KEY_LENGTH = 14 + 127 + 1;

    /**
     * The padding value.
     */
    private int pad;

    /**
     * The file ID.
     */
    private CatalogNodeId fileId;

    /**
     * The start block.
     */
    private long startBlock;

    /**
     * The attribute name.
     */
    private HfsUnicodeString attributeName;

    /**
     * Reads in an attribute key.
     *
     * @param src the source buffer.
     * @param offset the offset to read from.
     */
    public AttributeKey(byte[] src, int offset) {
        keyLength = BigEndian.getUInt16(src, offset) + 2;
        pad = BigEndian.getUInt16(src, offset + 2);
        fileId = new CatalogNodeId(src, offset + 4);
        startBlock = BigEndian.getUInt32(src, offset + 8);
        attributeName = new HfsUnicodeString(src, offset + 0xc);
    }

    /**
     * Creates a new attribute key.
     *
     * @param fileId the file ID.
     * @param attributeName the attribute name.
     */
    public AttributeKey(CatalogNodeId fileId, String attributeName) {
        this.fileId = fileId;
        this.attributeName = new HfsUnicodeString(attributeName);
    }

    public CatalogNodeId getFileId() {
        return fileId;
    }

    public HfsUnicodeString getAttributeName() {
        return attributeName;
    }

    @Override
    public int compareTo(Key key) {
        int result = -1;
        if (key instanceof AttributeKey) {
            AttributeKey otherKey = (AttributeKey) key;
            result = getFileId().compareTo(otherKey.getFileId());
            if (result == 0) {
                // Note: this is unlikely to be correct. See TN1150 section "Unicode Subtleties" for details
                // For reading in data is should be safe since the B-Tree will be pre-sorted, but for adding new entries
                // it will cause the order to be wrong.
                result = this.getAttributeName().compareTo(otherKey.getAttributeName());
            }
        }
        return result;
    }

    @Override
    public int hashCode() {
        return 789 ^ fileId.hashCode() + attributeName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AttributeKey)) {
            return false;
        }

        AttributeKey otherKey = (AttributeKey) obj;

        return
            fileId.getId() == otherKey.fileId.getId() &&
                (attributeName.getUnicodeString() == null || otherKey.getAttributeName().getUnicodeString() == null ||
                    attributeName.getUnicodeString().equals(otherKey.getAttributeName().getUnicodeString()));
    }

    @Override
    public byte[] getBytes() {
        int length = this.getKeyLength();
        byte[] data = new byte[length];
        BigEndian.setInt16(data, 0, length);
        BigEndian.setInt16(data, 2, pad);
        System.arraycopy(fileId.getBytes(), 0, data, 4, 4);
        BigEndian.setInt32(data, 8, (int) startBlock);
        System.arraycopy(attributeName.getBytes(), 0, data, 0xc, (attributeName.getLength() * 2) + 2);
        return data;
    }

    @Override
    public String toString() {
        return String.format("[length: %d, file-id: %d, attribute-name: '%s']", getKeyLength(), getFileId().getId(),
            getAttributeName());
    }
}
