package org.jnode.fs.hfsplus;

import java.io.IOException;

import org.jnode.fs.FSEntry;
import org.jnode.fs.hfsplus.tree.LeafNode;
import org.jnode.fs.spi.AbstractFSDirectory;
import org.jnode.fs.spi.FSEntryTable;

public class HFSPlusDirectory extends AbstractFSDirectory {

	private LeafNode record;
	
	public HFSPlusDirectory(HfsPlusFileSystem fs, LeafNode record){
		super(fs);
		this.record = record;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void writeEntries(FSEntryTable entries) throws IOException {
		// TODO Auto-generated method stub

	}

}
