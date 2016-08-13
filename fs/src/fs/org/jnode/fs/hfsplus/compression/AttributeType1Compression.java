package org.jnode.fs.hfsplus.compression;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.jnode.fs.hfsplus.HfsPlusFile;
import org.jnode.fs.hfsplus.HfsPlusFileSystem;
import org.jnode.fs.hfsplus.attributes.AttributeData;

/**
 * Attribute stored 'compressed' data 'Type 1'. Type 1 is no-compression, i.e. data is just stored.
 *
 * @author Luke Quinane
 */
public class AttributeType1Compression implements HfsPlusCompression {

    /**
     * The attribute data to read in.
     */
    private final AttributeData attributeData;

    /**
     * The uncompressed copy of the data.
     */
    private ByteBuffer uncompressed;

    /**
     * Creates a new instance.
     *
     * @param attributeData the attribute data to read from.
     */
    public AttributeType1Compression(AttributeData attributeData) {
        this.attributeData = attributeData;
    }

    @Override
    public void read(HfsPlusFileSystem fs, long fileOffset, ByteBuffer dest) throws IOException {
        if (uncompressed == null) {
            // 'Type1' is no compression, just copy the data out
            uncompressed = ByteBuffer.allocate((int) attributeData.getSize() - DecmpfsDiskHeader.LENGTH);
            attributeData.read(fs, DecmpfsDiskHeader.LENGTH, uncompressed);
        }

        uncompressed.position((int) fileOffset);
        uncompressed.limit(Math.min(uncompressed.capacity(), uncompressed.position() + dest.remaining()));
        dest.put(uncompressed);
    }

    /**
     * The factory for this compression type.
     */
    public static class Factory implements HfsPlusCompressionFactory {
        @Override
        public HfsPlusCompression createDecompressor(HfsPlusFile file, AttributeData attributeData,
                                                     DecmpfsDiskHeader decmpfsDiskHeader) {
            return new AttributeType1Compression(attributeData);
        }
    }
}
