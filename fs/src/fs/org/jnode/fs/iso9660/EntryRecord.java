/*
 * $Id$
 */
package org.jnode.fs.iso9660;

import java.io.IOException;

import org.jnode.fs.ntfs.NTFSUTIL;


/**
 * @author Chira
 */
public class EntryRecord
{
	private byte[] buff = null;
	private ISO9660Volume volume = null; 
	
	public EntryRecord(byte[] buff)
	{
		this.buff = buff;
	}
	public EntryRecord(ISO9660Volume volume, byte[] buff, int offset)
	{
		this.volume = volume;
		this.buff = new byte[Util.unsignedByteToInt(buff[offset])]; 
		System.arraycopy(buff,offset,this.buff,0,Util.unsignedByteToInt(buff[offset]));
	}
	public void readFileData(long offset,byte[] buffer,int bufferOffset, int size) throws IOException
	{
		volume.readFromLBN(this.getLocationOfExtent(),offset,buffer,bufferOffset,size);
	}
	public byte[] getExtentData() throws IOException
	{
		byte[] buffer = new byte[this.getDataLength()];
		volume.readFromLBN(this.getLocationOfExtent(),0,buffer,0,this.getDataLength());
		return buffer;
	}
	public int getLengthOfDirectoryEntry()
	{
		return Util.unsignedByteToInt(buff[0]);
	}
	public int getLengthOfExtendedAttribute()
	{
		return buff[1];
	}
	public int getLocationOfExtent()
	{
		return NTFSUTIL.LE_READ_U32_INT(buff,2);
	}
	public int getDataLength()
	{
		return NTFSUTIL.LE_READ_U32_INT(buff,10);
	}
	public boolean isDirectory()
	{
		return (buff[25] & 0x03) != 0;
	}
	public boolean isLastEntry()
	{
		return (buff[25] & 0x40) == 0;
	}
	public int getFlags()
	{
		return buff[25];
	}
	public int getLengthOfFileIdentifier()
	{
		return buff[32];
	}
	public int getFileUnitSize()
	{
		return buff[26];
	}
	public int getInterleaveSize()
	{
		return buff[27];
	}
	public String getFileIdentifier()
	{
		if(this.isDirectory())
		{
			if(this.getLengthOfFileIdentifier() == 1 && buff[33]== 0x00)
				return ".";
			if(this.getLengthOfFileIdentifier() == 1 && buff[33]== 0x01)
				return "..";
			return new String(buff, 33 , this.getLengthOfFileIdentifier());
		}
		return new String(buff, 33 , this.getLengthOfFileIdentifier() - 2);

	}
	/**
	 * @return Returns the volume.
	 */
	public ISO9660Volume getVolume() {
		return volume;
	}
	/**
	 * @param volume The volume to set.
	 */
	public void setVolume(ISO9660Volume volume) {
		this.volume = volume;
	}
}
