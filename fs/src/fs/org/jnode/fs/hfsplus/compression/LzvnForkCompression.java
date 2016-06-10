package org.jnode.fs.hfsplus.compression;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.apache.log4j.Logger;
import org.jnode.fs.hfsplus.HfsPlusFile;
import org.jnode.fs.hfsplus.HfsPlusFileSystem;
import org.jnode.fs.hfsplus.attributes.AttributeData;
import org.jnode.fs.util.FSUtils;
import org.jnode.util.BigEndian;
import org.jnode.util.LittleEndian;

/**
 * LZVN compressed data stored off in the file's resource fork.
 *
 * Adapted from: https://github.com/Piker-Alpha/LZVN/blob/master/C/lzvn_decode.c
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
     * The case table lookup values.
     */
    private static final short[] CASE_TABLE =
        {
            1, 1, 1, 1, 1, 1, 2, 3, 1, 1, 1, 1, 1, 1, 4, 3,
            1, 1, 1, 1, 1, 1, 4, 3, 1, 1, 1, 1, 1, 1, 5, 3,
            1, 1, 1, 1, 1, 1, 5, 3, 1, 1, 1, 1, 1, 1, 5, 3,
            1, 1, 1, 1, 1, 1, 5, 3, 1, 1, 1, 1, 1, 1, 5, 3,
            1, 1, 1, 1, 1, 1, 0, 3, 1, 1, 1, 1, 1, 1, 0, 3,
            1, 1, 1, 1, 1, 1, 0, 3, 1, 1, 1, 1, 1, 1, 0, 3,
            1, 1, 1, 1, 1, 1, 0, 3, 1, 1, 1, 1, 1, 1, 0, 3,
            5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
            1, 1, 1, 1, 1, 1, 0, 3, 1, 1, 1, 1, 1, 1, 0, 3,
            1, 1, 1, 1, 1, 1, 0, 3, 1, 1, 1, 1, 1, 1, 0, 3,
            6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
            6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
            1, 1, 1, 1, 1, 1, 0, 3, 1, 1, 1, 1, 1, 1, 0, 3,
            5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
            7, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
            9, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10
        };

    private static final int LZVN_0 = 0;
    private static final int LZVN_1 = 1;
    private static final int LZVN_2 = 2;
    private static final int LZVN_3 = 3;
    private static final int LZVN_4 = 4;
    private static final int LZVN_5 = 5;
    private static final int LZVN_6 = 6;
    private static final int LZVN_7 = 7;
    private static final int LZVN_8 = 8;
    private static final int LZVN_9 = 9;
    private static final int LZVN_10 = 10;
    private static final int LZVN_11 = 11;
    private static final int LZVN_CASE_TABLE = 127;

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
            int decodedLength = lzvnDecode(compressed, uncompressed);

            // Copy the data into the destination buffer
            uncompressed.position((int) fileOffset % LZVN_FORK_CHUNK_SIZE);
            int copySize = Math.min(dest.remaining(), decodedLength);
            uncompressed.limit(Math.min(uncompressed.capacity(), uncompressed.position() + copySize));
            dest.put(uncompressed);

            fileOffset += copySize;
        }
    }

    /**
     * Decodes the data in the compressed buffer into the uncompressed buffer.
     *
     * @param compressedByteBuffer   the buffer to read from.
     * @param decompressedByteBuffer the buffer to write to.
     * @return the number of bytes written to the decompressed buffer.
     */
    public static int lzvnDecode(ByteBuffer compressedByteBuffer, ByteBuffer decompressedByteBuffer) {
        long destOffset = 0;
        byte[] uncompressedBuffer = decompressedByteBuffer.array();
        byte[] compressedBuffer = compressedByteBuffer.array();

        long caseTableIndex;
        long byteCount = 0;
        long currentLength = 0;
        long negativeOffset = 0;
        long address;
        int jmpTo = LZVN_CASE_TABLE;

        int decompressedSize = decompressedByteBuffer.remaining();
        decompressedSize -= 8;

        if (decompressedSize < 8) {
            return 0;
        }

        long sourceOffset = 0;
        long sourceValue = LittleEndian.getInt64(compressedBuffer, 0);
        caseTableIndex = compressedBuffer[0] & 255;

        do {
            switch (jmpTo) {
                case LZVN_CASE_TABLE:
                    log.debug(String
                        .format("caseTable[%d]", LzvnForkCompression.CASE_TABLE[FSUtils.checkedCast(caseTableIndex)]));

                    switch (LzvnForkCompression.CASE_TABLE[FSUtils.checkedCast(caseTableIndex)]) {
                        case 0:
                            caseTableIndex >>= 6;
                            sourceOffset = sourceOffset + caseTableIndex + 1;
                            byteCount = 56;
                            byteCount &= sourceValue;
                            sourceValue >>>= 8;
                            byteCount >>>= 3;
                            byteCount += 3;

                            jmpTo = LZVN_10;
                            break;

                        case 1:
                            caseTableIndex >>>= 6;
                            sourceOffset = sourceOffset + caseTableIndex + 2;
                            negativeOffset = sourceValue;
                            negativeOffset = ReverseInt64(negativeOffset);
                            byteCount = negativeOffset;
                            negativeOffset <<= 5;
                            byteCount <<= 2;
                            negativeOffset >>>= 53;
                            byteCount >>>= 61;
                            sourceValue >>>= 16;
                            byteCount += 3;

                            jmpTo = LZVN_10;
                            break;

                        case 2:
                            return (int) destOffset;

                        case 3:
                            caseTableIndex >>>= 6;
                            sourceOffset = sourceOffset + caseTableIndex + 3;
                            byteCount = 56;
                            negativeOffset = 65535;
                            byteCount &= sourceValue;
                            sourceValue >>>= 8;
                            byteCount >>>= 3;
                            negativeOffset &= sourceValue;
                            sourceValue >>>= 16;
                            byteCount += 3;

                            jmpTo = LZVN_10;
                            break;

                        case 4:
                            sourceOffset++;
                            sourceValue =
                                LittleEndian.getInt64(compressedBuffer, FSUtils.checkedCast(sourceOffset));
                            caseTableIndex = (sourceValue & 255);

                            jmpTo = LZVN_CASE_TABLE;
                            break;

                        case 5:
                            return 0;

                        case 6:
                            caseTableIndex >>>= 3;
                            caseTableIndex &= 3;
                            sourceOffset = sourceOffset + caseTableIndex + 3;
                            byteCount = sourceValue;
                            byteCount &= 775;
                            sourceValue >>>= 10;
                            negativeOffset = (byteCount & 255);
                            byteCount >>>= 8;
                            negativeOffset <<= 2;
                            byteCount |= negativeOffset;
                            negativeOffset = 16383;
                            byteCount += 3;
                            negativeOffset &= sourceValue;
                            sourceValue >>>= 14;

                            jmpTo = LZVN_10;
                            break;

                        case 7:
                            sourceValue >>>= 8;
                            sourceValue &= 255;
                            sourceValue += 16;
                            sourceOffset = sourceOffset + sourceValue + 2;

                            jmpTo = LZVN_0;
                            break;

                        case 8:
                            sourceValue &= 15;
                            sourceOffset = sourceOffset + sourceValue + 1;

                            jmpTo = LZVN_0;
                            break;

                        case 9:
                            sourceOffset += 2;
                            byteCount = sourceValue;
                            byteCount >>>= 8;
                            byteCount &= 255;
                            byteCount += 16;

                            jmpTo = LZVN_11;
                            break;

                        case 10:
                            sourceOffset++;
                            byteCount = sourceValue;
                            byteCount &= 15;

                            jmpTo = LZVN_11;
                            break;

                        default:
                            throw new IllegalStateException("Invalid case table value");
                    }
                    break;

                case LZVN_0:
                    log.debug("jmpTable(0)");

                    currentLength = destOffset + sourceValue;
                    sourceValue = -sourceValue;

                    if (currentLength > decompressedSize) {
                        jmpTo = LZVN_2;
                        break;
                    }

                case LZVN_1:
                    log.debug("jmpTable(1)");

                    do {
                        address = sourceOffset + sourceValue;
                        caseTableIndex = LittleEndian.getInt64(compressedBuffer, FSUtils.checkedCast(address));

                        address = currentLength + sourceValue;
                        LittleEndian.setInt64(uncompressedBuffer, FSUtils.checkedCast(address), caseTableIndex);
                        sourceValue += 8;

                    } while ((0xffffffffffffffffL - (sourceValue - 8)) >= 8);

                    destOffset = currentLength;

                    sourceValue = LittleEndian.getInt64(compressedBuffer, FSUtils.checkedCast(sourceOffset));
                    caseTableIndex = (sourceValue & 255);

                    jmpTo = LZVN_CASE_TABLE;
                    break;

                case LZVN_2:
                    log.debug("jmpTable(2)");

                    currentLength = (decompressedSize + 8);

                case LZVN_3:
                    log.debug("jmpTable(3)");

                    do {
                        address = sourceOffset + sourceValue;
                        caseTableIndex = compressedBuffer[FSUtils.checkedCast(address)] & 255;

                        uncompressedBuffer[FSUtils.checkedCast(destOffset)] = (byte) caseTableIndex;
                        destOffset++;

                        if (currentLength == destOffset) {
                            return (int) destOffset;
                        }

                        sourceValue++;

                    } while (sourceValue != 0);

                    sourceValue = LittleEndian.getInt64(compressedBuffer, FSUtils.checkedCast(sourceOffset));
                    caseTableIndex = (sourceValue & 255);

                    jmpTo = LZVN_CASE_TABLE;
                    break;

                case LZVN_4:
                    log.debug("jmpTable(4)");

                    currentLength = (decompressedSize + 8);
                    jmpTo = LZVN_9;
                    break;

                case LZVN_5:
                    log.debug("jmpTable(5)");

                    do {
                        address = sourceValue;
                        caseTableIndex = LittleEndian.getInt64(uncompressedBuffer, FSUtils.checkedCast(address));

                        sourceValue += 8;
                        LittleEndian.setInt64(uncompressedBuffer, FSUtils.checkedCast(destOffset), caseTableIndex);
                        destOffset += 8;
                        byteCount -= 8;

                    } while ((byteCount + 8) > 8);

                    destOffset += byteCount;
                    sourceValue = LittleEndian.getInt64(compressedBuffer, FSUtils.checkedCast(sourceOffset));
                    caseTableIndex = (sourceValue & 255);

                    jmpTo = LZVN_CASE_TABLE;
                    break;

                case LZVN_6:
                    log.debug("jmpTable(6)");

                    do {
                        uncompressedBuffer[FSUtils.checkedCast(destOffset)] = (byte) (sourceValue & 0xff);
                        destOffset++;

                        if (destOffset == currentLength) {
                            return (int) destOffset;
                        }

                        sourceValue >>>= 8;
                        caseTableIndex--;

                    } while (caseTableIndex != 1);

                case LZVN_7:
                    log.debug("jmpTable(7)");

                    sourceValue = destOffset;
                    sourceValue -= negativeOffset;

                    if (sourceValue < negativeOffset) {
                        return 0;
                    }

                    jmpTo = LZVN_4;
                    break;

                case LZVN_8:
                    log.debug("jmpTable(8)");

                    if (caseTableIndex == 0) {
                        jmpTo = LZVN_7;
                        break;
                    }

                    currentLength = (decompressedSize + 8);
                    jmpTo = LZVN_6;
                    break;

                case LZVN_9:
                    log.debug("jmpTable(9)");

                    do {
                        address = sourceValue;
                        caseTableIndex = uncompressedBuffer[FSUtils.checkedCast(address)] & 255;

                        sourceValue++;
                        uncompressedBuffer[FSUtils.checkedCast(destOffset)] = (byte) caseTableIndex;
                        destOffset++;

                        if (destOffset == currentLength) {
                            return (int) destOffset;
                        }

                        byteCount--;

                    } while (byteCount != 0);

                    sourceValue = LittleEndian.getInt64(compressedBuffer, FSUtils.checkedCast(sourceOffset));
                    caseTableIndex = (sourceValue & 255);

                    jmpTo = LZVN_CASE_TABLE;
                    break;

                case LZVN_10:
                    log.debug("jmpTable(10)");

                    currentLength = (destOffset + caseTableIndex);
                    currentLength += byteCount;

                    if (currentLength < decompressedSize) {
                        LittleEndian.setInt64(uncompressedBuffer, FSUtils.checkedCast(destOffset), sourceValue);
                        destOffset += caseTableIndex;
                        sourceValue = destOffset;

                        if (sourceValue < negativeOffset) {
                            return 0;
                        }

                        sourceValue -= negativeOffset;

                        if (negativeOffset < 8) {
                            jmpTo = LZVN_4;
                            break;
                        }

                        jmpTo = LZVN_5;
                        break;
                    }

                    jmpTo = LZVN_8;
                    break;

                case LZVN_11:
                    log.debug("jmpTable(11)");

                    sourceValue = destOffset;
                    sourceValue -= negativeOffset;
                    currentLength = destOffset + byteCount;

                    if (currentLength < decompressedSize) {
                        if (negativeOffset >= 8) {
                            jmpTo = LZVN_5;
                            break;
                        }
                    }

                    jmpTo = LZVN_4;
                    break;
            }

        } while (true);
    }

    /**
     * Reverses the byte order of a 64-bit integer value.
     *
     * @param value the value to switch.
     * @return the switched value.
     */
    private static long ReverseInt64(long value) {
        byte[] swapBuffer = new byte[8];
        LittleEndian.setInt64(swapBuffer, 0, value);
        return BigEndian.getInt64(swapBuffer, 0);
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