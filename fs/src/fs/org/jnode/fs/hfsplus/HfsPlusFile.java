/*
 * $Id$
 *
 * Copyright (C) 2003-2014 JNode.org
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
import org.jnode.fs.hfsplus.catalog.CatalogNodeId;

public class HfsPlusFile implements FSFile, FSFileSlackSpace, FSFileStreams {

    private HfsPlusEntry entry;

    /**
     * The associated catalog file.
     */
    private CatalogFile file;

    /**
     * The hardlink file which contains the actual file data if this file is a hard link.
     */
    private CatalogFile hardLinkFile;

    public HfsPlusFile(HfsPlusEntry entry) {
        this.entry = entry;
        this.file = new CatalogFile(entry.getData());
        this.entry = entry;
    }

    @Override
    public void flush() throws IOException {

    }

    @Override
    public final long getLength() {
        int flags = file.getFlags();
        if ((flags & CatalogFile.FLAGS_HARDLINK_CHAIN) != 0) {
            return getHardLinkFile().getDatas().getTotalSize();
        } else {
            return file.getDatas().getTotalSize();
        }
    }

    @Override
    public void setLength(final long length) throws IOException {
        // TODO Auto-generated method stub
    }

    @Override
    public final void read(final long fileOffset, final ByteBuffer dest) throws IOException {
        HfsPlusFileSystem fs = getFileSystem();

        int flags = file.getFlags();
        if ((flags & CatalogFile.FLAGS_HARDLINK_CHAIN) != 0) {
            getHardLinkFile().getDatas().read(fs, fileOffset, dest);
        } else {
            file.getDatas().read(fs, fileOffset, dest);
        }
    }

    @Override
    public void write(final long fileOffset, final ByteBuffer src) throws IOException {
        // TODO Auto-generated method stub

    }

    /**
     * Gets the hard link file associated with this HFS+ file.
     *
     * @return the hardlink file.
     */
    public CatalogFile getHardLinkFile() {
        if (hardLinkFile != null) {
            return hardLinkFile;
        }

        HfsPlusDirectory privateDataDirectory = getFileSystem().getPrivateDataDirectory();
        if (privateDataDirectory == null) {
            return null;
        }

        if (entry.getParent() != null &&
            ((HfsPlusDirectory) entry.getParent()).getDirectoryId().equals(privateDataDirectory.getDirectoryId())) {
            // This file is already under the private data directory, so it should be the root file.
            return file;
        }

        int flags = file.getFlags();
        if ((flags & CatalogFile.FLAGS_HARDLINK_CHAIN) == 0) {
            throw new IllegalStateException("File is not hard linked");
        }

        // Lookup the CNID for the root of the hardlink chain
        CatalogNodeId hardLinkRoot = new CatalogNodeId(file.getPermissions().getSpecial());

        try {
            // Lookup the hardlink in the private data directory
            String nodeName = "iNode" + hardLinkRoot.getId();
            HfsPlusEntry hardLinkEntry = (HfsPlusEntry) privateDataDirectory.getEntry(nodeName);
            hardLinkFile = hardLinkEntry.createCatalogFile();
        } catch (IOException e) {
            throw new IllegalStateException("Error looking up hardlink root record: " + hardLinkRoot + " for: " + file);
        }

        return hardLinkFile;
    }

    @Override
    public boolean isValid() {
        return entry.isValid();
    }

    @Override
    public HfsPlusFileSystem getFileSystem() {
        return (HfsPlusFileSystem) entry.getFileSystem();
    }

    @Override
    public byte[] getSlackSpace() throws IOException {
        int blockSize = getFileSystem().getVolumeHeader().getBlockSize();

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
