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
 
package org.jnode.fs.fat;

import java.io.IOException;

import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.fs.FSEntryIterator;

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
	public void flush() throws IOException {
		if (root) {
			final FatFileSystem fs = (FatFileSystem)getFileSystem();
			if (fs != null) {
				long offset = FatUtils.getRootDirOffset(fs.getBootSector());
				write(fs.getApi(), offset);
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
		FSEntryIterator i = iterator();
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
