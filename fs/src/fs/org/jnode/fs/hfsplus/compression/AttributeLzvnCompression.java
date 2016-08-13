package org.jnode.fs.hfsplus.compression;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.jnode.fs.hfsplus.HfsPlusFile;
import org.jnode.fs.hfsplus.HfsPlusFileSystem;
import org.jnode.fs.hfsplus.attributes.AttributeData;

/**
 * Attribute stored LZVN compressed data.
 *
 * @author Luke Quinane
 */
public class AttributeLzvnCompression implements HfsPlusCompression {

    /**
     * The attribute data to read in.
     */
    private final AttributeData attributeData;

    /**
     * The decompression header.
     */
    private final DecmpfsDiskHeader decmpfsDiskHeader;

    /**
     * The uncompressed copy of the data.
     */
    private ByteBuffer uncompressed;

    /**
     * Creates a new instance.
     *
     * @param attributeData the attribute data to read from.
     * @param decmpfsDiskHeader the decompression header.
     */
    public AttributeLzvnCompression(AttributeData attributeData, DecmpfsDiskHeader decmpfsDiskHeader) {
        this.attributeData = attributeData;
        this.decmpfsDiskHeader = decmpfsDiskHeader;
    }

    @Override
    public void read(HfsPlusFileSystem fs, long fileOffset, ByteBuffer dest) throws IOException {
        if (uncompressed == null) {
            ByteBuffer compressed = ByteBuffer.allocate((int) attributeData.getSize() - DecmpfsDiskHeader.LENGTH);
            attributeData.read(fs, DecmpfsDiskHeader.LENGTH, compressed);
            uncompressed = ByteBuffer.allocate((int) decmpfsDiskHeader.getUncompressedSize());

            if (compressed.array()[0] == (byte) 0xff) {
                // 0xff seems to be a marker for uncompressed data. Skip this byte any just copy the data out.
                // I have no idea why they didn't use 'CMP_Type1' which is meant for uncompressed data according to the
                // headers
                compressed.position(1);
                uncompressed.put(compressed);
            } else {
                LzvnForkCompression.lzvnDecode(compressed, uncompressed);
            }
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
            return new AttributeLzvnCompression(attributeData, decmpfsDiskHeader);
        }
    }
}