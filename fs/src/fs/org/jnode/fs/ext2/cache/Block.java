package org.jnode.fs.ext2.cache;

import org.apache.log4j.Logger;

/**
 * @author Andras Nagy
 */
public class Block {
	private static final Logger log = Logger.getLogger(Block.class);
	byte[] data;
	boolean dirty = false;

	public Block(byte[] data) {
		this.data = data;
	}
	/**
	 * Returns the data.
	 * 
	 * @return byte[]
	 */
	public byte[] getData() {
		return data;
	}

	/**
	 * Sets the data.
	 * 
	 * @param data
	 *           The data to set
	 */
	public void setData(byte[] data) {
		this.data = data;
		dirty = true;
	}

	/**
	 * flush is called when the block is removed from the cache
	 */
	public void flush() {
		if (!dirty)
			return;
		//XXX...
		log.error("BLOCK FLUSHED FROM CACHE");
	}

}
