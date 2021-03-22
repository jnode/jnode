package org.jnode.fs.hfsplus.compression;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.apache.log4j.Logger;
import org.jnode.fs.hfsplus.HfsPlusFile;
import org.jnode.fs.hfsplus.HfsPlusFileSystem;
import org.jnode.fs.hfsplus.attributes.AttributeData;

/**
 * LZVN compressed data stored off in the file's resource fork.
 *
 * @author Luke Quinane
 */
public class LzvnForkCompression implements HfsPlusCompression {

    /**
     * The logger for this class.
     */
    private static final Logger log = Logger.getLogger(LzvnForkCompression.class);

    /**
     * The LZVN fork compression chunk size.
     */
    private static final int LZVN_FORK_CHUNK_SIZE = 0x10000;

    /**
     * The LZVN fork compression chunk workspace size.
     */
    private static final int LZVN_FORK_WORKSPACE_SIZE = 0x80000;

    /**
     * The HFS+ file.
     */
    private final HfsPlusFile file;

    /**
     * The detail of the fork compression if it is being used.
     */
    private LzvnForkCompressionDetails lzvnForkCompressionDetails;

    /**
     * Creates a new decompressor.
     *
     * @param file the file to read from.
     */
    public LzvnForkCompression(HfsPlusFile file) {
        this.file = file;
    }

    @Override
    public void read(HfsPlusFileSystem fs, long fileOffset, ByteBuffer dest) throws IOException {
        if (lzvnForkCompressionDetails == null) {
            lzvnForkCompressionDetails = new LzvnForkCompressionDetails(fs, file.getCatalogFile().getResources());
        }

        while (dest.remaining() > 0) {
            int chunk = (int) (fileOffset / LZVN_FORK_CHUNK_SIZE);
            long chunkOffset = lzvnForkCompressionDetails.getChunkOffset(chunk);
            long nextChunkOffset = lzvnForkCompressionDetails.getChunkOffset(chunk + 1);
            long chunkLength = nextChunkOffset - chunkOffset;

            // Read in the compressed chunk
            ByteBuffer compressed = ByteBuffer.allocate((int) chunkLength);
            file.getCatalogFile().getResources().read(fs, chunkOffset, compressed);

            // Decompress the chunk
            ByteBuffer uncompressed = ByteBuffer.allocate(LZVN_FORK_WORKSPACE_SIZE);
            LzvnDecode.LzvnState lzvnState = LzvnDecode.decodeBuffer(compressed, uncompressed);

            int decodedLength = lzvnState.getDestOffset();

            if (decodedLength == 0) {
                // Zero bytes can be returned if the stream starts with an end-of-stream op-code
                // That seems to mean that the remainder of the chunk is just uncompressed
                log.debug("Decode returned zero bytes at offset: " + fileOffset);
                compressed.position(1); // Skip the end-of-stream op-code
                compressed.limit((int) chunkLength);
                uncompressed.put(compressed);

                decodedLength = (int) chunkLength - 1;

                if (decodedLength <= 0) {
                    decodedLength = LZVN_FORK_CHUNK_SIZE;
                }
            }

            // Copy the data into the destination buffer
            uncompressed.position((int) fileOffset % LZVN_FORK_CHUNK_SIZE);
            int copySize = Math.min(dest.remaining(), decodedLength);
            uncompressed.limit(Math.min(uncompressed.capacity(), uncompressed.position() + copySize));
            dest.put(uncompressed);

            fileOffset += copySize;
        }
    }

    /**
     * The factory for this compression type.
     */
    public static class Factory implements HfsPlusCompressionFactory {
        @Override
        public HfsPlusCompression createDecompressor(HfsPlusFile file, AttributeData attributeData,
                                                     DecmpfsDiskHeader decmpfsDiskHeader) {
            return new LzvnForkCompression(file);
        }
    }
}