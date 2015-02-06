package org.jnode.fs.hfsplus.compression;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
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
     * The zlib fork compression chunk size.
     */
    private static final int ZLIB_FORK_CHUNK_SIZE = 0x10000;

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
     * The uncompressed copy of the data.
     */
    private ByteBuffer uncompressed;

    /**
     * The detail of the fork compression if it is being used.
     */
    private ZlibForkCompressionDetails zlibForkCompressionDetails;

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
        return decmpfsDiskHeader.getType() == DecmpfsDiskHeader.DATA_IN_FORK;
    }

    @Override
    public void read(HfsPlusFileSystem fs, long fileOffset, ByteBuffer dest) throws IOException {
        if (decmpfsDiskHeader.getType() == DecmpfsDiskHeader.COMPRESSION_TYPE1 ||
            decmpfsDiskHeader.getType() == DecmpfsDiskHeader.COMPRESSION_TYPE_ZLIB) {

            getUncompressed(fs);

            uncompressed.position((int) fileOffset);
            uncompressed.limit(uncompressed.position() + dest.remaining());
            dest.put(uncompressed);

        } else if (decmpfsDiskHeader.getType() == DecmpfsDiskHeader.DATA_IN_FORK) {
            if (zlibForkCompressionDetails == null) {
                zlibForkCompressionDetails = new ZlibForkCompressionDetails(fs, file.getCatalogFile().getResources());
            }

            int chunk = (int) (fileOffset / ZLIB_FORK_CHUNK_SIZE);
            int chunkLength = zlibForkCompressionDetails.getChunkLength(chunk);
            long chunkOffset = zlibForkCompressionDetails.getChunkOffset(chunk);
            ByteBuffer compressed = ByteBuffer.allocate(chunkLength);
            file.getCatalogFile().getResources().read(fs, chunkOffset, compressed);

            ByteBuffer uncompressed = ByteBuffer.allocate((int) decmpfsDiskHeader.getUncompressedSize());

            Inflater inflater = new Inflater();
            inflater.setInput(compressed.array());

            try {
                inflater.inflate(uncompressed.array());
            } catch (DataFormatException e) {
                throw new IllegalStateException("Error uncompressing data", e);
            }

            uncompressed.position((int) fileOffset % ZLIB_FORK_CHUNK_SIZE);
            uncompressed.limit(uncompressed.position() + dest.remaining());
            dest.put(uncompressed);

        } else {
            throw new UnsupportedOperationException("Unsupported compression type: " + decmpfsDiskHeader);
        }
    }

    /**
     * Gets the uncompressed buffer for inline compression types.
     *
     * @param fs the file system to read from.
     * @throws IOException if an error occurs.
     */
    private void getUncompressed(HfsPlusFileSystem fs) throws IOException {
        if (uncompressed != null) {
            return;
        }

        if (decmpfsDiskHeader.getType() == DecmpfsDiskHeader.COMPRESSION_TYPE1) {
            // 'Type1' is no compression, just copy the data out
            uncompressed = ByteBuffer.allocate((int) attributeData.getSize() - DecmpfsDiskHeader.LENGTH);
            attributeData.read(fs, DecmpfsDiskHeader.LENGTH, uncompressed);

        } else if (decmpfsDiskHeader.getType() == DecmpfsDiskHeader.COMPRESSION_TYPE_ZLIB) {
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
                Inflater inflater = new Inflater();
                inflater.setInput(compressed.array());

                try {
                    inflater.inflate(uncompressed.array());
                } catch (DataFormatException e) {
                    throw new IllegalStateException("Error uncompressing data", e);
                }
            }
        }
    }
}
