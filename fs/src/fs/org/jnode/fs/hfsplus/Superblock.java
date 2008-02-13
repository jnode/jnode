package org.jnode.fs.hfsplus;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jnode.fs.FileSystemException;
import org.jnode.util.BigEndian;
import org.jnode.util.NumberUtils;
/**
 * HFS+ volume header definition.
 * 
 * @author Fabien L.
 *
 */
public class Superblock extends HFSPlusObject {
	/** */
	private final Logger log = Logger.getLogger(getClass());
	/** */
	public static final int SUPERBLOCK_LENGTH = 1024;
	/** Data bytes array that contains superblock informations */
	private byte data[];
	
	public Superblock(HfsPlusFileSystem fs) throws FileSystemException {
		super(fs);
		log.setLevel(Level.INFO);
		try {
			ByteBuffer b = ByteBuffer.allocate(SUPERBLOCK_LENGTH);
			 //skip the first 1024 bytes (bootsector) and read the superblock
			fs.getApi().read(1024, b);
			data = new byte[SUPERBLOCK_LENGTH];
			System.arraycopy(b.array(), 0, data, 0, SUPERBLOCK_LENGTH);
			if(getMagic() != HfsPlusConstants.HFSPLUS_SUPER_MAGIC)
				throw new FileSystemException("Not hfs+ superblock ("+getMagic()+": bad magic)");
		} catch (IOException e) {
			throw new FileSystemException(e);
		}
	}
	
	public int getMagic() {
		return BigEndian.getInt16(data, 0);
	}
	
	public int getVersion() {
		return BigEndian.getInt16(data, 2);
	}
	
	public int getAttributes(){
		return BigEndian.getInt32(data, 4);
	}
	/**
	 * Get string representation of attribute.
	 * 
	 * @return
	 */
	public String getAttributesAsString(){
		String s = "";
		s = s + ((isAttribute(HfsPlusConstants.HFSPLUS_VOL_UNMNT_BIT))?" kHFSVolumeUnmountedBit":"");
		s = s + ((isAttribute(HfsPlusConstants.HFSPLUS_VOL_INCNSTNT_BIT))?" kHFSBootVolumeInconsistentBit":"");
		s = s + ((isAttribute(HfsPlusConstants.HFSPLUS_VOL_JOURNALED_BIT))?" kHFSVolumeJournaledBit":"");
		return s;
	}
	/**
	 * Check if a specific attribute is set.
	 * 
	 * @param maskBit See constants.
	 * 
	 * @return true if attribute is set.
	 */
	public boolean isAttribute(int maskBit){
		return (((getAttributes() >> maskBit) & 0x1) != 0);
	}
	
	public int getLastMountedVersion(){
		return BigEndian.getInt32(data, 8);
	}
	
	public int getJournalInfoBlock(){
		return BigEndian.getInt32(data, 12);
	}
	
	public int getCreateDate(){
		return BigEndian.getInt32(data, 16);
	}
	
	public int getModifyDate(){
		return BigEndian.getInt32(data, 20);
	}
	
	public int getBackupDate(){
		return BigEndian.getInt32(data, 24);
	}
	
	public int getCheckedDate(){
		return BigEndian.getInt32(data, 28);
	}
	//
	public int getFileCount(){
		return BigEndian.getInt32(data, 32);
	}
	public int getFolderCount(){
		return BigEndian.getInt32(data, 36);
	}
	//
	public int getBlockSize(){
		return BigEndian.getInt32(data, 40);
	}
	public int getTotalBlocks(){
		return BigEndian.getInt32(data, 44);
	}
	public int getFreeBlocks(){
		return BigEndian.getInt32(data, 48);
	}
	//
	public int getNextAllocation(){
		return BigEndian.getInt32(data, 52);
	}
	public long getRsrcClumpSize(){
		return BigEndian.getInt32(data, 56);
	}
	public int getDataClumpSize(){
		return BigEndian.getInt32(data, 60);
	}
	
	public int getNextCatalogId(){
		return BigEndian.getInt32(data, 64);
	}
	
	public int getWriteCount(){
		return BigEndian.getInt32(data, 68);
	}
	
	public long getEncodingsBmp(){
		return BigEndian.getInt64(data, 72);
	}
	
	public byte[] getFinderInfo(){
		byte[] result=new byte[32];
		System.arraycopy(data, 80, result, 0, 32);
		return result;
	}
	
	public HFSPlusForkData getAllocationFile(){
		return new HFSPlusForkData(data,112);
	}
	
	public HFSPlusForkData getExtentsFile(){
		return new HFSPlusForkData(data,192);
	}
	
	public HFSPlusForkData getCatalogFile(){
		return new HFSPlusForkData(data,272);
	}
	
	public HFSPlusForkData getAttributesFile(){
		return new HFSPlusForkData(data,352);
	}
	
	public HFSPlusForkData getStartupFile(){
		return new HFSPlusForkData(data,432);
	}
	
	
	public String toString(){
		StringBuffer buffer = new StringBuffer();
		buffer.append("Magic   		:0x").append(NumberUtils.hex(getMagic(),4)).append("\n");
		buffer.append("Version 		:").append(getVersion()).append("\n").append("\n");
		buffer.append("Attributes	:").append(getAttributesAsString()).append("\n").append("\n");
		buffer.append("Create date 	:").append(HFSUtils.printDate(getCreateDate(),"EEE MMM d HH:mm:ss yyyy")).append("\n");
		buffer.append("Modify date 	:").append(HFSUtils.printDate(getModifyDate(),"EEE MMM d HH:mm:ss yyyy")).append("\n");
		buffer.append("Backup date 	:").append(HFSUtils.printDate(getBackupDate(),"EEE MMM d HH:mm:ss yyyy")).append("\n");
		buffer.append("Checked date	:").append(HFSUtils.printDate(getCheckedDate(),"EEE MMM d HH:mm:ss yyyy")).append("\n").append("\n");
		buffer.append("File count 	:").append(getFileCount()).append("\n");
		buffer.append("Folder count	:").append(getFolderCount()).append("\n").append("\n");
		buffer.append("Block size 	:").append(getBlockSize()).append("\n");
		buffer.append("Total blocks	:").append(getTotalBlocks()).append("\n");
		buffer.append("Free blocks 	:").append(getFreeBlocks()).append("\n").append("\n");
		buffer.append("Next catalog ID	:").append(getNextCatalogId()).append("\n");
		buffer.append("Write count	:").append(getWriteCount()).append("\n");
		buffer.append("Encoding bmp	:").append(getEncodingsBmp()).append("\n");
		buffer.append("Finder Infos	:").append(getFinderInfo()).append("\n").append("\n");
		buffer.append("Finder Infos	:").append(getJournalInfoBlock()).append("\n").append("\n");
		buffer.append("Allocation file").append("\n");
		buffer.append(getAllocationFile().toString()).append("\n");
		buffer.append("Extents file").append("\n");
		buffer.append(getExtentsFile().toString()).append("\n");
		buffer.append("Catalog file").append("\n");
		buffer.append(getCatalogFile().toString()).append("\n");
		buffer.append("Attributes file").append("\n");
		buffer.append(getAttributesFile().toString()).append("\n");
		buffer.append("Startup file").append("\n");
		buffer.append(getStartupFile().toString()).append("\n");
		return buffer.toString();
	}
}
