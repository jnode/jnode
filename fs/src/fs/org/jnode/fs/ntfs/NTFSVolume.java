/*
 * $Id$
 */
package org.jnode.fs.ntfs;

import java.io.IOException;

import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.fs.ntfs.attributes.NTFSIndexEntry;
import org.jnode.fs.ntfs.attributes.NTFSNonResidentAttribute;


/**
 * @author Chira
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class NTFSVolume
{

	public static byte LONG_FILE_NAMES = 0x01;
	public static byte DOS_8_3 = 0x02;
	
	private byte currentNameSpace = LONG_FILE_NAMES;
	
	private NTFSBootRecord bootRecord = null;
	private BlockDeviceAPI api = null;
	/**
	 * 
	 */
	public NTFSVolume(BlockDeviceAPI api) throws IOException
	{
		super();
		if(bootRecord == null)
			bootRecord = new NTFSBootRecord();
		// I hope this is enaugh..should be
		byte[] buffer = new byte[512];
		this.api = api;
		
		api.read(0,buffer,0,512);
		bootRecord.initBootRecordData(buffer);
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
	public byte[] readCluster(long cluster) throws IOException
	{
		byte[] buff = new byte[getClusterSize() ];
		long clusterOffset = cluster * getClusterSize();
		
		api.read(clusterOffset,buff,0,	getClusterSize());
		return buff;
	}
	
	public byte[] readDataFromCluster(long cluster, int howManyBytes, int offset) throws IOException
	{
		return NTFSUTIL.extractSubBuffer(readCluster(cluster),offset,howManyBytes);
	}
	
	public byte[] readClusters(long firstCluster, long howMany) throws IOException
	{
		byte[] buff = new byte[(int) (howMany * getClusterSize())];

		long clusterOffset = firstCluster * getClusterSize();
		for(int i = 0 ; i< howMany;i++)
		{	
			api.read(clusterOffset + (i * getClusterSize()),buff,i * getClusterSize(),	getClusterSize());
		}
		return buff;
	}
	
	public int getClusterSize()
	{
		return this.getBootRecord().getSectorPerCluster() * this.getBootRecord().getBytesPerSector();
	}
	public void setBootRecord(NTFSBootRecord bootRecord)
	{
		this.bootRecord = bootRecord;
	}
	/**
	 * @return Returns the mTFRecord.
	 */
	public NTFSFileRecord getMFTRecord() throws IOException
	{
		if(this.getBootRecord().getBytesPerFileRecord() < this.getClusterSize())
		{
			return new NTFSFileRecord(
					this,
					this.readDataFromCluster(
							this.getBootRecord().getMFTPointer(),
							this.getBootRecord().getBytesPerFileRecord(),
							0
					));
					
		}
		return new NTFSFileRecord(
					this,
					this.readClusters(
							this.getBootRecord().getMFTPointer(),
							this.getBootRecord().getBytesPerFileRecord() / this.getClusterSize()
					)
		);
		
	}
	
	public NTFSFileRecord getIndexedFileRecord(NTFSIndexEntry indexEntry) throws IOException
	{
		// read the MFTdatathis
		NTFSNonResidentAttribute dataAttribute = (NTFSNonResidentAttribute) this.getMFTRecord().getAttribute(NTFSFileRecord.$DATA);
		// find out the VCN
		
		int offset = this.getBootRecord().getBytesPerFileRecord() * (indexEntry.getFileReferenceNumber());
		
		// read the buffer
		byte [] buffer = null;
		
		buffer = dataAttribute.readVCN(
				offset / this.getClusterSize(),
				this.getBootRecord().getBytesPerFileRecord() < this.getClusterSize() ? 1 :  this.getBootRecord().getBytesPerFileRecord() / this.getClusterSize() 
		);
		return 
			new NTFSFileRecord(
					this,
					NTFSUTIL.extractSubBuffer(
								buffer,
								offset % this.getClusterSize(),
								this.getBootRecord().getBytesPerFileRecord())
			); 
		
	}
	public NTFSFileRecord getRootDirectory() throws IOException
	{
		// first find the filerecord for root 
		NTFSNonResidentAttribute dataAttribute = ((NTFSNonResidentAttribute)getMFTRecord().getAttribute(NTFSFileRecord.$DATA));
		
		int bytesPerFileRecord = this.getBootRecord().getBytesPerFileRecord();
		int clusterSize = this.getClusterSize();
		
		int howmanyToRead = 1;
		
		if( bytesPerFileRecord > clusterSize)
		{
			howmanyToRead = bytesPerFileRecord / clusterSize; 
		}
		
		int vcn = 0;
		
		byte[] data = dataAttribute.readVCN(
					vcn,
					howmanyToRead
			);	

		int offset = 0;
		
		while(true)
		{
			
			NTFSFileRecord record = new NTFSFileRecord(
					this,
					NTFSUTIL.extractSubBuffer(
						data,
						offset,
						bytesPerFileRecord
					)
				);
			
			if(record.isDirectory() && record.getFileName().equals("."))
			{
				return record;
			}
			
			if(bytesPerFileRecord < clusterSize)
			{	
				offset += bytesPerFileRecord;
				if(offset == clusterSize)
				{
					vcn += howmanyToRead;
					data = dataAttribute.readVCN(
							vcn,
							howmanyToRead
					);
					offset=0;
				}
			}
			else
			{
				vcn += howmanyToRead;
				data = dataAttribute.readVCN(
						vcn,
						howmanyToRead
				);
			}
		}
	}
	/**
	 * @return Returns the currentNameSpace.
	 */
	public byte getCurrentNameSpace()
	{
		return currentNameSpace;
	}
	/**
	 * @param currentNameSpace The currentNameSpace to set.
	 */
	public void setCurrentNameSpace(byte currentNameSpace)
	{
		this.currentNameSpace = currentNameSpace;
	}
}
