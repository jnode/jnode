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
import org.jnode.driver.Device;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FSFile;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.spi.AbstractFileSystem;

/**
 * @author Matthias Treydte &lt;waldheinz at gmail.com&gt;
 */
public final class ExFatFileSystem extends AbstractFileSystem<NodeEntry> {

    private final ExFatSuperBlock sb;
    private final Node rootNode;
    private final UpcaseTable upcase;
    private final String label;
    private final ClusterBitMap bitmap;

    public ExFatFileSystem(Device device, boolean readOnly, ExFatFileSystemType type) throws FileSystemException {
        super(device, readOnly, type);

        try {
            sb = ExFatSuperBlock.read(this);
            rootNode = Node.createRoot(sb);
            final RootDirVisitor rootDirVis = new RootDirVisitor(sb);

            DirectoryParser.create(rootNode).parse(rootDirVis);

            if (rootDirVis.bitmap == null) {
                throw new FileSystemException("cluster bitmap not found");
            }

            if (rootDirVis.upcase == null) {
                throw new FileSystemException("upcase table not found");
            }

            this.upcase = rootDirVis.upcase;
            this.bitmap = rootDirVis.bitmap;
            this.label = rootDirVis.label;

        } catch (Exception e) {
            throw new FileSystemException(e);
        }
    }

    @Override
    public NodeEntry createRootEntry() throws IOException {
        return new NodeEntry(this, rootNode, null, 2);
    }

    @Override
    public long getTotalSpace() throws IOException {
        return -1;
    }

    @Override
    public long getFreeSpace() throws IOException {
        return -1;
    }

    @Override
    public long getUsableSpace() throws IOException {
        return -1;
    }

    @Override
    public String getVolumeName() throws IOException {
        return label;
    }

    @Override
    public void flush() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected FSFile createFile(FSEntry entry) throws IOException {
        return entry.getFile();
    }

    @Override
    protected FSDirectory createDirectory(FSEntry entry) throws IOException {
        return entry.getDirectory();
    }

    public UpcaseTable getUpcase() {
        return upcase;
    }

    /**
     * Gets the super block.
     *
     * @return the super block.
     */
    public ExFatSuperBlock getSuperBlock() {
        return sb;
    }

    /**
     * Gets the cluster bitmap.
     *
     * @return the bitmap.
     */
    public ClusterBitMap getClusterBitmap() {
        return bitmap;
    }

    private static class RootDirVisitor implements DirectoryParser.Visitor {

        private final ExFatSuperBlock sb;
        private ClusterBitMap bitmap;
        private UpcaseTable upcase;
        private String label;

        private RootDirVisitor(ExFatSuperBlock sb) {
            this.sb = sb;
        }

        @Override
        public void foundLabel(String label) {
            this.label = label;
        }

        @Override
        public void foundBitmap(
            long startCluster, long size) throws IOException {

            if (this.bitmap != null) {
                throw new IOException("already had a bitmap");
            }

            this.bitmap = ClusterBitMap.read(this.sb, startCluster, size);
        }

        @Override
        public void foundUpcaseTable(DirectoryParser parser, long startCluster, long size,
                                     long checksum) throws IOException {

            if (this.upcase != null) {
                throw new IOException("already had an upcase table");
            }

            this.upcase = UpcaseTable.read(
                this.sb, startCluster, size, checksum);

            /* the parser may use this table for file names to come */
            parser.setUpcase(this.upcase);
        }

        @Override
        public void foundNode(Node node, int index) {
            /* ignore */
        }

    }

}
