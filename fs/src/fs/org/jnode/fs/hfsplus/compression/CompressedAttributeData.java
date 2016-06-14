package org.jnode.fs.hfsplus.compression;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Map;
import org.jnode.fs.hfsplus.HfsPlusFile;
import org.jnode.fs.hfsplus.HfsPlusFileSystem;
import org.jnode.fs.hfsplus.attributes.AttributeData;

/**
 * An implementation of {@link org.jnode.fs.hfsplus.attributes.AttributeData} which reads out compressed data from
 * another attribute.
 *
 * @author Luke Quinane
 */
public class CompressedAttributeData extends AttributeData implements Closeable {

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

            if (!fs.getRegisteredCompressionTypes().containsKey(decmpfsDiskHeader.getType())) {
                throw new UnsupportedOperationException("Unsupported compression type: " + decmpfsDiskHeader);
            }

            HfsPlusCompressionFactory factory = fs.getRegisteredCompressionTypes().get(decmpfsDiskHeader.getType());
            decompressor = factory.createDecompressor(file, attributeData, decmpfsDiskHeader);
        }

        decompressor.read(fs, fileOffset, dest);
    }

    @Override
    public void close() throws IOException {
        if (decompressor instanceof Closeable) {
            ((Closeable) decompressor).close();
        }
    }

    /**
     * Gets the map of the default supported compression types.
     *
     * @return the map.
     */
    public static Map<Long, HfsPlusCompressionFactory> getDefaultTypes() {
        Map<Long, HfsPlusCompressionFactory> compressionTypeMap = new LinkedHashMap<Long, HfsPlusCompressionFactory>();
        compressionTypeMap.put(DecmpfsDiskHeader.COMPRESSION_TYPE1, new AttributeType1Compression.Factory());
        compressionTypeMap.put(DecmpfsDiskHeader.COMPRESSION_TYPE_ZLIB, new AttributeZlibCompression.Factory());
        compressionTypeMap.put(DecmpfsDiskHeader.COMPRESSION_TYPE_ZLIB_FORK, new ZlibForkCompression.Factory());
        compressionTypeMap.put(DecmpfsDiskHeader.COMPRESSION_TYPE_LZVN, new AttributeLzvnCompression.Factory());
        compressionTypeMap.put(DecmpfsDiskHeader.COMPRESSION_TYPE_LZVN_FORK, new LzvnForkCompression.Factory());
        return compressionTypeMap;
    }
}
