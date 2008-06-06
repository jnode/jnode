/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
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
    boolean dirty = false;
    protected Ext2FileSystem fs;
    protected long blockNr;

    public Block(Ext2FileSystem fs, long blockNr, byte[] data) {
        this.data = data;
        this.fs = fs;
        this.blockNr = blockNr;
        log.setLevel(Level.DEBUG);
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
     * @param data The data to set
     */
    public void setData(byte[] data) {
        this.data = data;
        dirty = true;
    }

    /**
     * flush is called when the block is being removed from the cache
     */
    public void flush() throws IOException {
        if (dirty) {
            fs.writeBlock(blockNr, data, true);
            log.debug("BLOCK FLUSHED FROM CACHE");
        }
    }

    /**
     * Get the dirty flag.
     * 
     * @return the dirty flag
     */
    public boolean isDirty() {
        return dirty;
    }

    /**
     * Set the dirty flag.
     * @param b 
     */
    public void setDirty(boolean b) {
        dirty = b;
    }

}
