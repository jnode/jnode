/*
 * $Id$
 */
package org.jnode.fs.iso9660;

import java.io.IOException;

import org.jnode.driver.block.BlockDeviceAPI;

/**
 * @author vali
 */
public class ISO9660Volume 
{
	private BlockDeviceAPI api = null;
	private VolumeDescriptor volumeDescriptor = null;
	
	public ISO9660Volume(BlockDeviceAPI api) throws IOException
	{
		this.api = api;
		initVolumeDescriptors();
	}

	public void initVolumeDescriptors() throws IOException
	{
		int currentLBN = 16;
		byte[] buff = new byte[ISO9660FileSystem.DefaultLBNSize];
		
		while(true)
		{
			// read the LB
			 this.readFromLBN(currentLBN,0,buff,0,ISO9660FileSystem.DefaultLBNSize);
			 VolumeDescriptor vd = new VolumeDescriptor(this,buff);
			 if(vd.getType() == VolumeDescriptor.VolumeSetTerminator_TYPE)
			 	return;
			 if(vd.getType() == VolumeDescriptor.PrimaryVolumeDescriptor_TYPE)
			 {
			 	this.setVolumeDescriptor(vd);
			 }
			 /*
			  if(vd.getType() == VolumeDescriptor.SupplementaryVolumeDescriptor_TYPE)
			 {
			 	this.setVolumeDescriptor(vd);
			 }
			 */
			 currentLBN++;
		}
		
	}
	public void readFromLBN(int startLBN,long offset, byte[] buffer, int bufferOffset , int length) throws IOException
	{
		api.read((startLBN * ISO9660FileSystem.DefaultLBNSize) + offset,buffer,bufferOffset,length);
	}

	/**
	 * @return Returns the volumeDescriptor.
	 */
	public VolumeDescriptor getVolumeDescriptor() {
		return volumeDescriptor;
	}
	/**
	 * @param pvd The volumeDescriptor to set.
	 */
	public void setVolumeDescriptor(VolumeDescriptor pvd) {
		this.volumeDescriptor = pvd;
	}
}
