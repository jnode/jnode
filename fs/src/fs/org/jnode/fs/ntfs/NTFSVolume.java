/*
 * $Id$
 */
package org.jnode.fs.ntfs;

import java.io.IOException;
import java.util.Iterator;

import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.fs.ntfs.attributes.NTFSAttribute;
import org.jnode.fs.ntfs.attributes.NTFSBitMapAttribute;
import org.jnode.fs.ntfs.attributes.NTFSDataAttribute;
import org.jnode.fs.ntfs.attributes.NTFSFileNameAttribute;
import org.jnode.fs.ntfs.attributes.NTFSIndexAllocationAttribute;
import org.jnode.fs.ntfs.attributes.NTFSIndexRootAttribute;
import org.jnode.fs.ntfs.attributes.NTFSStandardInformationAttribute;
import org.jnode.fs.ntfs.attributes.NTFSUnimplementedAttribute;


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
	 * @param cluster 
	 */
	public byte[] readCluster(int cluster) throws IOException
	{
		byte[] buff = new byte[getClusterSize() ];
		int clusterOffset = cluster * getClusterSize();
		
		api.read(clusterOffset,buff,0,	getClusterSize());
		return buff;
	}
	
	public byte[] readClusters(long firstCluster, long howMany) throws IOException
	{
		byte[] buff = new byte[(int) (getClusterSize() * howMany)];

		long clusterOffset = firstCluster * getClusterSize();
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
				if(fileRecord != null)
					if(fileRecord.getAlocatedSize() <= 0)
						return false;
				return true;
			}

			public Object next() {
				if(fileRecord != null)
					offset += Math.round(fileRecord.getAlocatedSize() / NTFSVolume.this.getClusterSize());
				fileRecord = getFileRecord(offset);
				//System.out.println("File: " + fileRecord.getFileName() +"," + offset );
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
			{
				return fileRecord; 
			}
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
		
		NTFSFileRecord fileRecord = new NTFSFileRecord(this);
		//  check for the magic numberb to see if we have a filerecord
		if(NTFSFileRecord.hasMagicNumber(buf))
		{
			// it is a file record
			
			//fill the NTFSFileRecord header data
			fileRecord.setRealSize(
					NTFSUTIL.READ32_INT( buf, 0x18)
			);
			fileRecord.setAlocatedSize(
					NTFSUTIL.READ32_INT( buf, 0x1C)
			);
			fileRecord.setHardLinkCount(
					NTFSUTIL.READ16_INT(buf[0x12],buf[0x13])
			);
			fileRecord.setUpdateSequenceOffset(
					NTFSUTIL.READ16_INT(buf[0x4],buf[0x5])
			);
			fileRecord.setUpdateSequenceSize(
					NTFSUTIL.READ16_INT(buf[0x6],buf[0x7])
			);
			
			fileRecord.setSequenceNumber(
					NTFSUTIL.READ16_INT(buf[0x10],buf[0x11])
			);
			fileRecord.setFirtAttributeOffset(
					NTFSUTIL.READ16_INT(buf[0x14],buf[0x15])
			);
			fileRecord.setFlags(
					NTFSUTIL.READ16_INT(buf[0x16],buf[0x17])
			);
			fileRecord.setNextAttributeID(
					NTFSUTIL.READ16_INT(buf[0x28],buf[0x29])
			);
			
			// fill attribtes list without data
			int offset = fileRecord.getFirtAttributeOffset();
			
			/*
			 * check the fixup
			 */
			// calculate the Update sequence number
			int usn = NTFSUTIL.READ16_INT(
						buf[fileRecord.getUpdateSequenceOffset()],
						buf[fileRecord.getUpdateSequenceOffset() + 1]);
			// check each sector if the last 2 bytes are equal with the USN from header
			for(int i = 1; i <= this.getBootRecord().SectorPerCluster * this.getBootRecord().ClustersPerMFTRecord;i++)
			{
				int sectusn = NTFSUTIL.READ16_INT(
								buf[(i * this.getBootRecord().BytesPerSector)-2],
								buf[(i * this.getBootRecord().BytesPerSector)-1]
								);
				if(sectusn != usn)
					throw new RuntimeException("ERROR! - USN doesn't mach");
				else
				{
					//copy the USN buffer to the end
					buf[(i * this.getBootRecord().BytesPerSector)-2] = buf[fileRecord.getUpdateSequenceOffset() + (i * 2)]; 
					buf[(i * this.getBootRecord().BytesPerSector)-1] = buf[fileRecord.getUpdateSequenceOffset() + (i * 2) + 1]; 
				}
			}
			
			outer: for(;;)
			{
				if(offset >= fileRecord.getRealSize())
					break;
				NTFSAttribute attribute = null;
				int type = NTFSUTIL.READ32_INT( buf, offset);
				if(type == -1)
					break;
				switch(type)
				{
					case 0x10: 
						attribute = new NTFSStandardInformationAttribute(fileRecord);
						fileRecord.getAttributeMap().put(new Integer(0x10),attribute);
						break; 
					case 0x30: 
						attribute = new NTFSFileNameAttribute(fileRecord);
						fileRecord.getAttributeMap().put(new Integer(0x30),attribute);
						break;
					case 0x80: 
						attribute = new NTFSDataAttribute(fileRecord);
						fileRecord.getAttributeMap().put(new Integer(0x80),attribute);
						break;
					case 0xb0: 
						attribute = new NTFSBitMapAttribute(fileRecord);
						fileRecord.getAttributeMap().put(new Integer(0xb0),attribute);
						break;
					case 0x90: 
						attribute = new NTFSIndexRootAttribute(fileRecord);
						fileRecord.getAttributeMap().put(new Integer(0x90),attribute);
						break;
					case 0xA0: attribute = new NTFSIndexAllocationAttribute(fileRecord);
						fileRecord.getAttributeMap().put(new Integer(0xA0),attribute);
						break;
					default:  
						attribute = new NTFSUnimplementedAttribute(fileRecord);
						fileRecord.getAttributeMap().put(new Integer(0xFF),attribute);
						break;
					}
				attribute.setAttributeType(
						NTFSUTIL.READ32_INT( buf, offset));
				attribute.setLength(
						NTFSUTIL.READ32_INT( buf, offset + 4));
				attribute.setAttributeOffset(
						NTFSUTIL.READ16_INT(buf[offset + 0x14],buf[offset + 0x15]));
				attribute.setResident(
						buf[offset + 0x08]==0);
				attribute.setNameLength(buf[offset + 0x09]);
				attribute.setNameOffset(
						NTFSUTIL.READ16_INT(buf[offset + 0x0A],buf[offset + 0x0B]));
				
				// if it is named fill the name attribute 
				if(attribute.getNameLength() > 0)
				{	
					char[] namebuf = new char[attribute.getNameLength()];
					for(int i = 0;i < attribute.getNameLength();i++)
					{
						namebuf[i] = NTFSUTIL.READ16_CHAR(
								buf[offset + attribute.getNameOffset() + (i*2)],
								buf[offset + attribute.getNameOffset() + (i*2) + 1]);
					}
					attribute.setName( new String(namebuf));
				}		
				if(attribute.isResident())
				{	
					attribute.setAttributeLength(
						NTFSUTIL.READ32_INT( buf, offset + 0x10));
					attribute.processAttributeData(NTFSUTIL.extractSubBuffer(
							buf,
							offset + attribute.getAttributeOffset(),
							attribute.getAttributeLength()));
				}
				else
				{	
					attribute.setStartVCN(
							NTFSUTIL.READ32_INT( buf, offset + 0x10));
					attribute.setLastVCN(
							NTFSUTIL.READ32_INT( buf, offset + 0x18));
					attribute.setDataRunsOffset(
							NTFSUTIL.READ16_INT( buf[offset + 0x20],buf[offset + 0x21]));
					attribute.setAttributeAlocatedSize(
							NTFSUTIL.READ32_INT( buf, offset + 0x28));
					if(attribute.getDataRunsOffset() > 0)
						attribute.processDataRuns(NTFSUTIL.extractSubBuffer(
							buf,
							offset + attribute.getDataRunsOffset(),
							attribute.getLength() - (attribute.getDataRunsOffset())));
				}
				offset+= attribute.getLength();
			}
		}
		return fileRecord;
	}
}
