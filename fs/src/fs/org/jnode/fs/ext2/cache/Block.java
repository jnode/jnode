/*
 * $Id$
 */
package org.jnode.fs.ext2.cache;

import java.io.IOException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jnode.fs.ext2.Ext2FileSystem;

/**
 * @author Andras Nagy
 */
public class Block {
	private final Logger log = Logger.getLogger(getClass());

	protected byte[] data;
	boolean dirty=false;
	protected Ext2FileSystem fs;
	protected long blockNr;
	
	public Block(Ext2FileSystem fs, long blockNr, byte[] data) {
		this.data=data;
		this.fs=fs;
		this.blockNr=blockNr;
		log.setLevel(Level.DEBUG);
	}
	/**
	 * Returns the data.
	 * @return byte[]
	 */
	public byte[] getData() {
		return data;
	}

	/**
	 * Sets the data.
	 * @param data The data to set
	 */
	public void setData(byte[] data) {
		this.data = data;
		dirty=true;
	}
	
	/**
	 * flush is called when the block is removed from the cache
	 */
	public void flush() throws IOException{
		if(!dirty)
			return;
		fs.writeBlock(blockNr, data, true);
		log.debug("BLOCK FLUSHED FROM CACHE");
	}

	/**
	 * Get the dirty flag.
	 * @return the dirty flag
	 */
	public boolean isDirty() {
		return dirty;
	}

	/**
	 * Set the dirty flag.
	 * @param boolean
	 */
	public void setDirty(boolean b) {
		dirty = b;
	}

}
