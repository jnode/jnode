/*
 * $Id$
 */
package org.jnode.fs.fat;

import org.jnode.fs.util.*;

/**
 * @author gbin
 */
public class FatLfnDirEntry extends FatBasicDirEntry {

	/**
	 * @param dir
	 */
	public FatLfnDirEntry(AbstractDirectory dir) {
		super(dir);
	}

	/**
	 * @param dir
	 * @param src
	 * @param offset
	 */
	public FatLfnDirEntry(AbstractDirectory dir, byte[] src, int offset) {
		super(dir, src, offset);
	}

	public FatLfnDirEntry(AbstractDirectory dir, String subName, int ordinal, byte checkSum, boolean isLast) {
		super(dir);
		//System.out.println("Create lfn entry with subname '" + subName + "'");
		char[] unicodechar = new char[13]; //{ ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ' };
		subName.getChars(0, subName.length(), unicodechar, 0);
		if (isLast) {
			DosUtils.set8(rawData, 0, ordinal + (1 << 6)); // set the 6th
			// security ending
			// bit
		} else {
			DosUtils.set8(rawData, 0, ordinal);
		}

		DosUtils.set16(rawData, 1, unicodechar[0]);
		DosUtils.set16(rawData, 3, unicodechar[1]);
		DosUtils.set16(rawData, 5, unicodechar[2]);
		DosUtils.set16(rawData, 7, unicodechar[3]);
		DosUtils.set16(rawData, 9, unicodechar[4]);
		DosUtils.set8(rawData, 11, 0x0f); // this is the hidden attribute tag for
		// lfn
		DosUtils.set8(rawData, 12, 0); // reserved
		DosUtils.set8(rawData, 13, checkSum); // checksum
		DosUtils.set16(rawData, 14, unicodechar[5]);
		DosUtils.set16(rawData, 16, unicodechar[6]);
		DosUtils.set16(rawData, 18, unicodechar[7]);
		DosUtils.set16(rawData, 20, unicodechar[8]);
		DosUtils.set16(rawData, 22, unicodechar[9]);
		DosUtils.set16(rawData, 24, unicodechar[10]);
		DosUtils.set16(rawData, 26, 0); // sector... unused
		DosUtils.set16(rawData, 28, unicodechar[11]);
		DosUtils.set16(rawData, 30, unicodechar[12]);
		//System.out.println("Est ce meme '" + getSubstring() + "'");

	}

	public byte getOrdinal() {
		return (byte)DosUtils.get8(rawData, 0);
	}

	public byte getCheckSum() {
		return (byte)DosUtils.get8(rawData, 13);
	}

	public String getSubstring() {
		char[] unicodechar = new char[13];
		unicodechar[0] = (char)DosUtils.get16(rawData, 1);
		unicodechar[1] = (char)DosUtils.get16(rawData, 3);
		unicodechar[2] = (char)DosUtils.get16(rawData, 5);
		unicodechar[3] = (char)DosUtils.get16(rawData, 7);
		unicodechar[4] = (char)DosUtils.get16(rawData, 9);
		unicodechar[5] = (char)DosUtils.get16(rawData, 14);
		unicodechar[6] = (char)DosUtils.get16(rawData, 16);
		unicodechar[7] = (char)DosUtils.get16(rawData, 18);
		unicodechar[8] = (char)DosUtils.get16(rawData, 20);
		unicodechar[9] = (char)DosUtils.get16(rawData, 22);
		unicodechar[10] = (char)DosUtils.get16(rawData, 24);
		unicodechar[11] = (char)DosUtils.get16(rawData, 28);
		unicodechar[12] = (char)DosUtils.get16(rawData, 30);
		int index = 12;
		while (unicodechar[index] == 0)
			index--;
		return (new String(unicodechar)).substring(0, index + 1);
	}

	public String toString() {
		return "LFN ordinal " + getOrdinal() + " subString = " + getSubstring() + "CheckSum = " + getCheckSum();
	}
}
