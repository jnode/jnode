package org.jnode.fs.hfsplus.compression;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.jnode.fs.hfsplus.HfsPlusFile;
import org.jnode.fs.hfsplus.HfsPlusFileSystem;
import org.jnode.fs.hfsplus.attributes.AttributeData;

/**
 * An implementation of {@link org.jnode.fs.hfsplus.attributes.AttributeData} which reads out compressed data from
 * another attribute.
 *
 * @author Luke Quinane
 */
public class CompressedAttributeData extends AttributeData {

    /**
     * The HFS+ file.
     */
    private HfsPlusFile file;

    /**
     * The attribute data which has the compressed copy of the data.
     */
    private AttributeData attributeData;

    /**
     * The compression on-disk header.
     */
    private DecmpfsDiskHeader decmpfsDiskHeader;

    /**
     * The decompressor to use to read back the data.
     */
    private HfsPlusCompression decompressor;

    public CompressedAttributeData(HfsPlusFileSystem fs, HfsPlusFile file, AttributeData attributeData) {
        this.file = file;
        this.attributeData = attributeData;

        try {
            ByteBuffer buffer = ByteBuffer.allocate(DecmpfsDiskHeader.LENGTH);
            attributeData.read(fs, 0, buffer);
            decmpfsDiskHeader = new DecmpfsDiskHeader(buffer.array(), 0);
        } catch (IOException e) {
            throw new IllegalStateException("Error reading compression disk header", e);
        }
    }

    @Override
    public long getSize() {
        return decmpfsDiskHeader.getUncompressedSize();
    }

    /**
     * Checks whether the compressed data is stored in the file's resource fork.
     *
     * @return {@code true} if compressed in the resource fork.
     */
    public boolean isCompressedInFork() {
        return
            decmpfsDiskHeader.getType() == DecmpfsDiskHeader.COMPRESSION_TYPE_ZLIB_FORK ||
            decmpfsDiskHeader.getType() == DecmpfsDiskHeader.COMPRESSION_TYPE_LZVN_FORK;
    }

    /**
     * Gets the compression type.
     *
     * @return the compression type.
     */
    public long getCompressionType() {
        return decmpfsDiskHeader.getType();
    }

    @Override
    public void read(HfsPlusFileSystem fs, long fileOffset, ByteBuffer dest) throws IOException {
        if (decompressor == null) {
            if (decmpfsDiskHeader.getType() == DecmpfsDiskHeader.COMPRESSION_TYPE1) {
                decompressor = new AttributeType1Compression(attributeData);
            } else if (decmpfsDiskHeader.getType() == DecmpfsDiskHeader.COMPRESSION_TYPE_ZLIB) {
                decompressor = new AttributeZlibCompression(attributeData, decmpfsDiskHeader);
            } else if (decmpfsDiskHeader.getType() == DecmpfsDiskHeader.COMPRESSION_TYPE_ZLIB_FORK) {
                decompressor = new ZlibForkCompression(file);
            } else if (decmpfsDiskHeader.getType() == DecmpfsDiskHeader.COMPRESSION_TYPE_LZVN) {
                decompressor = new AttributeLzvnCompression(attributeData, decmpfsDiskHeader);
            } else if (decmpfsDiskHeader.getType() == DecmpfsDiskHeader.COMPRESSION_TYPE_LZVN_FORK) {
                decompressor = new LzvnForkCompression(file);
            } else {
                throw new UnsupportedOperationException("Unsupported compression type: " + decmpfsDiskHeader);
            }
        }

        decompressor.read(fs, fileOffset, dest);
    }
}
