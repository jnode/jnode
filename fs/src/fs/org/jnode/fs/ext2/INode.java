/*
 * $Id$
 */
package org.jnode.fs.ext2;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.jnode.util.NumberUtils;

/**
 * @author Andras Nagy
 */
public class INode {
	
	public static final int INODE_LENGTH = 128;
	private static final Logger log = Logger.getLogger(Ext2DirectoryRecord.class);
	
	//the data constituting the inode itself
	private byte[] data;
	
	private Ext2FileSystem fs;
	
	public INode(Ext2FileSystem fs, byte[] data) {
		this.fs = fs;
		this.data = new byte[INODE_LENGTH];
		System.arraycopy(data, 0, this.data, 0, INODE_LENGTH);
	}
		
	public Ext2FileSystem getExt2FileSystem() {
		return fs;
	}
	
	public int getMode() {
		int iMode=Ext2Utils.get16(data, 0);
		log.debug("INode.getIMode(): "+NumberUtils.hex(iMode));
		return iMode;
	}
	
	public int getUid() {
		return Ext2Utils.get16(data, 2);		
	}
	
	public long getSize() {
		return Ext2Utils.get32(data, 4);	
	}
	
	public long getAtime() {
		return Ext2Utils.get32(data, 8);
	}
	
	public long getCtime() {
		return Ext2Utils.get32(data, 12);
	}
		
	public long getMtime() {
		return Ext2Utils.get32(data, 16);
	}
		
	public long getDtime() {
		return Ext2Utils.get32(data, 20);
	}
		
	public int getGid() {
		return Ext2Utils.get16(data, 24);
	}
		
	public int getLinksCount() {
		return Ext2Utils.get16(data, 26);
	}
	
	/**
	 * Return the size in 512-byte blocks.
	 */
	public long getBlocks() {
		return Ext2Utils.get32(data, 28);
	}

	public long getFlags() {
		return Ext2Utils.get32(data, 32);
	}
	
	public long getOSD1() {
		return Ext2Utils.get32(data, 36);
	}
	
	public byte[] getDataBlock(long i) throws IOException {
		//get the direct blocks (0; 11)
		if(i<12)
			return fs.getBlock( Ext2Utils.get32(data,40+(int)i*4) );

		//see the indirect blocks (12; indirectCount-1)
		int indirectCount = fs.getSuperblock().getBlockSize() << 2; //a block index is 4 bytes long
		if(i<12+indirectCount) {
			long offset=i-12;
			//the 12th index points to the indirect block
			byte[] indirectBlock = fs.getBlock( Ext2Utils.get32(data,40+12*4) );
			long blockIndex = Ext2Utils.get32(indirectBlock, (int)offset*4);
			return fs.getBlock( blockIndex );
		}
		
		//see the double indirect blocks (indirectCount; doubleIndirectCount-1)
		int doubleIndirectCount = indirectCount * indirectCount;
		if(i<12+indirectCount+doubleIndirectCount) {
			//the 13th index points to the double indirect block
			byte[] doubleIndirectBlock = fs.getBlock( Ext2Utils.get32(data,40+13*4) );
			long offset = i-indirectCount-12;
			long indirectBlockNr = offset / indirectCount;
			long indirectBlockOffset= offset % indirectCount;
			
			byte[] indirectBlock = fs.getBlock( Ext2Utils.get32(doubleIndirectBlock, (int)indirectBlockNr) );
			long blockIndex = Ext2Utils.get32(indirectBlock, (int)indirectBlockOffset*4);

			return fs.getBlock( blockIndex );
		}
		
		//see the triple indirect blocks (doubleIndirectCount; tripleIndirectCount-1)
		int tripleIndirectCount = indirectCount * indirectCount * indirectCount;
		if(i<12+indirectCount+doubleIndirectCount+tripleIndirectCount) {
			//the 14th index points to the triple indirect block
			byte[] tripleIndirectBlock = fs.getBlock( Ext2Utils.get32(data,40+14*4) );
			long offset = i-doubleIndirectCount-indirectCount-12;
			long doubleIndirectBlockNr = offset / (indirectCount * indirectCount);
			long doubleIndirectBlockOffset = offset % (indirectCount * indirectCount);
			
			byte[] doubleIndirectBlock = fs.getBlock( Ext2Utils.get32(tripleIndirectBlock, (int)doubleIndirectBlockNr) );
			long indirectBlockIndex = Ext2Utils.get32( doubleIndirectBlock, (int)doubleIndirectBlockOffset*4);
			long indirectBlockOffset = offset % indirectCount;
			
			byte[] indirectBlock = fs.getBlock( Ext2Utils.get32(doubleIndirectBlock, (int)indirectBlockIndex*4) );
			long blockIndex = Ext2Utils.get32( indirectBlock, (int)indirectBlockOffset*4 );
			
			return fs.getBlock( blockIndex );
		}
		else{
			throw new IOException("file too big: more than "+12+indirectCount+doubleIndirectCount+tripleIndirectCount+" blocks");
		}	
	}
	
	public long getGeneration() {
		return Ext2Utils.get32(data, 100);	
	}

	public long getFileACL() {
		return Ext2Utils.get32(data, 104);	
	}

	public long getDirACL() {
		return Ext2Utils.get32(data, 108);	
	}

	public long getFAddr() {
		return Ext2Utils.get32(data, 112);	
	}

	//TODO: return OSD2 fields (12 bytes from offset 116)
}
