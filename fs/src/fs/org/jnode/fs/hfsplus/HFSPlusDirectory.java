package org.jnode.fs.hfsplus;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.jnode.fs.FSEntry;
import org.jnode.fs.hfsplus.catalog.CatalogFolder;
import org.jnode.fs.hfsplus.tree.LeafRecord;
import org.jnode.fs.spi.AbstractFSDirectory;
import org.jnode.fs.spi.FSEntryTable;

public class HFSPlusDirectory extends AbstractFSDirectory {
	
	private final Logger log = Logger.getLogger(getClass());

	private LeafRecord record;
	
	private CatalogFolder folder;
	
	public HFSPlusDirectory(HFSPlusEntry e){
		super((HfsPlusFileSystem)e.getFileSystem());
		this.record = e.getRecord();
		this.folder = new CatalogFolder(record.getRecordData());
		log.debug("Folder : " + folder.toString());
	}
	
	@Override
	protected FSEntry createDirectoryEntry(String name) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected FSEntry createFileEntry(String name) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected FSEntryTable readEntries() throws IOException {
		return FSEntryTable.EMPTY_TABLE;
	}

	@Override
	protected void writeEntries(FSEntryTable entries) throws IOException {
		// TODO Auto-generated method stub

	}

}
