/**
 * $Id$
 */
package org.jnode.fs.fat;

import org.jnode.fs.FileSystemException;

/**
 * <description>
 * 
 * @author epr
 */
public class GrubBootSector extends BootSector {

	/**
	 * Constructor for GrubBootSector.
	 * @param size
	 */
	public GrubBootSector(int size) {
		super(size);
	}

	/**
	 * Constructor for GrubBootSector.
	 * @param src
	 */
	public GrubBootSector(byte[] src) throws FileSystemException {
		super(src);
	}
	
	/**
	 * Gets the first sector of stage2 
	 * @return long
	 */
	public long getStage2Sector() {
		return get32(0x44);
	}

	/**
	 * Sets the first sector of stage2
	 */
	public void setStage2Sector(long v) {
		set32(0x44, v);
	}

}
