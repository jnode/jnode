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
import org.jnode.fs.hfsplus.attributes.AttributeData;
import org.jnode.fs.hfsplus.catalog.CatalogFile;
import org.jnode.fs.hfsplus.catalog.CatalogNodeId;
import org.jnode.fs.hfsplus.compression.CompressedAttributeData;
import org.jnode.fs.hfsplus.compression.DecmpfsDiskHeader;

public class HfsPlusFile implements FSFile, FSFileSlackSpace, FSFileStreams {

    /**
     * The associated entry.
     */
    private final HfsPlusEntry entry;

    /**
     * The associated catalog file.
     */
    private final CatalogFile file;

    /**
     * The hardlink file which contains the actual file data if this file is a hard link.
     */
    private HfsPlusFile hardLinkFile;

    /**
     * The attribute which contains the compressed file data if this file is compressed.
     */
    private CompressedAttributeData compressedData;

    public HfsPlusFile(HfsPlusEntry entry) {
        this.file = new CatalogFile(entry.getData());
        this.entry = entry;
    }

    /**
     * Gets the associated entry.
     *
     * @return the entry.
     */
    public HfsPlusEntry getEntry() {
        return entry;
    }

    @Override
    public void flush() throws IOException {

    }

    @Override
    public final long getLength() {
        if (isHardLinked()) {
            return getHardLinkFile().getLength();
        } else if (isCompressed()) {
            return getCompressedData().getSize();
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

        if (isHardLinked()) {
            getHardLinkFile().read(fileOffset, dest);
        } else if (isCompressed()) {
            getCompressedData().read(fs, fileOffset, dest);
        } else {
            file.getDatas().read(fs, fileOffset, dest);
        }
    }

    @Override
    public void write(final long fileOffset, final ByteBuffer src) throws IOException {
        // TODO Auto-generated method stub

    }

    /**
     * Checks whether the file has the hard-link flag set.
     *
     * @return {@code true} if the flag is set.
     */
    private boolean hasHardLinkFlag() {
        int flags = file.getFlags();
        return (flags & CatalogFile.FLAGS_HARDLINK_CHAIN) != 0;
    }

    /**
     * Checks whether the file is a hard-link.
     *
     * @return {@code true} if a hard-link.
     */
    public boolean isHardLinked() {
        return hasHardLinkFlag() && getHardLinkFile() != null;
    }

    /**
     * Checks whether the file is compressed.
     *
     * @return {@code true} if compressed.
     */
    public boolean isCompressed() {
        int ownerFlags = file.getPermissions().getOwnerFlags();
        return (ownerFlags & HfsPlusBSDInfo.USER_FLAG_COMPRESSED) != 0 && getCompressedData() != null;
    }

    /**
     * Gets the compressed data for this file.
     *
     * @return the compressed data, or {@code null} if no compressed data was found.
     */
    public CompressedAttributeData getCompressedData() {
        if (compressedData == null) {
            try {
                AttributeData attributeData = getFileSystem().getAttributes().getAttribute(
                    file.getFileId(), DecmpfsDiskHeader.DECMPFS_ATTRIBUTE_NAME);

                if (attributeData != null) {
                    compressedData = new CompressedAttributeData(getFileSystem(), this, attributeData);
                }
            } catch (IOException e) {
                throw new IllegalStateException("Error getting compressed data attribute", e);
            }
        }

        return compressedData;
    }

    /**
     * Gets the hard link file associated with this HFS+ file.
     *
     * @return the hardlink file.
     */
    public HfsPlusFile getHardLinkFile() {
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
            // Return null to fall through to the default reading logic.
            return null;
        }

        if (!hasHardLinkFlag()) {
            throw new IllegalStateException("File is not hard linked");
        }

        // Lookup the CNID for the root of the hardlink chain
        CatalogNodeId hardLinkRoot = new CatalogNodeId(file.getPermissions().getSpecial());

        try {
            // Lookup the hardlink in the private data directory
            String nodeName = "iNode" + hardLinkRoot.getId();
            HfsPlusEntry hardLinkEntry = (HfsPlusEntry) privateDataDirectory.getEntry(nodeName);
            hardLinkFile = new HfsPlusFile(hardLinkEntry);
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
        if (isCompressed()) {
            return new byte[0];
        }

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

    @Override
    public final String toString() {
        return String.format("HfsPlusFile:[%s '%s']", getCatalogFile().getFileId(), entry.getName());
    }

    /**
     * A file for the resource fork stream.
     */
    public class ResourceForkFile implements FSFile {
        /**
         * Gets the catalog file for the resource fork file.
         *
         * @return the catalog file.
         */
        public CatalogFile getCatalogFile() {
            return file;
        }

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
