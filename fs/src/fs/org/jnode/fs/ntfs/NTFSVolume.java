/*
 * $Id$
 */
package org.jnode.fs.ntfs;

import java.io.IOException;
import java.util.Iterator;

import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.fs.FileSystemException;


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
	public Iterator getNTFSIterator() throws IOException
	{
		if(this.getBootRecord().getBytesPerFileRecord() < this.getClusterSize())
			// loop over bytes from sector
			return new Iterator()
			{
	
				int offset = 0;
				int clusterOffset = 0;
				NTFSFileRecord fileRecord = NTFSVolume.this.getMFTRecord();
				
			    byte[] buffer = NTFSVolume.this.getMFTRecord().getAttribute(NTFSFileRecord.$DATA).readVCN(0,1,NTFSVolume.this.getClusterSize()); 
				
				public boolean hasNext() 
				{
					return new String(buffer,offset,4).equals("FILE");
				}
	
				public Object next() {
					try {
						fileRecord = new NTFSFileRecord(
											NTFSVolume.this,
											NTFSUTIL.extractSubBuffer(
														buffer
														,offset,
														NTFSVolume.this.getBootRecord().getBytesPerFileRecord())
						);
						
						offset += NTFSVolume.this.getBootRecord().getBytesPerFileRecord();
						
						if(offset >= NTFSVolume.this.getClusterSize())
						{	
							clusterOffset++;
							offset = 0;
							buffer = NTFSVolume.this.getMFTRecord().getAttribute(NTFSFileRecord.$DATA).readVCN(clusterOffset,1,NTFSVolume.this.getClusterSize());
						}
						
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				
					return fileRecord;
				}
	
				public void remove() {
					throw new UnsupportedOperationException("not yet implemented");
				}
			
			};
		else
			return new Iterator()
			{
	
				int clusterOffset = 0;
				
				NTFSFileRecord fileRecord = NTFSVolume.this.getMFTRecord();
				//number of VCNS
				int VCNNumber = fileRecord.getAttribute(NTFSFileRecord.$DATA).getNumberOfVCN();
				
				byte[] buffer = NTFSVolume.this.getMFTRecord().getAttribute(NTFSFileRecord.$DATA).readVCN(
						clusterOffset,
						NTFSVolume.this.getBootRecord().getBytesPerFileRecord() / NTFSVolume.this.getClusterSize(),
						NTFSVolume.this.getClusterSize());
				
				
				public boolean hasNext() 
				{
					if(clusterOffset < VCNNumber && new String(buffer,0,4).equals(NTFSFileRecord.MAGIC_NUMBER)) 
					 	return true;
					else
						return false;
				}
	
				public Object next() {
					try {
							fileRecord = new NTFSFileRecord(
												NTFSVolume.this,
															buffer
							);
						clusterOffset+= NTFSVolume.this.getBootRecord().getBytesPerFileRecord() / NTFSVolume.this.getClusterSize();
						buffer = NTFSVolume.this.getMFTRecord().getAttribute(NTFSFileRecord.$DATA).readVCN(
								clusterOffset,
								NTFSVolume.this.getBootRecord().getBytesPerFileRecord() / NTFSVolume.this.getClusterSize(),
								NTFSVolume.this.getClusterSize());
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				
					return fileRecord;
				}
	
				public void remove() {
					throw new UnsupportedOperationException("not yet implemented");
				}
			
			};
}
	public NTFSFileRecord getRootDirectory() throws IOException
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
}
