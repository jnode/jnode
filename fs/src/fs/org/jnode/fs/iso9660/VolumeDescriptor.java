/*
 * $Id$
 */
package org.jnode.fs.iso9660;

import java.io.IOException;

import org.jnode.util.BigEndian;
import org.jnode.util.LittleEndian;

/**
 * @author vali
 *
 */
public class VolumeDescriptor 
{
	public static int VolumeSetTerminator_TYPE = 255;
	public static int PrimaryVolumeDescriptor_TYPE = 1;
	public static int SupplementaryVolumeDescriptor_TYPE = 2;
	
	private int type = 0;
	private String standardIdentifier = null;
	private String systemIdentifier = null;
	private String volumeIdentifier = null;
	private String volumeSetIdentifier = null;
	private int numberOfLB = 0; 
	private int LBSize = 0; 
	private int pathTableSize = 0; 
	private int locationOfTyp_L_PathTable = 0; 
	private int locationOfOptionalTyp_L_PathTable = 0; 
	private int locationOfTyp_M_PathTable = 0; 
	private int locationOfOptionalTyp_M_PathTable = 0; 
	private int volumeSetSize = 0;
	private EntryRecord rootDirectoryEntry = null;
	private ISO9660Volume volume = null;

	public VolumeDescriptor( ISO9660Volume volume, byte[] buffer) throws IOException
	{
		this.volume = volume;
		init(buffer);
	}
	public void printOut()
	{
		System.out.println("Primary volume information: ");
		System.out.println("	- Standard Identifier: " + this.getStandardIdentifier());
		System.out.println("	- System Identifier: " + this.getSystemIdentifier());
		System.out.println("	- Volume Identifier: " + this.getVolumeIdentifier());
		System.out.println("	- Volume set Identifier: " + this.getVolumeSetIdentifier());
		System.out.println("	- Volume set size: " + this.getVolumeSetSize());
		System.out.println("	- Number of LBs: " + this.getNumberOfLB());
		System.out.println("	- Size of LBs: " + this.getLBSize());
		System.out.println("	- PathTable size: " + this.getPathTableSize());
		System.out.println("		- Location of L PathTable : " + this.getLocationOfTyp_L_PathTable());
		System.out.println("		- Location of Optional L PathTable : " + this.getLocationOfOptionalTyp_L_PathTable());
		System.out.println("		- Location of M PathTable : " + this.getLocationOfTyp_M_PathTable());
		System.out.println("		- Location of Optional M PathTable : " + this.getLocationOfOptionalTyp_M_PathTable());
		System.out.println("	- Root directory entry: " );
		System.out.println("		- Size: " + this.getRootDirectoryEntry().getLengthOfDirectoryEntry());
		System.out.println("		- Extended attribute size: " + this.getRootDirectoryEntry().getLengthOfExtendedAttribute());
		System.out.println("		- Location of the extent: " + this.getRootDirectoryEntry().getLocationOfExtent());
		System.out.println("		- Length of the file identifier: " + this.getRootDirectoryEntry().getLengthOfFileIdentifier());
		System.out.println("		- is directory: " + this.getRootDirectoryEntry().isDirectory());
		System.out.println("		- File identifier: " + this.getRootDirectoryEntry().getFileIdentifier());
		System.out.println("		- Data Length: " + this.getRootDirectoryEntry().getDataLength());
		System.out.println("		- File unit size: " + this.getRootDirectoryEntry().getFileUnitSize());
		
	}
	
	public void init(byte[] buff)
	{
		this.setType(LittleEndian.getUInt8(buff, 0));
		this.setStandardIdentifier(new String(buff,1,5));
		this.setSystemIdentifier(new String(buff,8,31));
		this.setVolumeIdentifier(new String(buff,40,31));
		this.setVolumeSetIdentifier(new String(buff,190,127));
		this.setNumberOfLB((int)LittleEndian.getUInt32(buff,80));
		this.setVolumeSetSize(LittleEndian.getUInt16(buff, 120));
		
		this.setLBSize(LittleEndian.getUInt16(buff, 128));
		// path table info
		this.setPathTableSize((int)LittleEndian.getUInt32(buff,132));
		this.setLocationOfTyp_L_PathTable((int)LittleEndian.getUInt32(buff,140));
		this.setLocationOfOptionalTyp_L_PathTable((int)LittleEndian.getUInt32(buff,144));
		this.setLocationOfTyp_M_PathTable((int)BigEndian.getUInt32(buff,148));
		this.setLocationOfOptionalTyp_M_PathTable((int)BigEndian.getUInt32(buff,152));
		
		this.setRootDirectoryEntry(new EntryRecord(this.getVolume(),buff,156));
		
	}		
	/**
	 * @return Returns the numberOfLB.
	 */
	public int getNumberOfLB()
	{
		return numberOfLB;
	}
	/**
	 * @param numberOfLB The numberOfLB to set.
	 */
	public void setNumberOfLB(int numberOfLB)
	{
		this.numberOfLB = numberOfLB;
	}
	/**
	 * @return Returns the lBSize.
	 */
	public int getLBSize()
	{
		return LBSize;
	}
	/**
	 * @param size The lBSize to set.
	 */
	public void setLBSize(int size)
	{
		LBSize = size;
	}
	/**
	 * @return Returns the patheTableSize.
	 */
	public int getPathTableSize()
	{
		return pathTableSize;
	}
	/**
	 * @param patheTableSize The patheTableSize to set.
	 */
	public void setPathTableSize(int patheTableSize)
	{
		this.pathTableSize = patheTableSize;
	}
	/**
	 * @return Returns the locationOfOptionalTyp_L_PathTable.
	 */
	public int getLocationOfOptionalTyp_L_PathTable()
	{
		return locationOfOptionalTyp_L_PathTable;
	}
	/**
	 * @param locationOfOptionalTyp_L_PathTable The locationOfOptionalTyp_L_PathTable to set.
	 */
	public void setLocationOfOptionalTyp_L_PathTable(
			int locationOfOptionalTyp_L_PathTable)
	{
		this.locationOfOptionalTyp_L_PathTable = locationOfOptionalTyp_L_PathTable;
	}
	/**
	 * @return Returns the locationOfOptionalTyp_M_PathTable.
	 */
	public int getLocationOfOptionalTyp_M_PathTable()
	{
		return locationOfOptionalTyp_M_PathTable;
	}
	/**
	 * @param locationOfOptionalTyp_M_PathTable The locationOfOptionalTyp_M_PathTable to set.
	 */
	public void setLocationOfOptionalTyp_M_PathTable(
			int locationOfOptionalTyp_M_PathTable)
	{
		this.locationOfOptionalTyp_M_PathTable = locationOfOptionalTyp_M_PathTable;
	}
	/**
	 * @return Returns the locationOfTyp_L_PathTable.
	 */
	public int getLocationOfTyp_L_PathTable()
	{
		return locationOfTyp_L_PathTable;
	}
	/**
	 * @param locationOfTyp_L_PathTable The locationOfTyp_L_PathTable to set.
	 */
	public void setLocationOfTyp_L_PathTable(int locationOfTyp_L_PathTable)
	{
		this.locationOfTyp_L_PathTable = locationOfTyp_L_PathTable;
	}
	/**
	 * @return Returns the locationOfTyp_M_PathTable.
	 */
	public int getLocationOfTyp_M_PathTable()
	{
		return locationOfTyp_M_PathTable;
	}
	/**
	 * @param locationOfTyp_M_PathTable The locationOfTyp_M_PathTable to set.
	 */
	public void setLocationOfTyp_M_PathTable(int locationOfTyp_M_PathTable)
	{
		this.locationOfTyp_M_PathTable = locationOfTyp_M_PathTable;
	}
	/**
	 * @return Returns the rootDirectoryEntry.
	 */
	public EntryRecord getRootDirectoryEntry()
	{
		return rootDirectoryEntry;
	}
	/**
	 * @param rootDirectoryEntry The rootDirectoryEntry to set.
	 */
	public void setRootDirectoryEntry(EntryRecord rootDirectoryEntry)
	{
		this.rootDirectoryEntry = rootDirectoryEntry;
	}
	/**
	 * @return Returns the volumeSetSize.
	 */
	public int getVolumeSetSize() {
		return volumeSetSize;
	}
	/**
	 * @param volumeSetSize The volumeSetSize to set.
	 */
	public void setVolumeSetSize(int volumeSetSize) {
		this.volumeSetSize = volumeSetSize;
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
	/**
	 * @return Returns the volumeSetIdentifier.
	 */
	public String getVolumeSetIdentifier()
	{
		return volumeSetIdentifier;
	}
	/**
	 * @param volumeSetIdentifier The volumeSetIdentifier to set.
	 */
	public void setVolumeSetIdentifier(String volumeSetIdentifier)
	{
		this.volumeSetIdentifier = volumeSetIdentifier;
	}
	
	/**
	 * @return Returns the standardIdentifier.
	 */
	public String getStandardIdentifier()
	{
		return standardIdentifier;
	}
	/**
	 * @param standardIdentifier The standardIdentifier to set.
	 */
	public void setStandardIdentifier(String standardIdentifier)
	{
		this.standardIdentifier = standardIdentifier;
	}
	/**
	 * @return Returns the systemIdentifier.
	 */
	public String getSystemIdentifier()
	{
		return systemIdentifier;
	}
	/**
	 * @param systemIdentifier The systemIdentifier to set.
	 */
	public void setSystemIdentifier(String systemIdentifier)
	{
		this.systemIdentifier = systemIdentifier;
	}
	/**
	 * @return Returns the volumeIdentifier.
	 */
	public String getVolumeIdentifier()
	{
		return volumeIdentifier;
	}
	/**
	 * @param volumeIdentifier The volumeIdentifier to set.
	 */
	public void setVolumeIdentifier(String volumeIdentifier)
	{
		this.volumeIdentifier = volumeIdentifier;
	}
	
	/**
	 * @return Returns the type.
	 */
	public int getType() {
		return type;
	}
	/**
	 * @param type The type to set.
	 */
	public void setType(int type) {
		this.type = type;
	}
}
