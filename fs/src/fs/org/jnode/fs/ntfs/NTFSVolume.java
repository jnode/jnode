/*
 * $Id$
 */
package org.jnode.fs.ntfs;

import java.io.*;
import java.util.*;

import org.jnode.driver.DeviceAPI;
import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.fs.ntfs.attributes.*;

/**
 * @author Chira
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class NTFSVolume
{
	private NTFSBootRecord bootRecord = null;
	private BlockDeviceAPI api = null;
	private NTFS_MFTRecord MTFRecord = new NTFS_MFTRecord();
	/**
	 * 
	 */
	public NTFSVolume(BlockDeviceAPI api) throws IOException
	{
		super();
		if(bootRecord == null)
			bootRecord = new NTFSBootRecord();
		byte[] buffer = new byte[512];
		api.read(0,buffer,0,512);
		bootRecord.initBootRecordData(buffer);
		this.api = api;
	}
	/**
	 * @return Returns the bootRecord.
	 */
	public NTFSBootRecord getBootRecord()
	{
		return bootRecord;
	}
	/**
	 * @param bootRecord The bootRecord to set.
	 */
	public byte[] readCluster(int cluster) throws IOException
	{
		byte[] buff = new byte[getClusterSize() ];
		int clusterOffset = cluster * getClusterSize();
		
		api.read(clusterOffset,buff,0,	getClusterSize());
		return buff;
	}
	
	public byte[] readClusters(int firstCluster, int howMany) throws IOException
	{
		byte[] buff = new byte[getClusterSize() * howMany];

		int clusterOffset = firstCluster * getClusterSize();
		for(int i = 0 ; i< howMany;i++)
		{	
			api.read(clusterOffset + (i * getClusterSize()),buff,i * getClusterSize(),	getClusterSize());
		}
		return buff;
	}
	
	public int getClusterSize()
	{
		return this.getBootRecord().SectorPerCluster * this.getBootRecord().BytesPerSector;
	}
	public void setBootRecord(NTFSBootRecord bootRecord)
	{
		this.bootRecord = bootRecord;
	}
	/**
	 * @return Returns the mTFRecord.
	 */
	public NTFS_MFTRecord getMTFRecord()
	{
		return MTFRecord;
	}
	/**
	 * @param record The mTFRecord to set.
	 */
	public void setMTFRecord(NTFS_MFTRecord record)
	{
		MTFRecord = record;
	}
	public NTFSFileRecord getFirstFileRecord()
	{
		return getFileRecord(0);
	}
	public Iterator getNTFSIterator()
	{
		return new Iterator()
		{

			int offset = 0;
			NTFSFileRecord fileRecord = null;
			public boolean hasNext() {
				return true;
			}

			public Object next() {
				if(fileRecord != null)
					offset += Math.round(fileRecord.getAlocatedSize() / NTFSVolume.this.getClusterSize());
				fileRecord = getFileRecord(offset);
				return fileRecord;
			}

			public void remove() {
				throw new UnsupportedOperationException("not yet implemented");
			}
		
		};
	}
	public NTFSFileRecord getRootDirectory()
	{
		NTFSFileRecord  fileRecord = null;
		for(Iterator itr = this.getNTFSIterator();itr.hasNext();)
		{
			fileRecord = (NTFSFileRecord) itr.next();	
			if(fileRecord.getFileName().equals("."))
				return fileRecord; 
		}	
		return null;
	}
	public NTFSFileRecord getFileRecord(int MFTClusterOffset)
	{
		byte[] buf;
		try
		{
			buf = this.readClusters(this.getBootRecord().MFTPointer + MFTClusterOffset,getBootRecord().ClustersPerMFTRecord);
		} catch (IOException e)
		{
			return null;
		}
		
		NTFSFileRecord fileRecord = new NTFSFileRecord();
		//  check for the magic numberb to see if we have a filerecord
		if(NTFSFileRecord.hasMagicNumber(buf))
		{
			// it is a file record
			
			//fill the NTFSFileRecord header data
			fileRecord.setRealSize(
					NTFSUTIL.ARRAY2INT( buf, 0x18)
			);
			fileRecord.setAlocatedSize(
					NTFSUTIL.ARRAY2INT( buf, 0x1C)
			);
			fileRecord.setHardLinkCount(
					NTFSUTIL.makeWORDfrom2Bytes(buf[0x12],buf[0x13])
			);
			fileRecord.setUpdateSequenceOffset(
					NTFSUTIL.makeWORDfrom2Bytes(buf[0x4],buf[0x5])
			);
			fileRecord.setSequenceNumber(
					NTFSUTIL.makeWORDfrom2Bytes(buf[0x10],buf[0x11])
			);
			fileRecord.setFirtAttributeOffset(
					NTFSUTIL.makeWORDfrom2Bytes(buf[0x14],buf[0x15])
			);
			fileRecord.setFlags(
					NTFSUTIL.makeWORDfrom2Bytes(buf[0x16],buf[0x17])
			);
			fileRecord.setNextAttributeID(
					NTFSUTIL.makeWORDfrom2Bytes(buf[0x28],buf[0x29])
			);
			
			// fill attribtes list without data
			int offset = fileRecord.getFirtAttributeOffset();
			
			outer: for(;;)
			{
				if(offset >= fileRecord.getRealSize())
					break;
				NTFSAttribute attribute = null;
				int type = NTFSUTIL.ARRAY2INT( buf, offset);
				if(type == -1)
					break;
				switch(type)
				{
					case 0x10: attribute = new NTFSStandardInformationAttribute();
						break; 
					case 0x30: attribute = new NTFSFileNameAttribute();
						break;
					case 0x80: attribute = new NTFSDataAttribute();
						break;
					case 0xb0: attribute = new NTFSBitMapAttribute();
						break;
					default:  attribute = new NTFSUnimplementedAttribute();
						break;
					}
				attribute.setAttributeType(
						NTFSUTIL.ARRAY2INT( buf, offset));
				attribute.setLength(
						NTFSUTIL.ARRAY2INT( buf, offset + 4));
				attribute.setAttributeLength(
						NTFSUTIL.ARRAY2INT( buf, offset + 0x10));
				attribute.setAttributeOffset(
						NTFSUTIL.makeWORDfrom2Bytes(buf[offset + 0x14],buf[offset + 0x15]));
				attribute.processAttributeData(
						NTFSUTIL.extractSubBuffer(
									buf,
									offset + attribute.getAttributeOffset(),
									attribute.getAttributeLength()));
				fileRecord.getAttributeList().add(attribute);
				offset+= attribute.getLength();
			}
		}
		return fileRecord;
	}
	public void printFileRecords()	
	{
		if(this.getBootRecord() == null)
			return;
		
			// printout data
		System.out.println("------------ File record data -------------");
		/*System.out.println("Real size of the file record: " + fileRecord.getRealSize());
			System.out.println("Disk alocated size of the file record: " + fileRecord.getAlocatedSize());
			System.out.println("Hard link count: " + fileRecord.getHardLinkCount());
			System.out.println("Next Attr. ID: " + fileRecord.getNextAttributeID());
			if(fileRecord.getFlags() == 0x02)
				System.out.println("This is a directory!" );
			else
				if(fileRecord.getFlags() == 0x01)
					System.out.println("Record in use!" );
			else
				System.out.println("Flags: " + fileRecord.getFlags());
			System.out.println("Offset Attr.: 0x" + Integer.toHexString(fileRecord.getFirtAttributeOffset()));
			*/
		for(Iterator itr = this.getNTFSIterator();itr.hasNext();)
		{
			NTFSFileRecord fileRecord = (NTFSFileRecord) itr.next();	
			
			for(Iterator it = fileRecord.getAttributeList().iterator();it.hasNext();)
			{
				NTFSAttribute attribute = (NTFSAttribute) it.next();
				if( attribute instanceof NTFSFileNameAttribute)
				{	
					System.out.println(((NTFSFileNameAttribute)attribute).getFileName());
				}
				/*switch (attribute.getAttributeType())
				{
					case 0x10 : System.out.println("Attr. type: $STANDARD_INFORMATION");
							break;
					case 0x20 : System.out.println("Attr. type: $ATTRIBUTE_LIST"); 
							break;
					case 0x30 : System.out.println("Attr. type: $FILE_NAME"); 
							break;
					case 0x40 : System.out.println("Attr. type: $OBJECT_ID"); 
							break;
					case 0x80 : System.out.println("Attr. type: $DATA"); 
						break;
					case 0xb0 : System.out.println("Attr. type: $BITMAP");
						break;
					default :	System.out.println("Attr. type: NOT KNOWN( 0x" + Integer.toHexString(attribute.getAttributeType()) + ")" );
				
				}*/
			}
		}		
	}
}
