/**
 * $Id$
 */
package org.jnode.fs.fat;

/**
 * <description>
 * 
 * @author epr
 */
public class FatUtils {
	
	public static final int FIRST_CLUSTER = 2;

	/**
	 * Gets the offset (in bytes) of the fat with the given index
	 * @param bs
	 * @param fatNr (0..)
	 * @return long
	 */
	public static long getFatOffset(BootSector bs, int fatNr) {
		long sectSize = bs.getBytesPerSector();
		long sectsPerFat = bs.getSectorsPerFat();
		long resSects = bs.getNrReservedSectors();
		
		long offset = resSects * sectSize;
		
		if (fatNr > 0) {
			offset += (fatNr-1) * sectsPerFat * sectSize;
		}
		
		return offset;
	}

	/**
	 * Gets the offset (in bytes) of the root directory with the given index
	 * @param bs
	 * @return long
	 */
	public static long getRootDirOffset(BootSector bs) {
		long sectSize = bs.getBytesPerSector();
		long sectsPerFat = bs.getSectorsPerFat();
		int fats = bs.getNrFats();
		
		long offset = getFatOffset(bs, 0);
		
		offset += fats * sectsPerFat * sectSize;
		
		return offset;
	}

	/**
	 * Gets the offset of the data (file) area 
	 * @param bs
	 * @return long
	 */
	public static long getFilesOffset(BootSector bs) {
		long offset = getRootDirOffset(bs);
		
		offset += bs.getNrRootDirEntries() * 32;
		
		return offset;
	}
}
