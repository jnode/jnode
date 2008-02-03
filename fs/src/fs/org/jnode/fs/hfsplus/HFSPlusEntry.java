package org.jnode.fs.hfsplus;

import org.jnode.fs.hfsplus.tree.LeafRecord;
import org.jnode.fs.spi.AbstractFSEntry;
import org.jnode.fs.spi.FSEntryTable;

public class HFSPlusEntry extends AbstractFSEntry{

	private LeafRecord record;
	
	public HFSPlusEntry(HfsPlusFileSystem fs, FSEntryTable table,
			HFSPlusDirectory parent, String name, LeafRecord record) {
		super(fs, table, parent, name, getFSEntryType(name, record));
		this.record = record;
	}

	static private int getFSEntryType(String name, LeafRecord record) {
		int mode = record.getType();
		if("/".equals(name))
			return AbstractFSEntry.ROOT_ENTRY;
		else if(mode == HfsPlusConstants.RECORD_TYPE_FOLDER)
			return AbstractFSEntry.DIR_ENTRY;
		else if(mode == HfsPlusConstants.RECORD_TYPE_FILE)
			return AbstractFSEntry.FILE_ENTRY;
		else
			return AbstractFSEntry.OTHER_ENTRY;
	}

	public LeafRecord getRecord() {
		return record;
	}
}
