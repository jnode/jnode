package org.jnode.fs.hfsplus.compression;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.jnode.fs.hfsplus.HfsPlusFileSystem;
import org.jnode.fs.hfsplus.HfsPlusForkData;
import org.jnode.util.LittleEndian;

/**
 * Contains the details of the ZLIB compression fork details.
 *
 * @author Luke Quinane
 */
public class ZlibForkCompressionDetails {

    /**
     * The number of compressed chunks.
     */
    private int chunkCount;

    /**
     * The list of compressed chunk lengths.
     */
    private List<Integer> lengthArray = new ArrayList<Integer>();

    /**
     * The list of chunk offsets.
     */
    private List<Long> offsetArray = new ArrayList<Long>();

    /**
     * Creates a new instance.
     *
     * @param fs the file system to read from.
     * @param forkData the fork data containing the compressed data details.
     * @throws IOException if an error occurs.
     */
    public ZlibForkCompressionDetails(HfsPlusFileSystem fs, HfsPlusForkData forkData) throws IOException {
        // Read in the number of chunks
        ByteBuffer header = ByteBuffer.allocate(0x108);
        forkData.read(fs, 0, header);
        chunkCount = LittleEndian.getInt32(header.array(), 0x104);

        // Read in the offset length array data
        ByteBuffer offsetLengthArrayData = ByteBuffer.allocate(8 * chunkCount);
        forkData.read(fs, 0x108, offsetLengthArrayData);

        // Each chunk has an offset from the end of the header and a length stored
        for (int i = 0; i < chunkCount; i++) {
            long chunkOffset = 0x104 + LittleEndian.getUInt32(offsetLengthArrayData.array(), 8 * i);
            int chunkLength = LittleEndian.getInt32(offsetLengthArrayData.array(), 4 + 8 * i);
            offsetArray.add(chunkOffset);
            lengthArray.add(chunkLength);
        }
    }

    /**
     * Looks up the chunk length for the given chunk.
     *
     * @param chunk the chunk to look up.
     * @return the length.
     */
    public int getChunkLength(int chunk) {
        return lengthArray.get(chunk);
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
