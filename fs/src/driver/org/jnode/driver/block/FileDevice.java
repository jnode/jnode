/**
 * $Id$
 */
package org.jnode.driver.block;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.jnode.driver.Device;

/**
 * <description>
 * 
 * @author epr
 */
public class FileDevice extends Device implements BlockDeviceAPI {

	private RandomAccessFile raf;

	public FileDevice(File file, String mode) throws FileNotFoundException, IOException {
		super(null, "file" + System.currentTimeMillis());
		raf = new RandomAccessFile(file, mode);
		registerAPI(BlockDeviceAPI.class, this);
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
	 * @throws IOException
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
}
