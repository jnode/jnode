/*
 * $Id$
 */
package org.jnode.fs.fat;

import java.io.IOException;
import java.util.Vector;

import org.jnode.fs.FSAccessRights;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FSFile;
import org.jnode.fs.FileSystem;

/**
 * @author gbin
 */
class LfnEntry implements FSEntry {
	// decompacted LFN entry
	private String fileName;
	// TODO: Make them available
	//private Date creationTime;
	//private Date lastAccessed;
	private FatLfnDirectory parent;
	private FatDirEntry realEntry;

	public LfnEntry(FatLfnDirectory parent, FatDirEntry realEntry, String longName) {
		this.realEntry = realEntry;
		this.parent = parent;
		fileName = longName.trim();
	}

	public LfnEntry(FatLfnDirectory parent, Vector entries, int offset, int length) {
		this.parent = parent;
		StringBuffer name = new StringBuffer();
		// this is just an old plain 8.3 entry, copy it;
		if (length == 1) {
			realEntry = (FatDirEntry)entries.get(offset);
			fileName = realEntry.getName();
			return;
		}
		// stored in reversed order
		for (int i = length - 2; i >= 0; i--) {
			FatLfnDirEntry entry = (FatLfnDirEntry)entries.get(i + offset);
			name.append(entry.getSubstring());
		}
		fileName = name.toString().trim();
		realEntry = (FatDirEntry)entries.get(offset + length - 1);
	}

	public FatBasicDirEntry[] compactForm() {
		int totalEntrySize = (fileName.length() / 13) + 1; // + 1 for the real

		if ((fileName.length() % 13) != 0) // there is a remaining part
			totalEntrySize++;

		// entry
		FatBasicDirEntry[] entries = new FatBasicDirEntry[totalEntrySize];
		int j = 0;
		int checkSum = calculateCheckSum();
		//System.out.println("calculated Checksum=" + checkSum);
		for (int i = totalEntrySize - 2; i > 0; i--) {
			entries[i] =
				new FatLfnDirEntry(parent, fileName.substring(j * 13, j * 13 + 13), j + 1, (byte)checkSum, false);
			j++;
		}
		//System.out.println("lfn = " + fileName.substring(j * 13));
		entries[0] = new FatLfnDirEntry(parent, fileName.substring(j * 13), j + 1, (byte)checkSum, true);
		entries[totalEntrySize - 1] = realEntry;
		return entries;

	}

	private byte calculateCheckSum() {

		char[] fullName = new char[] { ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ' };
		char[] name = realEntry.getNameOnly().toCharArray();
		char[] ext = realEntry.getExt().toCharArray();
		System.arraycopy(name, 0, fullName, 0, name.length);
		System.arraycopy(ext, 0, fullName, 8, ext.length);

		//System.out.println("Compute checksum on : '" + new String(fullName) + "'");
		byte[] dest = new byte[11];
		for (int i = 0; i < 11; i++)
			dest[i] = (byte)fullName[i];

		int sum = dest[0];
		for (int i = 1; i < 11; i++) {
			sum = dest[i] + (((sum & 1) << 7) + ((sum & 0xfe) >> 1));
		}

		//System.out.println("result = " + (byte) (sum & 0xff));
		return (byte) (sum & 0xff);
	}

	public String getName() {
		return fileName;
	}

	public FSDirectory getParent() {
		return realEntry.getParent();
	}

	public long getLastModified() {
		return realEntry.getLastModified();
	}

	public boolean isFile() {
		return realEntry.isFile();
	}

	public boolean isDirectory() {
		return realEntry.isDirectory();
	}

	public void setName(String newName) {
		fileName = newName;
		realEntry.setName(parent.generateShortNameFor(newName));
	}

	public void setLastModified(long lastModified) {
		realEntry.setLastModified(lastModified);
	}

	public FSFile getFile() throws IOException {
		return realEntry.getFile();
	}

	public FSDirectory getDirectory() throws IOException {
		return realEntry.getDirectory();
	}

	public FSAccessRights getAccessRights() throws IOException {
		return realEntry.getAccessRights();
	}

	public boolean isValid() {
		return realEntry.isValid();
	}

	public FileSystem getFileSystem() {
		return realEntry.getFileSystem();
	}

	public boolean isDeleted() {
		return realEntry.isDeleted();
	}

	public String toString() {
		return "LFN = " + fileName + " / SFN = " + realEntry.getName();
	}

	/**
	 * @return Returns the realEntry.
	 */
	public FatDirEntry getRealEntry() {
		return realEntry;
	}

	/**
	 * @param realEntry
	 *           The realEntry to set.
	 */
	public void setRealEntry(FatDirEntry realEntry) {
		this.realEntry = realEntry;
	}

}