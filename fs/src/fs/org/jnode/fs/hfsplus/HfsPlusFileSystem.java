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

	public HfsPlusFileSystem(Device device, boolean readOnly) throws FileSystemException {
		super(device, readOnly);
	}

	public void read() throws FileSystemException {
		sb = new Superblock(this);
		log.debug("Superblock informations :\n" + sb.toString());
	}

	@Override
	protected FSDirectory createDirectory(FSEntry entry) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected FSFile createFile(FSEntry entry) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected HFSPlusEntry createRootEntry() throws IOException {
		Catalog cf = new Catalog(sb, this);
		int currentOffset = cf.init();
		LeafRecord record = cf.getRecord(CatalogNodeId.HFSPLUS_POR_CNID, currentOffset);
		if(record != null) {
			record.toString();
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
}
