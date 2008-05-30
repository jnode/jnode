package org.jnode.fs.hfsplus;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.jnode.fs.hfsplus.catalog.CatalogFile;
import org.jnode.fs.hfsplus.extent.ExtentDescriptor;
import org.jnode.fs.hfsplus.tree.LeafRecord;
import org.jnode.fs.spi.AbstractFSFile;

public class HFSPlusFile extends AbstractFSFile {

	private LeafRecord record;
	private CatalogFile file;
	
	public HFSPlusFile(final HFSPlusEntry e){
		super((HfsPlusFileSystem)e.getFileSystem());
		this.record = e.getRecord();
		this.file = new CatalogFile(record.getRecordData());
	}
	
	@Override
	public void flush() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public final long getLength() {
		return file.getDataFork().getTotalSize();
	}

	@Override
	public final void read(final long fileOffset, final ByteBuffer dest) throws IOException {
		HfsPlusFileSystem fs = (HfsPlusFileSystem)getFileSystem();
		for(ExtentDescriptor d: file.getDataFork().getExtents()){
			if(d.getStartBlock() != 0 && d.getBlockCount() != 0){
				long firstOffset = d.getStartBlock()*fs.getVolumeHeader().getBlockSize();
				fs.getApi().read(firstOffset, dest);
			}
		}
	}

	@Override
	public void write(final long fileOffset, final ByteBuffer src) throws IOException {
		// TODO Auto-generated method stub

	}

	public void setLength(final long length) throws IOException {
		// TODO Auto-generated method stub

	}

}
