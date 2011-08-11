/*
 * $Id$
 *
 * Copyright (C) 2003-2010 JNode.org
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

import org.apache.log4j.Logger;
import org.jnode.driver.Device;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FSFile;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.hfsplus.catalog.Catalog;
import org.jnode.fs.hfsplus.catalog.CatalogNodeId;
import org.jnode.fs.hfsplus.tree.LeafRecord;
import org.jnode.fs.spi.AbstractFileSystem;

public class HfsPlusFileSystem extends AbstractFileSystem<HfsPlusEntry> {
    private final Logger log = Logger.getLogger(getClass());

    /** HFS volume header */
    private SuperBlock volumeHeader;

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
        volumeHeader = new SuperBlock(this, false);
        log.debug(volumeHeader.toString());
        if (!volumeHeader.isAttribute(SuperBlock.HFSPLUS_VOL_UNMNT_BIT)) {
            log.info(getDevice().getId() +
                    " Filesystem has not been cleanly unmounted, mounting it readonly");
            setReadOnly(true);
        }
        if (volumeHeader.isAttribute(SuperBlock.HFSPLUS_VOL_SOFTLOCK_BIT)) {
            log.info(getDevice().getId() + " Filesystem is marked locked, mounting it readonly");
            setReadOnly(true);
        }
        if (volumeHeader.isAttribute(SuperBlock.HFSPLUS_VOL_JOURNALED_BIT)) {
            log
                    .info(getDevice().getId() +
                            " Filesystem is journaled, write access is not supported. Mounting it readonly");
            setReadOnly(true);
        }
        try {
            catalog = new Catalog(this);
        } catch (IOException e) {
            throw new FileSystemException(e);
        }
    }

    @Override
    protected final FSDirectory createDirectory(final FSEntry entry) throws IOException {
        return entry.getDirectory();
    }

    @Override
    protected final FSFile createFile(final FSEntry entry) throws IOException {
        return entry.getFile();
    }

    @Override
    protected final HfsPlusEntry createRootEntry() throws IOException {
        log.info("Create root entry.");
        LeafRecord record = catalog.getRecord(CatalogNodeId.HFSPLUS_POR_CNID);
        if (record != null) {
            return new HfsPlusEntry(this, null, "/", record);
        }
        log.error("Root entry : No record found.");
        return null;
    }

    public final long getFreeSpace() {
        return volumeHeader.getFreeBlocks() * volumeHeader.getBlockSize();
    }

    public final long getTotalSpace() {
        return volumeHeader.getTotalBlocks() * volumeHeader.getBlockSize();
    }

    public final long getUsableSpace() {
        return -1;
    }

    public final Catalog getCatalog() {
        return catalog;
    }

    public final SuperBlock getVolumeHeader() {
        return volumeHeader;
    }

    /**
     * Create a new HFS+ file system.
     * 
     * @param params creation parameters
     * 
     * @throws FileSystemException
     */
    public void create(HFSPlusParams params) throws FileSystemException {
        volumeHeader = new SuperBlock(this, true);
        try {
            params.initializeDefaultsValues(this);
            volumeHeader.create(params);
            log.debug("Volume header : \n" + volumeHeader.toString());
            long volumeBlockUsed =
                    volumeHeader.getTotalBlocks() - volumeHeader.getFreeBlocks() -
                            ((volumeHeader.getBlockSize() == 512) ? 2 : 1);
            // ---
            log.debug("Write allocation bitmap bits to disk.");
            writeAllocationFile((int) volumeBlockUsed);
            log.debug("Write Catalog to disk.");
            Catalog catalog = new Catalog(params, this);
            catalog.update();
            log.debug("Write volume header to disk.");
            volumeHeader.update();
            flush();
        } catch (IOException e) {
            throw new FileSystemException("Unable to create HFS+ filesystem", e);
        }
    }

    private void writeAllocationFile(int blockUsed) {
        @SuppressWarnings("unused")
        int bytes = blockUsed >> 3;
        @SuppressWarnings("unused")
        int bits = blockUsed & 0x0007;
        // FIXME ... this should be completed
    }
}
