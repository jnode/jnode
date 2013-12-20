package org.jnode.fs.exfat;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @author Matthias Treydte &lt;waldheinz at gmail.com&gt;
 */
final class DirectoryParser {

    private final static int ENTRY_SIZE = 32;
    private final static int ENAME_MAX_LEN = 15;
    private final static int VALID = 0x80;
    private final static int CONTINUED = 0x40;

    /**
     * If this bit is not set it means "critical", if it is set "benign".
     */
    private final static int IMPORTANCE_MASK = 0x20;

    private final static int EOD = (0x00);
    private final static int BITMAP = (0x01 | VALID);
    private final static int UPCASE = (0x02 | VALID);
    private final static int LABEL = (0x03 | VALID);
    private final static int FILE = (0x05 | VALID);
    private final static int FILE_INFO = (0x00 | VALID | CONTINUED);
    private final static int FILE_NAME = (0x01 | VALID | CONTINUED);

    private final static int FLAG_FRAGMENTED = 1;
    private final static int FLAG_CONTIGUOUS = 3;

    public static DirectoryParser create(Node node) throws IOException {
        assert (node.isDirectory()) : "not a directory"; //NOI18N

        final DirectoryParser result = new DirectoryParser(node);
        result.init();
        return result;
    }

    private final ExFatSuperBlock sb;
    private final ByteBuffer chunk;
    private final Node node;
    private long cluster;
    private UpcaseTable upcase;
    private int index;

    private DirectoryParser(Node node) {
        this.node = node;
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

            switch (entryType) {
                case LABEL:
                    parseLabel(v);
                    break;

                case BITMAP:
                    parseBitmap(v);
                    break;

                case UPCASE:
                    parseUpcaseTable(v);
                    break;

                case FILE:
                    parseFile(v);
                    break;

                case EOD:
                    return;

                default:
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

    private void parseFile(Visitor v) throws IOException {
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

        if (DeviceAccess.getUint8(chunk) != FILE_INFO) {
            throw new IOException("expected file info");
        }

        final int flag = DeviceAccess.getUint8(chunk);
        skip(1); /* unknown */
        int nameLen = DeviceAccess.getUint8(chunk);
        final int nameHash = DeviceAccess.getUint16(chunk);
        skip(2); /* unknown */
        final long realSize = DeviceAccess.getUint64(chunk);
        skip(4); /* unknown */
        final long startCluster = DeviceAccess.getUint32(chunk);
        final long size = DeviceAccess.getUint64(chunk);

        if (realSize != size) {
            throw new IOException("real size does not equal size");
        }

        conts--;

        /* read file name */
        final StringBuilder nameBuilder = new StringBuilder(nameLen);

        while (conts-- > 0) {
            advance();
            actualChecksum = addChecksum(actualChecksum);

            if (DeviceAccess.getUint8(chunk) != FILE_NAME) {
                throw new IOException("expected file name");
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

        if (referenceChecksum != actualChecksum) {
            throw new IOException("checksum mismatch");
        }

        final String name = nameBuilder.toString();

        if ((this.upcase != null) && (hashName(name) != nameHash)) {
            throw new IOException("name hash mismatch ("
                + Integer.toHexString(hashName(name)) +
                " != " + Integer.toHexString(nameHash) + ")");
        }

        v.foundNode(Node.create(sb, startCluster, attrib, name, (flag == FLAG_CONTIGUOUS), realSize, times), index);
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
