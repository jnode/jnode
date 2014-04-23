package org.jnode.fs.ext4;

import java.io.IOException;
import org.apache.log4j.Logger;
import org.jnode.util.LittleEndian;

/**
 * A class for checking the ext4 multiple-mount protection (MMP) status.
 */
public class MultipleMountProtection {
    /**
     * Logger
     */
    private static final Logger log = Logger.getLogger(MultipleMountProtection.class);

    /**
     * The length of the MMP structure.
     */
    public static final int MMP_LENGTH = 1024;

    /**
     * The MMP magic number ("MMP").
     */
    public static final int MMP_MAGIC = 0x004D4D50;

    /**
     * The sequence number value for a clean unmount.
     */
    public static final int MMP_SEQ_CLEAN = 0xFF4D4D50;

    /**
     * The sequence number value when the file system is being fscked.
     */
    public static final int MMP_SEQ_FSCK = 0xE24D4D50;

    /**
     * The maximum valid sequence number value.
     */
    public static final int MMP_SEQ_MAX = 0xE24D4D4F;

    /**
     * The sequence number.
     */
    private int sequenceNumber;

    /**
     * The time the MMP block was last updated.
     */
    private long time;

    /**
     * The host name of the node which opened the file system.
     */
    private String nodeName;

    /**
     * The block device name of the file system.
     */
    private String blockDeviceName;

    /**
     * The recheck interval, in seconds.
     */
    private int checkInterval;

    /**
     * The checksum of the MMP block.
     */
    private int checksum;

    public MultipleMountProtection(byte[] data) throws IOException {
        int magic = LittleEndian.getInt32(data, 0);
        if (magic != MMP_MAGIC) {
            throw new IOException("Invalid MMP magic: " + magic);
        }

        sequenceNumber = LittleEndian.getInt32(data, 0x4);
        time = LittleEndian.getInt64(data, 0x8);
        nodeName = new String(data, 0x10, 64, "UTF-8").replace("\u0000", "");
        blockDeviceName = new String(data, 0x50, 64, "UTF-8").replace("\u0000", "");
        checkInterval = LittleEndian.getInt16(data, 0x70);
        checksum = LittleEndian.getInt32(data, 0x3fc);
    }

    /**
     * Checks if the file system appears to be in use.
     *
     * @return {@code true} if in use.
     */
    public boolean isInUse() {
        if (sequenceNumber != MMP_SEQ_CLEAN) {
            log.warn(String.format("File system appears to be in use from: %s:%s, seq:%x", nodeName, blockDeviceName,
                sequenceNumber));
            return true;

        } else {
            return false;
        }
    }
}
