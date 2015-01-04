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
 
package org.jnode.fs.hfsplus;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import org.jnode.fs.FSFile;
import org.jnode.fs.FSFileSlackSpace;
import org.jnode.fs.FSFileStreams;
import org.jnode.fs.FileSystem;
import org.jnode.fs.hfsplus.catalog.CatalogFile;

public class HfsPlusFile implements FSFile, FSFileSlackSpace, FSFileStreams {

    private HfsPlusEntry entry;

    private CatalogFile file;

    public HfsPlusFile(HfsPlusEntry entry) {
        this.entry = entry;
        this.file = new CatalogFile(entry.getData());
    }

    @Override
    public void flush() throws IOException {

    }

    @Override
    public final long getLength() {
        return file.getDatas().getTotalSize();
    }

    @Override
    public void setLength(final long length) throws IOException {
        // TODO Auto-generated method stub
    }

    @Override
    public final void read(final long fileOffset, final ByteBuffer dest) throws IOException {
        HfsPlusFileSystem fs = (HfsPlusFileSystem) getFileSystem();
        file.getDatas().read(fs, fileOffset, dest);
    }

    @Override
    public void write(final long fileOffset, final ByteBuffer src) throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isValid() {
        return entry.isValid();
    }

    @Override
    public FileSystem<?> getFileSystem() {
        return entry.getFileSystem();
    }

    @Override
    public byte[] getSlackSpace() throws IOException {
        int blockSize = ((HfsPlusFileSystem) getFileSystem()).getVolumeHeader().getBlockSize();

        int slackSpaceSize = blockSize - (int) (getLength() % blockSize);

        if (slackSpaceSize == blockSize) {
            slackSpaceSize = 0;
        }

        byte[] slackSpace = new byte[slackSpaceSize];
        read(getLength(), ByteBuffer.wrap(slackSpace));

        return slackSpace;
    }

    @Override
    public Map<String, FSFile> getStreams() {
        Map<String, FSFile> streams = new HashMap<String, FSFile>();

        if (file.getResources().getTotalSize() > 0) {
            streams.put("rsrc", new ResourceForkFile());
        }

        return streams;
    }

    /**
     * Gets the catalog file.
     *
     * @return the catalog file.
     */
    public CatalogFile getCatalogFile() {
        return file;
    }

    /**
     * A file for the resource fork stream.
     */
    public class ResourceForkFile implements FSFile {
        @Override
        public long getLength() {
            return file.getResources().getTotalSize();
        }

        @Override
        public void setLength(long length) throws IOException {
            throw new UnsupportedOperationException("Not implemented yet");
        }

        @Override
        public void read(long fileOffset, ByteBuffer dest) throws IOException {
            HfsPlusFileSystem fs = (HfsPlusFileSystem) getFileSystem();
            file.getResources().read(fs, fileOffset, dest);
        }

        @Override
        public void write(long fileOffset, ByteBuffer src) throws IOException {
            throw new UnsupportedOperationException("Not implemented yet");
        }

        @Override
        public void flush() throws IOException {
        }

        @Override
        public boolean isValid() {
            return entry.isValid();
        }

        @Override
        public FileSystem<?> getFileSystem() {
            return HfsPlusFile.this.getFileSystem();
        }
    }
}
