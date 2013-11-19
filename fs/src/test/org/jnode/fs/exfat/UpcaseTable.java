
package org.jnode.fs.exfat;

import java.io.IOException;

/**
 * TODO: Add support for compressed format:
 * http://www.ntfs.com/exfat-upcase-table.htm
 *
 * @author Matthias Treydte &lt;waldheinz at gmail.com&gt;
 */
final class UpcaseTable {

    public static UpcaseTable read(ExFatSuperBlock sb,
                                   long startCluster, long size, long checksum) throws IOException {

        Cluster.checkValid(startCluster);
        
        /* validate size */

        if ((size == 0) || (size > (0xffff * 2)) || (size % 2) != 0) {
            throw new IOException("bad upcase table size " + size);
        }

        final UpcaseTable result = new UpcaseTable(sb,
            sb.clusterToOffset(startCluster), size);
        
        /* verify checksum */
        final long actualCs = result.checkSum();
        if (checksum != actualCs) {

            final StringBuilder msg = new StringBuilder();
            msg.append("checksum mismatch (expected 0x");
            msg.append(Long.toHexString(checksum));
            msg.append(", got 0x");
            msg.append(Long.toHexString(actualCs));
            msg.append(")");

            throw new IOException(msg.toString());
        }

        return result;
    }

    private final ExFatSuperBlock sb;

    /**
     * Size in bytes.
     */
    private final long size;

    /**
     * Number of chars.
     */
    private final long chars;

    /**
     * Offset to the first byte of the table, relative to byte 0 on the
     * device.
     */
    private final long offset;

    private final DeviceAccess da;

    private UpcaseTable(ExFatSuperBlock sb, long offset, long size) {
        this.sb = sb;
        this.da = sb.getDeviceAccess();
        this.size = size;
        this.chars = size / 2;
        this.offset = offset;
    }

    public long checkSum() throws IOException {
        long sum = 0;

        for (int i = 0; i < size; i++) {
            sum = ((sum << 31) | (sum >> 1)) + da.getUint8(offset + i);
            sum &= 0xffffffffl;
        }

        return sum;
    }

    public char toUpperCase(char c) throws IOException {
        if (c > this.chars) {
            return c;
        } else {
            return da.getChar(offset + (c * 2));
        }
    }

    public String toUpperCase(String s) throws IOException {
        final StringBuilder result = new StringBuilder(s.length());

        for (char c : s.toCharArray()) {
            result.append(toUpperCase(c));
        }

        return result.toString();
    }

    public long getCharCount() {
        return this.chars;
    }

}
