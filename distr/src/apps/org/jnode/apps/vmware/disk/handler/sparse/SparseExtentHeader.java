package org.jnode.apps.vmware.disk.handler.sparse;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.apache.log4j.Logger;
import org.jnode.apps.vmware.disk.handler.UnsupportedFormatException;

/**
 * Wrote from the 'Virtual Disk Format 1.0' specifications (from VMWare)
 * 
 * @author Fabien DUMINY (fduminy at jnode dot org)
 *
 */
public class SparseExtentHeader 
{
	private static final Logger LOG = Logger.getLogger(SparseExtentFactory.class);
		
	private static final int MAGIC_NUMBER = 0x564d444b; // 'V','M', 'D', 'K' 	
	private static final int VERSION = 1;
	
	// flags
	private boolean validNewLineDetectionTest;
	private boolean redundantGrainTableWillBeUsed;
	
	private long capacity;
	private long grainSize;
	private long descriptorOffset;
	private long descriptorSize;
	private long rgdOffset;
	
	private static final int numGTEsPerGT = 512;
	
	private long gdOffset;
	private long overHead;
	private boolean uncleanShutdown;

	private static final byte singleEndLineChar = '\n';
	private static final byte nonEndLineChar = ' ';
	private static final byte doubleEndLineChar1 = '\r';
	private static final byte doubleEndLineChar2 = '\n';
	
	private static final int PAD_SIZE = 435;
	
	public static boolean readMagicNumber(ByteBuffer bb) throws IOException
	{
		int magicNum = bb.getInt();
		LOG.debug("magicNum="+Long.toHexString(magicNum));
		return (magicNum == MAGIC_NUMBER);		
	}
	
	public static SparseExtentHeader read(ByteBuffer bb) 
				throws IOException, UnsupportedFormatException 
	{
		SparseExtentHeader header = new SparseExtentHeader();
		
		if(!readMagicNumber(bb))
		{
			throw new UnsupportedFormatException("not the magic number");
		}
		
		int version = bb.getInt();
		if(version != VERSION)
		{
			throw new IOException("bad version number (found:" + version + ")");
		}		
		
		int flags = bb.getInt();
		header.validNewLineDetectionTest = ((flags & 0x01) == 0x01); // bit 0  
		header.redundantGrainTableWillBeUsed = ((flags & 0x02) == 0x02); // bit 1
		
		header.capacity = bb.getLong();
		header.grainSize = bb.getLong();
		header.descriptorOffset = bb.getLong();
		header.descriptorSize = bb.getLong();
		
		int nb = bb.getInt(); 
		if(nb != numGTEsPerGT)
		{
			throw new IOException("bad number of entries per grain table (found:" + nb + ")");
		}		
		
		header.rgdOffset = bb.getLong();
		header.gdOffset = bb.getLong();
		header.overHead = bb.getLong();
		header.uncleanShutdown = (bb.get() != 0);
 
		if(bb.get() != singleEndLineChar)
		{
			throw new IOException("file corrupted after a FTP");
		}		
		if(bb.get() != nonEndLineChar)
		{
			throw new IOException("file corrupted after a FTP");
		}		
		if(bb.get() != doubleEndLineChar1)
		{
			throw new IOException("file corrupted after a FTP");
		}		
		if(bb.get() != doubleEndLineChar2)
		{
			throw new IOException("file corrupted after a FTP");
		}		
		
		if(bb.remaining() < PAD_SIZE)
		{
			throw new UnsupportedFormatException("bad pad size");
		}
		
		return header;
	}
	
	public void write(RandomAccessFile raf) throws IOException
	{		
		raf.seek(0L);
		raf.writeInt(MAGIC_NUMBER);
		raf.writeInt(VERSION);
		
		int flags = 0;
		if(validNewLineDetectionTest)
		{
			flags &= 0x01; // bit 0
		}
		if(redundantGrainTableWillBeUsed)
		{
			flags &= 0x02; // bit 1
		}		
		raf.writeInt(flags);
		
		raf.writeLong(capacity);
		raf.writeLong(grainSize);
		raf.writeLong(descriptorOffset);
		raf.writeLong(descriptorSize);
		raf.writeInt(numGTEsPerGT);
		raf.writeLong(rgdOffset);
		raf.writeLong(gdOffset);
		raf.writeLong(overHead);
		raf.writeBoolean(uncleanShutdown);
		raf.writeByte(singleEndLineChar);
		raf.writeByte(nonEndLineChar);
		raf.writeByte(doubleEndLineChar1);
		raf.writeByte(doubleEndLineChar2);
		
		for(int i = 0 ; i < PAD_SIZE ; i++)
		{
			raf.writeByte(0);
		}
	}

	public int getNumGTEsPerGT() {
		return numGTEsPerGT;
	}

	public long getCapacity() {
		return capacity;
	}

	public long getGrainSize() {
		return grainSize;
	}

	public long getDescriptorOffset() 
	{
		return descriptorOffset;
	}
}
