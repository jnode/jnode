package org.jnode.fs.hfsplus.compression;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.jnode.fs.hfsplus.HfsPlusFileSystem;
import org.jnode.fs.hfsplus.HfsPlusForkData;
import org.jnode.util.LittleEndian;

/**
 * Contains the details of the LZVN compression details.
 *
 * @author Luke Quinane
 */
public class LzvnForkCompressionDetails {

    /**
     * The number of compressed chunks.
     */
    private int chunkCount;

    /**
     * The list of chunk offsets.
     */
    private List<Long> offsetArray = new ArrayList<Long>();

    /**
     * Creates a new instance.
     *
     * @param fs the file system to read from.
     * @param forkData the fork data containing the compressed data details.
     * @throws java.io.IOException if an error occurs.
     */
    public LzvnForkCompressionDetails(HfsPlusFileSystem fs, HfsPlusForkData forkData) throws IOException {
        // Read in the number of chunks
        ByteBuffer offsetToFirstChunkLengthBuffer = ByteBuffer.allocate(0x4);
        forkData.read(fs, 0, offsetToFirstChunkLengthBuffer);
        int offsetToFirstChunk = LittleEndian.getInt32(offsetToFirstChunkLengthBuffer.array(), 0);
        chunkCount = offsetToFirstChunk / 4;

        // Read in the offset array data
        ByteBuffer offsetArrayData = ByteBuffer.allocate(offsetToFirstChunk);
        forkData.read(fs, 0x0, offsetArrayData);

        // Each chunk has an offset from the end of the header
        for (int i = 0; i < chunkCount; i++) {
            long chunkOffset = LittleEndian.getUInt32(offsetArrayData.array(), 4 * i);
            offsetArray.add(chunkOffset);
        }
    }

    /**
     * Looks up the chunk offset for the given chunk.
     *
     * @param chunk the chunk to look up.
     * @return the offset.
     */
    public long getChunkOffset(int chunk) {
        return offsetArray.get(chunk);
    }
}
