package org.jnode.fs.hfsplus.compression;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.jnode.fs.hfsplus.HfsPlusFile;
import org.jnode.fs.hfsplus.HfsPlusFileSystem;

/**
 * LZVN compressed data stored off in the file's resource fork.
 *
 * @author Luke Quinane
 */
public class LzvnForkCompression implements HfsPlusCompression {

    /**
     * The LZVN fork compression chunk size.
     */
    private static final int LZVN_FORK_CHUNK_SIZE = 0x10000;

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

        throw new UnsupportedOperationException("Not implemented");
    }
}