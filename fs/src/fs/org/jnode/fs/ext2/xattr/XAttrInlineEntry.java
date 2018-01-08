package org.jnode.fs.ext2.xattr;

/**
 * An inline extended attribute entry.
 *
 * @author Luke Quinane
 */
public class XAttrInlineEntry extends XAttrEntry {

    /**
     * Creates a new entry with the given data.
     *
     * @param data the data.
     * @param offset the offset into the entry.
     */
    public XAttrInlineEntry(byte[] data, int offset) {
        super(data, offset);
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
        // For some reason the offset needs to be applied to the value offset
        System.arraycopy(data, offset + getValueOffset(), value, 0, value.length);
        return value;
    }
}
