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

	private Superblock sb;

	private Catalog catalog;

	public HfsPlusFileSystem(Device device, boolean readOnly) throws FileSystemException {
		super(device, readOnly);
	}

	final public HfsPlusFileSystemType getType() {
		return HfsPlusFileSystemType.getInstance();
	}

	public void read() throws FileSystemException {
		sb = new Superblock(this);
		log.debug("Superblock informations :\n" + sb.toString());
	}

	@Override
	protected FSDirectory createDirectory(FSEntry entry) throws IOException {
		HFSPlusEntry e = (HFSPlusEntry)entry;
		return new HFSPlusDirectory(e);
	}

	@Override
	protected FSFile createFile(FSEntry entry) throws IOException {
		HFSPlusEntry e = (HFSPlusEntry)entry;
		return new HFSPlusFile(e);
	}

	@Override
	protected HFSPlusEntry createRootEntry() throws IOException {
		catalog = new Catalog(this);
		LeafRecord record = catalog.getRecord(CatalogNodeId.HFSPLUS_POR_CNID);
		if(record != null) {
			return new HFSPlusEntry(this,null,null,"/",record);
		}
		log.debug("Root entry : No record found.");
		return null;
	}

	public long getFreeSpace() {
		return sb.getFreeBlocks() * sb.getBlockSize();
	}

	public long getTotalSpace() {
		return sb.getTotalBlocks() * sb.getBlockSize();
	}

	public long getUsableSpace() {
		// TODO Auto-generated method stub
		return 0;
	}

	public Catalog getCatalog() {
		return catalog;
	}

	public Superblock getSb() {
		return sb;
	}
}
