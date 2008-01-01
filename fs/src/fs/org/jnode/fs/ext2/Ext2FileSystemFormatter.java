/*
 * $Id: Ext2FileSystemType.java 3530 2007-09-26 20:16:47Z konkubinaten $
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

package org.jnode.fs.ext2;

import org.jnode.driver.Device;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.Formatter;
import org.jnode.util.BinaryPrefix;

/**
 * @author Andras Nagy
 */
public class Ext2FileSystemFormatter extends Formatter<Ext2FileSystem> {
    private BlockSize blockSize;

    /**
     *
     * @param blockSizeKb size of blocks in KB
     */
    public Ext2FileSystemFormatter(BlockSize blockSize)
    {
    	super(new Ext2FileSystemType());
    	this.blockSize = blockSize;
    }

	/**
	 *
	 */
	public synchronized Ext2FileSystem format(Device device) throws FileSystemException {
		Ext2FileSystem fs = new Ext2FileSystem(device, false);
		fs.create(blockSize);
		return fs;
	}
}
