/**
 * $Id$
 */
package org.jnode.fs.fat;

import java.io.IOException;
import java.util.Iterator;

import org.jnode.driver.block.BlockDeviceAPI;

/**
 * <description>
 * 
 * @author epr
 */
public class FatDirectory extends AbstractDirectory {

	private boolean root = false;
	private String label;

	/**
	 * Constructor for Directory.
	 * 
	 * @param fs
	 * @param file
	 */
	public FatDirectory(FatFileSystem fs, FatFile file) throws IOException {
		super(fs, file);
		this.file = file;
		read();
	}

	//  for root
	protected FatDirectory(FatFileSystem fs, int nrEntries) {
		super(fs, nrEntries, null);
		root = true;
	}

	/**
	 * Read the contents of this directory from the persistent storage at the
	 * given offset.
	 */
	protected synchronized void read() throws IOException {
		entries.setSize((int)file.getLengthOnDisk() / 32);
		final byte[] data = new byte[entries.size() * 32];
		file.read(0, data, 0, data.length);
		read(data);
		resetDirty();
	}

	/**
	 * Write the contents of this directory to the given persistent storage at
	 * the given offset.
	 */
	protected synchronized void write() throws IOException {
		if (label != null)
			applyLabel();
		final byte[] data = new byte[entries.size() * 32];
		if (canChangeSize(entries.size())) {
			file.setLength(data.length);
		}
		write(data);
		file.write(0, data, 0, data.length);
		resetDirty();
	}

	public synchronized void read(BlockDeviceAPI device, long offset) throws IOException {
		byte[] data = new byte[entries.size() * 32];
		device.read(offset, data, 0, data.length);
      //System.out.println("Directory at offset :" + offset);
      //System.out.println("Length in bytes = " + entries.size() * 32);
      read(data);
		resetDirty();
	}

	public synchronized void write(BlockDeviceAPI device, long offset) throws IOException {
		if (label != null)
			applyLabel();
		final byte[] data = new byte[entries.size() * 32];
		write(data);
		device.write(offset, data, 0, data.length);
		resetDirty();
	}

	/**
	 * Flush the contents of this directory to the persistent storage
	 */
	protected void flush() throws IOException {
		if (root) {
			final FatFileSystem fs = (FatFileSystem)getFileSystem();
			if (fs != null) {
				long offset = FatUtils.getRootDirOffset(fs.getBootSector());
				write(fs.getBlockDeviceAPI(), offset);
			}
		} else {
			write();
		}
	}

	/**
	 * @see org.jnode.fs.fat.AbstractDirectory#canChangeSize(int)
	 */
	protected boolean canChangeSize(int newSize) {
		return !root;
	}

	/**
	 * Set the label
	 * 
	 * @param label
	 */
	public void setLabel(String label) throws IOException {
		if (!root)
			throw new IOException("You can change the volume name on a non root directory");
		this.label = label;
	}

	private void applyLabel() throws IOException {
		FatDirEntry labelEntry = null;
		Iterator i = iterator();
		FatDirEntry current;
		while (labelEntry == null && i.hasNext()) {
			current = (FatDirEntry)i.next();
			if (current.isLabel() && !(current.isHidden() && current.isReadonly() && current.isSystem())) {
				labelEntry = current;
			}
		}
		if (labelEntry == null) {
			labelEntry = addFatFile(label);
			labelEntry.setLabel();
		}
		labelEntry.setName(label);
		if (label.length() > 8) {
			labelEntry.setExt(label.substring(8));
		} else {
			labelEntry.setExt("");
		}
	}
}
