package org.jnode.fs.jfat.command;

import org.jnode.fs.jfat.BootSector;

/**
 * 
 * @author Tango Devian
 * 
 */
/**
 * This file will contain the differetn value se
 * 
 */


class GrubJFatBootSector extends BootSector{

	public GrubJFatBootSector(byte[] sector) {
		super(sector);
	}
	/**
	 * Constructor for GrubBootSector.
	 * @param size
	 */
	public GrubJFatBootSector(int size) {
		super(size);
	}
	/**
	 * Gets the first sector of stage1_5 
	 * @return long
	 */
	public long getStage1_5Sector() {
		return get32(0x44);
	}

	/**
	 * Sets the first sector of stage1_5
	 */
	public void setStage1_5Sector(long v) {
		set32(0x44, v);
	}
	
	
	
	
	
}
