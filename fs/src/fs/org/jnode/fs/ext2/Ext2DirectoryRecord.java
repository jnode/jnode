/*
 * $Id$
 */
package org.jnode.fs.ext2;

import org.apache.log4j.Logger;

/**
 * A single directory record, i.e. the inode number and name of an entry in a directory
 * 
 * @author Andras Nagy
 */
public class Ext2DirectoryRecord {
	private int iNodeNr;
	private int recLen;
	private short nameLen;
	private short type;
	private StringBuffer name;
	private static final Logger log = Logger.getLogger(Ext2DirectoryRecord.class);

	/**
	 * @param data:
	 *            the data that makes up the directory block
	 * @param offset:
	 *            the offset where the current DirectoryRecord begins
	 */
	public Ext2DirectoryRecord(byte[] data, int offset) {
		iNodeNr = (int) Ext2Utils.get32(data, offset);
		recLen = Ext2Utils.get16(data, offset + 4);
		nameLen = Ext2Utils.get8(data, offset + 6);
		type = Ext2Utils.get8(data, offset + 7);

		name = new StringBuffer();
		if (iNodeNr != 0) {
			//XXX character conversion
			for (int i = 0; i < nameLen; i++)
				name.append((char) Ext2Utils.get8(data, offset + 8 + i));
			log.debug("Ext2DirectoryRecord(): iNode=" + iNodeNr + ", name=" + name);
		}

	}

	/**
	 * Returns the fileType.
	 * 
	 * @return short
	 */
	public short getType() {
		return type;
	}

	/**
	 * Returns the iNodeNr.
	 * 
	 * @return long
	 */
	public int getINodeNr() {
		return iNodeNr;
	}

	/**
	 * Returns the name.
	 * 
	 * @return StringBuffer
	 */
	public String getName() {
		return name.toString();
	}
	/**
	 * Returns the recLen.
	 * 
	 * @return int
	 */
	public int getRecLen() {
		return recLen;
	}

}
