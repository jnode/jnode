
package org.jnode.fs.exfat;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.jnode.fs.FSFile;
import org.jnode.fs.spi.AbstractFSObject;

/**
 * @author Matthias Treydte &lt;waldheinz at gmail.com&gt;
 */
final class NodeFile extends AbstractFSObject implements FSFile {

    private final Node node;

    public NodeFile(ExFatFileSystem fs, Node node) {
        super(fs);

        this.node = node;
    }

    @Override
    public long getLength() {
        return this.node.getSize();
    }

    @Override
    public void setLength(long length) throws IOException {
        if (getLength() == length) return;

        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void read(long offset, ByteBuffer dest) throws IOException {
        final int len = dest.remaining();

        if (len == 0) return;

        if (offset + len > getLength()) {
            throw new EOFException();
        }

        long cluster = node.getStartCluster();
        final int bpc = node.getSuperBlock().getBytesPerCluster();
        int remain = dest.remaining();

        while (remain > 0) {
            int toRead = Math.min(bpc, remain);
            dest.limit(dest.position() + toRead);
            node.getSuperBlock().readCluster(dest, cluster);

            remain -= toRead;
            cluster = this.node.nextCluster(cluster);

            if (Cluster.invalid(cluster)) {
                throw new IOException("invalid cluster");
            }
        }
    }

    @Override
    public void write(long offset, ByteBuffer src) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void flush() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
