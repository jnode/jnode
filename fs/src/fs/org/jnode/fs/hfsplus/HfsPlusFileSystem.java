/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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

import org.apache.log4j.Logger;
import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FSFile;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.hfsplus.catalog.Catalog;
import org.jnode.fs.hfsplus.catalog.CatalogNodeId;
import org.jnode.fs.hfsplus.tree.LeafRecord;
import org.jnode.fs.spi.AbstractFileSystem;

public class HfsPlusFileSystem extends AbstractFileSystem<HFSPlusEntry> {
    private final Logger log = Logger.getLogger(getClass());

    /** HFS volume header */
    private Superblock sb;

    /** Catalog special file for this instance */
    private Catalog catalog;

    /**
     * 
     * @param device
     * @param readOnly
     * @param type
     * @throws FileSystemException
     */
    public HfsPlusFileSystem(final Device device, final boolean readOnly,
            final HfsPlusFileSystemType type) throws FileSystemException {
        super(device, readOnly, type);
    }

    /**
     * 
     * @throws FileSystemException
     */
    public final void read() throws FileSystemException {
        sb = new Superblock(this, false);
        if (!sb.isAttribute(Superblock.HFSPLUS_VOL_UNMNT_BIT)) {
            log
            .info(getDevice().getId()
                    + " Filesystem has not been cleanly unmounted, mounting it readonly");
            setReadOnly(true);
        }
        if (sb.isAttribute(Superblock.HFSPLUS_VOL_SOFTLOCK_BIT)) {
            log.info(getDevice().getId()
                    + " Filesystem is marked locked, mounting it readonly");
            setReadOnly(true);
        }
        if (sb.isAttribute(Superblock.HFSPLUS_VOL_JOURNALED_BIT)) {
            log
            .info(getDevice().getId()
                    + " Filesystem is journaled, write access is not supported. Mounting it readonly");
            setReadOnly(true);
        }
        try {
            catalog = new Catalog(this);
        } catch (IOException e) {
            throw new FileSystemException(e);
        }
    }

    @Override
    protected final FSDirectory createDirectory(final FSEntry entry)
        throws IOException {
        return entry.getDirectory();
    }

    @Override
    protected final FSFile createFile(final FSEntry entry) throws IOException {
        return entry.getFile();
    }

    @Override
    protected final HFSPlusEntry createRootEntry() throws IOException {
        log.info("Create root entry.");
        LeafRecord record = catalog.getRecord(CatalogNodeId.HFSPLUS_POR_CNID);
        if (record != null) {
            return new HFSPlusEntry(this, null, "/", record);
        }
        log.error("Root entry : No record found.");
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jnode.fs.FileSystem#getFreeSpace()
     */
    public final long getFreeSpace() {
        return sb.getFreeBlocks() * sb.getBlockSize();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jnode.fs.FileSystem#getTotalSpace()
     */
    public final long getTotalSpace() {
        return sb.getTotalBlocks() * sb.getBlockSize();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jnode.fs.FileSystem#getUsableSpace()
     */
    public final long getUsableSpace() {
        return -1;
    }

    public final Catalog getCatalog() {
        return catalog;
    }

    public final Superblock getVolumeHeader() {
        return sb;
    }

    /**
     * 
     * @param params
     * 
     * @throws FileSystemException
     */
    public void create(HFSPlusParams params) throws FileSystemException {
        sb = new Superblock(this, true);
        try {
            params.initializeDefaultsValues(this.getApi().getLength(), this
                    .getFSApi().getSectorSize());
            sb.create(params);
            log.debug(sb.toString());
            // ---
            long volumeBlockUsed = sb.getTotalBlocks() - sb.getFreeBlocks()
                - ((sb.getBlockSize() == 512) ? 2 : 1);
            // ---
            log.debug("Write allocation bitmap bits to disk.");
            writeAllocationFile((int) volumeBlockUsed);
            // ---
            log.debug("Write Catalog to disk.");
            long offset = sb.getCatalogFile().getExtent(0).getStartOffset(sb.getBlockSize());
            Catalog catalog = new Catalog(params);
            this.getApi().write(offset,  catalog.getBytes());
            log.debug("Write volume header to disk.");
            this.getApi().write(1024, ByteBuffer.wrap(sb.getBytes()));
            flush();
        } catch (IOException e) {
            throw new FileSystemException("Unable to create HFS+ filesystem", e);
        } catch (ApiNotFoundException e) {
            throw new FileSystemException("Unable to create HFS+ filesystem", e);
        }
    }
    
    private void writeAllocationFile(int blockUsed) {
        int bytes = blockUsed >> 3;
        int bits  = blockUsed & 0x0007;
    }
}
