/*
 * $Id$
 *
 * Copyright (C) 2003-2015 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.fs.exfat;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @author Matthias Treydte &lt;waldheinz at gmail.com&gt;
 */
public class DirectoryParser {

    private static final int ENTRY_SIZE = 32;
    private static final int ENAME_MAX_LEN = 15;
    private static final int VALID = 0x80;
    private static final int CONTINUED = 0x40;

    /**
     * If this bit is not set it means "critical", if it is set "benign".
     */
    private static final int IMPORTANCE_MASK = 0x20;

    private static final int EOD = (0x00);
    private static final int BITMAP = (0x01 | VALID);
    private static final int UPCASE = (0x02 | VALID);
    private static final int LABEL = (0x03 | VALID);
    private static final int FILE = (0x05);
    private static final int FILE_INFO = (0x00 | CONTINUED);
    private static final int FILE_NAME = (0x01 | CONTINUED);

    private static final int FLAG_FRAGMENTED = 1;
    private static final int FLAG_CONTIGUOUS = 3;

    public static DirectoryParser create(Node node) throws IOException {
        return create(node, false, false);
    }

    public static DirectoryParser create(Node node, boolean showDeleted, boolean performChecks) throws IOException {
        assert (node.isDirectory()) : "not a directory"; //NOI18N

        final DirectoryParser result = new DirectoryParser(node, showDeleted, performChecks);
        result.init();
        return result;
    }

    private final ExFatSuperBlock sb;
    private final ByteBuffer chunk;
    private final Node node;
    private boolean showDeleted;
    private boolean performChecks;
    private long cluster;
    private UpcaseTable upcase;
    private int index;

    private DirectoryParser(Node node, boolean showDeleted, boolean performChecks) {
        this.node = node;
        this.showDeleted = showDeleted;
        this.performChecks = performChecks;
        this.sb = node.getSuperBlock();
        this.chunk = ByteBuffer.allocate(sb.getBytesPerCluster());
        this.chunk.order(ByteOrder.LITTLE_ENDIAN);
        this.cluster = node.getStartCluster();
        this.upcase = null;
    }

    public DirectoryParser setUpcase(UpcaseTable upcase) {
        if (this.upcase != null) {
            throw new IllegalStateException("already had an upcase table");
        }

        this.upcase = upcase;

        return this;
    }

    private void init() throws IOException {
        this.sb.readCluster(chunk, cluster);
        chunk.rewind();
    }

    private boolean advance() throws IOException {
        assert ((chunk.position() % ENTRY_SIZE) == 0) :
            "not on entry boundary"; //NOI18N

        if (chunk.remaining() == 0) {
            cluster = node.nextCluster(cluster);

            if (Cluster.invalid(cluster)) {
                return false;
            }

            this.chunk.rewind();
            this.sb.readCluster(chunk, cluster);
            this.chunk.rewind();
        }

        return true;
    }

    private void skip(int bytes) {
        chunk.position(chunk.position() + bytes);
    }

    public void parse(Visitor v) throws IOException {

        while (true) {
            final int entryType = DeviceAccess.getUint8(chunk);

            if (entryType == LABEL) {
                parseLabel(v);

            } else if (entryType == BITMAP) {
                parseBitmap(v);

            } else if (entryType == UPCASE) {
                parseUpcaseTable(v);

            } else if ((entryType & FILE) == FILE) {
                boolean deleted = (entryType & VALID) == 0;
                if (showDeleted || !deleted) {
                    parseFile(v, deleted);
                } else {
                    skip(ENTRY_SIZE - 1);
                }

            } else if (entryType == EOD) {
                return;

            } else {
                if ((entryType & VALID) != 0) {
                    throw new IOException(
                        "unknown entry type " + entryType);
                } else {
                    skip(ENTRY_SIZE - 1);
                }
            }

            if (!advance()) {
                return;
            }

            index++;
        }
    }

    private void parseLabel(Visitor v) throws IOException {
        final int len = DeviceAccess.getUint8(chunk);

        if (len > ENAME_MAX_LEN) {
            throw new IOException(len + " is too long");
        }

        final StringBuilder labelBuilder = new StringBuilder(len);

        for (int i = 0; i < len; i++) {
            labelBuilder.append(DeviceAccess.getChar(chunk));
        }

        v.foundLabel(labelBuilder.toString());

        skip((ENAME_MAX_LEN - len) * DeviceAccess.BYTES_PER_CHAR);
    }

    private void parseBitmap(Visitor v) throws IOException {
        skip(19); /* unknown content */

        final long startCluster = DeviceAccess.getUint32(chunk);
        final long size = DeviceAccess.getUint64(chunk);

        v.foundBitmap(startCluster, size);
    }

    private void parseUpcaseTable(Visitor v) throws IOException {
        skip(3); /* unknown */
        final long checksum = DeviceAccess.getUint32(chunk);
        assert (checksum >= 0);

        skip(12); /* unknown */
        final long startCluster = DeviceAccess.getUint32(chunk);
        final long size = DeviceAccess.getUint64(chunk);

        v.foundUpcaseTable(this, startCluster, size, checksum);
    }

    private void parseFile(Visitor v, boolean deleted) throws IOException {
        int actualChecksum = startChecksum();

        int conts = DeviceAccess.getUint8(chunk);

        if (conts < 2) {
            throw new IOException("too few continuations (" + conts + ")");
        }

        final int referenceChecksum = DeviceAccess.getUint16(chunk);
        final int attrib = DeviceAccess.getUint16(chunk);
        skip(2); /* unknown */
        final EntryTimes times = EntryTimes.read(chunk);
        skip(7); /* unknown */

        advance();

        actualChecksum = addChecksum(actualChecksum);

        if ((DeviceAccess.getUint8(chunk) & FILE_INFO) != FILE_INFO) {
            throw new IOException("expected file info");
        }

        if (deleted) {
            // Keep the index consistent with the index when not recovering deleted files
            index++;
        }

        final int flag = DeviceAccess.getUint8(chunk);
        skip(1); /* unknown */
        int nameLen = DeviceAccess.getUint8(chunk);
        final int nameHash = DeviceAccess.getUint16(chunk);
        skip(2); /* unknown */
        final long size = DeviceAccess.getUint64(chunk);
        skip(4); /* unknown */
        final long startCluster = DeviceAccess.getUint32(chunk);
        final long allocatedSize = DeviceAccess.getUint64(chunk);

        conts--;

        /* read file name */
        final StringBuilder nameBuilder = new StringBuilder(nameLen);

        while (conts-- > 0) {
            advance();
            actualChecksum = addChecksum(actualChecksum);

            if ((DeviceAccess.getUint8(chunk) & FILE_NAME) != FILE_NAME) {
                throw new IOException("expected file name");
            }

            if (deleted) {
                // Keep the index consistent with the index when not recovering deleted files
                index++;
            }

            skip(1); /* unknown */

            final int toRead = Math.min(ENAME_MAX_LEN, nameLen);

            for (int i = 0; i < toRead; i++) {
                nameBuilder.append(DeviceAccess.getChar(chunk));
            }

            nameLen -= toRead;
            assert (nameLen >= 0);

            if (nameLen == 0) {
                assert (conts == 0) : "conts remaining?!"; //NOI18N
                skip((ENAME_MAX_LEN - toRead) * DeviceAccess.BYTES_PER_CHAR);
            }
        }

        if (performChecks && !deleted && referenceChecksum != actualChecksum) {
            throw new IOException("checksum mismatch");
        }

        final String name = nameBuilder.toString();

        if (performChecks && (this.upcase != null) && (hashName(name) != nameHash)) {
            throw new IOException("name hash mismatch ("
                + Integer.toHexString(hashName(name)) +
                " != " + Integer.toHexString(nameHash) + ")");
        }

        v.foundNode(Node.create(sb, startCluster, attrib, name, (flag == FLAG_CONTIGUOUS), size, allocatedSize, times, deleted),
            index);
    }

    private int hashName(String name) throws IOException {
        int hash = 0;

        for (int i = 0; i < name.length(); i++) {
            final int c = this.upcase.toUpperCase(name.charAt(i));

            hash = ((hash << 15) | (hash >> 1)) + (c & 0xff);
            hash &= 0xffff;
            hash = ((hash << 15) | (hash >> 1)) + (c >> 8);
            hash &= 0xffff;
        }

        return (hash & 0xffff);
    }

    private int startChecksum() {
        final int oldPos = chunk.position();
        chunk.position(chunk.position() - 1);
        assert ((chunk.position() % ENTRY_SIZE) == 0);

        int result = 0;

        for (int i = 0; i < ENTRY_SIZE; i++) {
            final int b = DeviceAccess.getUint8(chunk);
            if ((i == 2) || (i == 3)) continue;
            result = ((result << 15) | (result >> 1)) + b;
            result &= 0xffff;
        }

        chunk.position(oldPos);
        return result;
    }

    private int addChecksum(int sum) {
        chunk.mark();
        assert ((chunk.position() % ENTRY_SIZE) == 0);

        for (int i = 0; i < ENTRY_SIZE; i++) {
            sum = ((sum << 15) | (sum >> 1)) + DeviceAccess.getUint8(chunk);
            sum &= 0xffff;
        }

        chunk.reset();
        return sum;
    }

    interface Visitor {

        public void foundLabel(
            String label) throws IOException;

        /**
         * @param startCluster
         * @param size         bitmap size in bytes
         */
        public void foundBitmap(
            long startCluster, long size) throws IOException;

        /**
         * @param checksum
         * @param startCluster
         * @param size         table size in bytes
         */
        public void foundUpcaseTable(DirectoryParser parser,
                                     long checksum, long startCluster, long size) throws IOException;

        public void foundNode(Node node, int index) throws IOException;
    }

}
