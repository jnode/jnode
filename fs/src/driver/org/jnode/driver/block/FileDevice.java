/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.driver.block;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.jnode.driver.Device;
import org.jnode.fs.partitions.PartitionTableEntry;

/**
 * <description>
 * 
 * @author epr
 */
public class FileDevice extends Device implements FSBlockDeviceAPI {

	private RandomAccessFile raf;

	public FileDevice(File file, String mode) throws FileNotFoundException, IOException {
		super(null, "file" + System.currentTimeMillis());
		raf = new RandomAccessFile(file, mode);
		//registerAPI(BlockDeviceAPI.class, this);
		registerAPI(FSBlockDeviceAPI.class, this);
	}

	/**
	 * @see org.jnode.driver.block.BlockDeviceAPI#getLength()
	 * @return The length
	 * @throws IOException
	 */
	public long getLength() throws IOException {
		return raf.length();
	}

	/**
	 * @param devOffset
	 * @param dest
	 * @param destOffset
	 * @param length
	 * @see org.jnode.driver.block.BlockDeviceAPI#read(long, byte[], int, int)
	 * @throws IOException
	 */
	public void read(long devOffset, byte[] dest, int destOffset, int length) throws IOException {
		raf.seek(devOffset);
		raf.read(dest, destOffset, length);
	}

	/**
	 * @param devOffset
	 * @param src
	 * @param srcOffset
	 * @param length
	 * @see org.jnode.driver.block.BlockDeviceAPI#write(long, byte[], int, int)
	 * @throws IOException
	 */
	public void write(long devOffset, byte[] src, int srcOffset, int length) throws IOException {
		//		log.debug("fd.write devOffset=" + devOffset + ", length=" + length);
		raf.seek(devOffset);
		raf.write(src, srcOffset, length);
	}
	/**
	 * @see org.jnode.driver.block.BlockDeviceAPI#flush()
	 */
	public void flush() {
		// Nothing to flush
	}

	public void setLength(long length) throws IOException {
		raf.setLength(length);
	}

	public void close() throws IOException {
		raf.close();
	}
    /**
     * @see org.jnode.driver.block.FSBlockDeviceAPI#getPartitionTableEntry()
     */
    public PartitionTableEntry getPartitionTableEntry() {
        return null;
    }
    
    /**
     * @see org.jnode.driver.block.FSBlockDeviceAPI#getSectorSize()
     */
    public int getSectorSize() throws IOException {
        return 512;
    }
}
