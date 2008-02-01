package org.jnode.fs.hfsplus;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.jnode.fs.hfsplus.tree.LeafRecord;
import org.jnode.fs.spi.AbstractFSFile;

public class HFSPlusFile extends AbstractFSFile {

	private LeafRecord record;
	
	public HFSPlusFile(HFSPlusEntry e){
		super((HfsPlusFileSystem)e.getFileSystem());
		this.record = e.getRecord();
	}
	
	@Override
	public void flush() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public long getLength() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void read(long fileOffset, ByteBuffer dest) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void write(long fileOffset, ByteBuffer src) throws IOException {
		// TODO Auto-generated method stub

	}

	public void setLength(long length) throws IOException {
		// TODO Auto-generated method stub

	}

}
