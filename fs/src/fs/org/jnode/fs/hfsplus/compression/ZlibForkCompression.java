package org.jnode.fs.hfsplus.compression;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import org.jnode.fs.hfsplus.HfsPlusFile;
import org.jnode.fs.hfsplus.HfsPlusFileSystem;
import org.jnode.fs.util.FSUtils;

/**
 * ZLIB compressed data stored off in the file's resource fork.
 *
 * @author Luke Quinane
 */
public class ZlibForkCompression implements HfsPlusCompression {

    /**
     * The zlib fork compression chunk size.
     */
    private static final int ZLIB_FORK_CHUNK_SIZE = 0x10000;

    /**
     * The HFS+ file.
     */
    private final HfsPlusFile file;

    /**
     * The detail of the fork compression if it is being used.
     */
    private ZlibForkCompressionDetails zlibForkCompressionDetails;

    /**
     * Creates a new decompressor.
     *
     * @param file the file to read from.
     */
    public ZlibForkCompression(HfsPlusFile file) {
        this.file = file;
    }

    @Override
    public void read(HfsPlusFileSystem fs, long fileOffset, ByteBuffer dest) throws IOException {
        if (zlibForkCompressionDetails == null) {
            zlibForkCompressionDetails = new ZlibForkCompressionDetails(fs, file.getCatalogFile().getResources());
        }

        int chunk = FSUtils.checkedCast(fileOffset / ZLIB_FORK_CHUNK_SIZE);
        int chunkLength = zlibForkCompressionDetails.getChunkLength(chunk);
        long chunkOffset = zlibForkCompressionDetails.getChunkOffset(chunk);
        ByteBuffer compressed = ByteBuffer.allocate(chunkLength);
        file.getCatalogFile().getResources().read(fs, chunkOffset, compressed);

        ByteBuffer uncompressed = ByteBuffer.allocate(ZLIB_FORK_CHUNK_SIZE);

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
    }
}
