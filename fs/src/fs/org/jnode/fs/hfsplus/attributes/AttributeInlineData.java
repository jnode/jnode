package org.jnode.fs.hfsplus.attributes;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.jnode.fs.hfsplus.HfsPlusFileSystem;
import org.jnode.util.BigEndian;

/**
 * Attribute data stored inline in the attributes file b-tree ('HFSPlusAttrData').
 *
 * @author Luke Quinane
 */
public class AttributeInlineData extends AttributeData {

    /**
     * The attribute size.
     */
    private long attributeSize;

    /**
     * The attribute data.
     */
    private byte[] attributeData;

    /**
     * Reads in a new inline attribute.
     *
     * @param source the source buffer to read from.
     * @param offset the offset to read from.
     */
    public AttributeInlineData(byte[] source, int offset) {
        recordType = BigEndian.getUInt32(source, offset);
        attributeSize = BigEndian.getUInt32(source, offset + 0xc);

        attributeData = new byte[(int) attributeSize];
        System.arraycopy(source, offset + 0x10, attributeData, 0, attributeData.length);
    }

    @Override
    public long getSize() {
        return attributeSize;
    }

    @Override
    public void read(HfsPlusFileSystem fs, long fileOffset, ByteBuffer dest) throws IOException {
        dest.put(attributeData, (int) fileOffset, dest.remaining());
    }

    @Override
    public String toString() {
        return String.format("inline-attribute:[length:%d]", attributeSize);
    }
}
