/**
 * $Id$
 */
package org.jnode.fs.fat;

import org.apache.log4j.Logger;
import org.jnode.fs.util.DosUtils;

/**
 * <description>
 * 
 * @author epr
 * @author Fabien DUMINY
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
		long fatSize = sectsPerFat * sectSize;
		
		offset += fatNr * fatSize;
		
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
	
	/**
	 * Return the name (without extension) of a full file name
	 * @param nameExt
	 * @return
	 */
	static public String splitName(String nameExt) {
		int i = nameExt.indexOf('.');
		if (i < 0) {
			return nameExt;
		} else {
			return nameExt.substring(0, i);
		}
	}

	/**
	 * Return the extension (without name) of a full file name
	 * @param nameExt
	 * @return
	 */
	static public String splitExt(String nameExt) {
		int i = nameExt.indexOf('.');
		if (i < 0) {
			return "";
		} else {
			return nameExt.substring(i + 1);
		}
	}
	
	/**
	 * Normalize full file name in DOS 8.3 format from the name and the ext
	 * @param name
	 * @param ext
	 * @return
	 */
	static public String normalizeName(String name, String ext)
	{
		if (ext.length() > 0) {
			return (name + "." + ext).toUpperCase();
		} else {
			return name.toUpperCase();
		}		
	}

	/**
	 * Normalize full file name in DOS 8.3 format from the given full name
	 * @param nameExt
	 * @return
	 */
	static public String normalizeName(String nameExt)
	{
		if(nameExt.equals("."))
			return nameExt;
		
		if(nameExt.equals(".."))
			return nameExt;
		
		return normalizeName(splitName(nameExt), splitExt(nameExt));
	}
	
	static public final int SUBNAME_SIZE = 13; 

	/**
	 * Write the part of a long file name to the given byte array
	 * @param src
	 * @param srcOffset
	 * @param ordinal
	 * @param checkSum
	 * @param isLast
	 * @param dest
	 * @param destOffset
	 */
	static public void writeSubString(char[] src, int srcOffset, int ordinal, byte checkSum, 
				boolean isLast, byte[] dest, int destOffset) 
	{
		log.debug("<<< BEGIN writeSubString subString="+new String(src, srcOffset, SUBNAME_SIZE)+" >>>");
		if (isLast) {
			DosUtils.set8(dest, destOffset, ordinal + (1 << 6)); // set the 6th
			// security ending
			// bit
		} else {
			DosUtils.set8(dest, destOffset, ordinal);
		}

		DosUtils.set16(dest, destOffset + 1, src[srcOffset+0]);
		DosUtils.set16(dest, destOffset + 3, src[srcOffset+1]);
		DosUtils.set16(dest, destOffset + 5, src[srcOffset+2]);
		DosUtils.set16(dest, destOffset + 7, src[srcOffset+3]);
		DosUtils.set16(dest, destOffset + 9, src[srcOffset+4]);
		DosUtils.set8(dest, destOffset + 11, 0x0f); // this is the hidden attribute tag for
		// lfn
		DosUtils.set8(dest, destOffset + 12, 0); // reserved
		DosUtils.set8(dest, destOffset + 13, checkSum); // checksum
		DosUtils.set16(dest, destOffset + 14, src[srcOffset+5]);
		DosUtils.set16(dest, destOffset + 16, src[srcOffset+6]);
		DosUtils.set16(dest, destOffset + 18, src[srcOffset+7]);
		DosUtils.set16(dest, destOffset + 20, src[srcOffset+8]);
		DosUtils.set16(dest, destOffset + 22, src[srcOffset+9]);
		DosUtils.set16(dest, destOffset + 24, src[srcOffset+10]);
		DosUtils.set16(dest, destOffset + 26, 0); // sector... unused
		DosUtils.set16(dest, destOffset + 28, src[srcOffset+11]);
		DosUtils.set16(dest, destOffset + 30, src[srcOffset+12]);
		
		log.debug("<<< END writeSubString dest=\n" /* +FSUtils.toString(dest)*/ +">>>");		
	}

	static public byte getOrdinal(byte[] rawData, int offset) {
		return (byte)DosUtils.get8(rawData, offset);
	}

	static public byte getCheckSum(byte[] rawData, int offset) {
		return (byte)DosUtils.get8(rawData, offset + 13);
	}

	/**
	 * Read a part of the long filename from the given byte array and
	 * append the result to the given StringBuffer  	
	 * @param sb
	 * @param rawData
	 * @param offset
	 */
	static public void appendSubstring(StringBuffer sb, byte[] rawData, int offset) {
		log.debug("<<< BEGIN appendSubstring buffer="+sb.toString()+">>>");
		int index = 12;
		char[] unicodechar = getUnicodeChars(rawData, offset);
		log.debug("appendSubstring: unicodechar="+new String(unicodechar));
		while (unicodechar[index] == 0)
			index--;
		
		sb.append(unicodechar,0, index+1);
		log.debug("<<< END appendSubstring buffer="+sb.toString()+">>>");
	}

	/**
	 * Return a part of a long file name read from the given byte array
     */ 
	static public String getSubstring(byte[] rawData, int offset) {
		log.debug("<<< BEGIN getSubString: rawData=" /*+FSUtils.toString(rawData, offset, 12)*/+" >>>");
		//log.debug("getSubString: rawData as chars="+FSUtils.toStringAsChars(rawData, offset, 12));
		int index = 12;
		char[] unicodechar = getUnicodeChars(rawData, offset);
		while (unicodechar[index] == 0)
			index--;
						
		//log.debug("getSubString: rawData.length="+rawData.length+" offset="+offset+" nbChars(index)="+index);						
		String str = new String(unicodechar, 0, index);
		log.debug("<<< END getSubString: return="+str+" >>>");
		
		return str;
	}

	/**
	  * convert an array of bytes to an array of chars (unicode)
	  */
	static char[] getUnicodeChars(byte[] rawData, int offset)
	{
		char[] unicodechar = new char[SUBNAME_SIZE];
		unicodechar[0] = (char)DosUtils.get16(rawData, offset + 1);
		unicodechar[1] = (char)DosUtils.get16(rawData, offset + 3);
		unicodechar[2] = (char)DosUtils.get16(rawData, offset + 5);
		unicodechar[3] = (char)DosUtils.get16(rawData, offset + 7);
		unicodechar[4] = (char)DosUtils.get16(rawData, offset + 9);
		unicodechar[5] = (char)DosUtils.get16(rawData, offset + 14);
		unicodechar[6] = (char)DosUtils.get16(rawData, offset + 16);
		unicodechar[7] = (char)DosUtils.get16(rawData, offset + 18);
		unicodechar[8] = (char)DosUtils.get16(rawData, offset + 20);
		unicodechar[9] = (char)DosUtils.get16(rawData, offset + 22);
		unicodechar[10] = (char)DosUtils.get16(rawData, offset + 24);
		unicodechar[11] = (char)DosUtils.get16(rawData, offset + 28);
		unicodechar[12] = (char)DosUtils.get16(rawData, offset + 30);
		return unicodechar;
	}
	
    static private final Logger log = Logger.getLogger(FatUtils.class);	
	
}
