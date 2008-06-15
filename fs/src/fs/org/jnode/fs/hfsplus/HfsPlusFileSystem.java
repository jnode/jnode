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
    public void create() throws FileSystemException {
        // TODO implements file system creation.
    }

    /**
     * 
     * @throws FileSystemException
     */
    public final void read() throws FileSystemException {
        sb = new Superblock(this);

        log.debug("Superblock informations:\n" + sb.toString());
        if (!sb.isAttribute(HfsPlusConstants.HFSPLUS_VOL_UNMNT_BIT)) {
            log.info(getDevice().getId() +
                    " Filesystem has not been cleanly unmounted, mounting it readonly");
            setReadOnly(true);
        }
        if (sb.isAttribute(HfsPlusConstants.HFSPLUS_VOL_SOFTLOCK_BIT)) {
            log.info(getDevice().getId() + " Filesystem is marked locked, mounting it readonly");
            setReadOnly(true);
        }
        if (sb.isAttribute(HfsPlusConstants.HFSPLUS_VOL_JOURNALED_BIT)) {
            log.info(getDevice().getId() +
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
        HFSPlusEntry e = (HFSPlusEntry) entry;
        return new HFSPlusDirectory(e);
    }

    @Override
    protected final FSFile createFile(final FSEntry entry) throws IOException {
        HFSPlusEntry e = (HFSPlusEntry) entry;
        return new HFSPlusFile(e);
    }

    @Override
    protected final HFSPlusEntry createRootEntry() throws IOException {
        LeafRecord record = catalog.getRecord(CatalogNodeId.HFSPLUS_POR_CNID);
        if (record != null) {
            return new HFSPlusEntry(this, null, null, "/", record);
        }
        log.debug("Root entry : No record found.");
        return null;
    }

    public final long getFreeSpace() {
        return sb.getFreeBlocks() * sb.getBlockSize();
    }

    public final long getTotalSpace() {
        return sb.getTotalBlocks() * sb.getBlockSize();
    }

    public final long getUsableSpace() {
        // TODO Auto-generated method stub
        return -1;
    }

    public final Catalog getCatalog() {
        return catalog;
    }

    public final Superblock getVolumeHeader() {
        return sb;
    }
}
