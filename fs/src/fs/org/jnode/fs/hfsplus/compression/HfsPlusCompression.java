package org.jnode.fs.hfsplus.compression;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.jnode.fs.hfsplus.HfsPlusFileSystem;

/**
 * The interface for a HFS+ compression implementation.
 *
 * @author Luke Quinane
 */
public interface HfsPlusCompression {

    /**
     * Reads compressed data and decompresses it.
     *
     * @param fs the file system.
     * @param fileOffset the offset to read from.
     * @param dest the buffer to read into.
     * @throws java.io.IOException if an error occurs.
     */
    void read(HfsPlusFileSystem fs, long fileOffset, ByteBuffer dest) throws IOException;
}
