/*
 * $Id$
 */
package org.jnode.fs.ntfs;

import java.io.IOException;
import java.util.Iterator;

import org.jnode.driver.block.BlockDeviceAPI;


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
	public NTFSFileRecord getFirstFileRecord() throws IOException
	{
		return getFileRecord(0);
	}
	public Iterator getNTFSIterator()
	{
		return new Iterator()
		{

			int offset = 0;
			NTFSFileRecord fileRecord = null;
			
			public boolean hasNext() 
			{
				if(offset == -1)
					return false;
				if(fileRecord != null)
					if(fileRecord.getAlocatedSize() <= 0)
						return false;
				return true;
			}

			public Object next() {
				if(fileRecord != null)
					offset += Math.round(fileRecord.getAlocatedSize() / NTFSVolume.this.getClusterSize());
				try 
				{
					
					fileRecord = getFileRecord(offset);
					
				} catch (IOException e) {
					offset = -1;
					return null;
				}
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
			String fileName = fileRecord.getFileName();
			if(fileName.equals("."))
			{
				return fileRecord; 
			}
		}	
		return null;
	}
	public NTFSFileRecord getFileRecord(int MFTClusterOffset) throws IOException
	{
		return new NTFSFileRecord(this,MFTClusterOffset);
	}
}
