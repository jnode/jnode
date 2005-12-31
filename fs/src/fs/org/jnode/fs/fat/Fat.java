/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2006 JNode.org
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
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.fs.FileSystemFullException;

/**
 * <description>
 * 
 * @author epr
 */
public class Fat {

	public static final int FAT12 = 12;
	public static final int FAT16 = 16;
	public static final int FAT32 = 32;

	private long[] entries;
	/** The type of FAT, legal values are: 12, 16, 32 */
	private int bitSize;
	/** The number of sectors this fat takes */
	private int nrSectors;
	/** The number of bytes/sector */
	private int sectorSize;

	private final long eofMarker;
	private boolean dirty;
	
	/** entry index for find next free entry*/
	private int lastFreeCluster = 2;

	/**
	 * Create a new instance
	 * 
	 * @param bitSize
	 * @param nrSectors
	 * @param sectorSize
	 */
	public Fat(int bitSize, int mediumDescriptor, int nrSectors, int sectorSize) {
		this.bitSize = bitSize;
		this.nrSectors = nrSectors;
		this.sectorSize = sectorSize;
		this.dirty = false;
		switch (bitSize) {
			case FAT12 :
				{
					entries = new long[(int) ((nrSectors * sectorSize) / 1.5)];
					eofMarker = 0xFFF;
				}
				break;
			case FAT16 :
				{
					entries = new long[(nrSectors * sectorSize) / 2];
					eofMarker = 0xFFFF;
				}
				break;
			case FAT32 :
				{
					entries = new long[(nrSectors * sectorSize) / 4];
					eofMarker = 0xFFFFFFFF;
				}
				break;
			default :
				throw new IllegalArgumentException("Invalid bitSize " + bitSize);
		}
		entries[0] = (mediumDescriptor & 0xFF) | 0xFFFFFF00;
	}

	/**
	 * Read the contents of this FAT from the given device at the given offset.
	 * 
	 * @param device
	 */
	public synchronized void read(BlockDeviceAPI device, long offset) throws IOException {
        byte[] data = new byte[nrSectors * sectorSize];
		device.read(offset, ByteBuffer.wrap(data));
		for (int i = 0; i < entries.length; i++) {
			switch (bitSize) {
				case 12 :
					{
						int idx = (int) (i * 1.5);
						int b1 = data[idx] & 0xFF;
						int b2 = data[idx + 1] & 0xFF;
						int v = (b2 << 8) | b1;
						if ((i % 2) == 0) {
							entries[i] = v & 0xFFF;
						} else {
							entries[i] = v >> 4;
						}
						//System.out.println("i=" + i + ", idx=" + idx + ", b1=0x" +
						// Integer.toHexString(b1) + ", b2=0x" +
						// Integer.toHexString(b2) + ", v=0x" +
						// Integer.toHexString(v) + " -> 0x" +
						// Long.toHexString(entries[i]));
					}
					break;
				case 16 :
					{
						int idx = i * 2;
						int b1 = data[idx] & 0xFF;
						int b2 = data[idx + 1] & 0xFF;
						entries[i] = (b2 << 8) | b1;
					}
					break;
				case 32 :
					{
						int idx = i * 4;
						long b1 = data[idx] & 0xFF;
						long b2 = data[idx + 1] & 0xFF;
						long b3 = data[idx + 2] & 0xFF;
						long b4 = data[idx + 3] & 0xFF;
						entries[i] = (b4 << 24) | (b3 << 16) | (b2 << 8) | b1;
					}
					break;
			}
		}
		this.dirty = false;
	}

	/**
	 * Write the contents of this FAT to the given device at the given offset.
	 * 
	 * @param device
	 */
	public synchronized void write(BlockDeviceAPI device, long offset) throws IOException {
		byte[] data = new byte[nrSectors * sectorSize];
		for (int i = 0; i < entries.length; i++) {
			long v = entries[i];
			switch (bitSize) {
				case 12 :
					{
						int idx = (int) (i * 1.5);
						if ((i % 2) == 0) {
							data[idx] = (byte) (v & 0xFF);
							data[idx + 1] = (byte) ((v >> 8) & 0x0F);
						} else {
							data[idx] |= (byte) ((v & 0x0F) << 4);
							data[idx + 1] = (byte) ((v >> 4) & 0xFF);
						}
					}
					break;
				case 16 :
					{
						int idx = i << 1;
						data[idx] = (byte) (v & 0xFF);
						data[idx + 1] = (byte) ((v >> 8) & 0xFF);
					}
					break;
				case 32 :
					{
						int idx = i << 2;
						data[idx] = (byte) (v & 0xFF);
						data[idx + 1] = (byte) ((v >> 8) & 0xFF);
						data[idx + 2] = (byte) ((v >> 16) & 0xFF);
						data[idx + 3] = (byte) ((v >> 24) & 0xFF);
					}
					break;
			}

		}
		device.write(offset, ByteBuffer.wrap(data));
		this.dirty = false;
	}

	/**
	 * Gets the medium descriptor byte
	 * 
	 * @return int
	 */
	public int getMediumDescriptor() {
		return (int) (entries[0] & 0xFF);
	}

	/**
	 * Sets the medium descriptor byte
	 */
	public void setMediumDescriptor(int descr) {
		entries[0] = 0xFFFFFF00 | (descr & 0xFF);
	}

	/**
	 * Gets the number of entries of this fat
	 * 
	 * @return int
	 */
	public int getNrEntries() {
		return entries.length;
	}

	/**
	 * Gets the entry at a given offset
	 * 
	 * @param index
	 * @return long
	 */
	public long getEntry(int index) {
		return entries[index];
	}

	public synchronized long[] getChain(long startCluster) {
		testCluster(startCluster);
		// Count the chain first
		int count = 1;
		long cluster = startCluster;
		while (!isEofCluster(entries[(int)cluster])) {
			count++;
			cluster = entries[(int)cluster];
		}
		// Now create the chain
		long[] chain = new long[count];
		chain[0] = startCluster;
		cluster = startCluster;
		int i = 0;
		while (!isEofCluster(entries[(int)cluster])) {
			cluster = entries[(int)cluster];
			chain[++i] = cluster;
		}
		return chain;
	}

	/**
	 * Gets the cluster after the given cluster
	 * 
	 * @param cluster
	 * @return long The next cluster number or -1 which means eof.
	 */
	public synchronized long getNextCluster(long cluster) {
		testCluster(cluster);
		long entry = entries[(int)cluster];
		if (isEofCluster(entry)) {
			return -1;
		} else {
			return entry;
		}
	}

	/**
	 * Allocate a cluster for a new file
	 * 
	 * @return long
	 */
	public synchronized long allocNew() throws IOException {

		int i;
		int entryIndex = -1;
		
		for (i = lastFreeCluster; i < entries.length; i++) {
			if (isFreeCluster(entries[i])) {
				entryIndex=i;
				break;
			} 
		}
		if(entryIndex<0){	
			for(i = 2; i < lastFreeCluster;i++){
				if (isFreeCluster(entries[i])) {
					entryIndex=i;
					break;
				} 			 
			}
		}
		if(entryIndex < 0){
			throw new FileSystemFullException("FAT Full (" + entries.length + ", " + i + ")");
		}
		entries[entryIndex] = eofMarker;
		lastFreeCluster = entryIndex+1;
		this.dirty      = true;

		return entryIndex;
		
	}

	/**
	 * Allocate a series of clusters for a new file
	 * 
	 * @return long
	 */
	public synchronized long[] allocNew(int nrClusters) throws IOException {

		long rc[] = new long[nrClusters];

		rc[0] = allocNew();
		for (int i = 1; i < nrClusters; i++) {
			rc[i] = allocAppend(rc[i - 1]);
		}

		return rc;
	}

	/**
	 * Allocate a cluster to append to a new file
	 * 
	 * @return long
	 */
	public synchronized long allocAppend(long cluster) throws IOException {

		testCluster(cluster);

		while (!isEofCluster(entries[(int)cluster])) {
			cluster = entries[(int)cluster];
		}

		long newCluster = allocNew();
		entries[(int)cluster] = newCluster;

		return newCluster;
	}

	public synchronized void setEof(long cluster) {
		testCluster(cluster);
		entries[(int)cluster] = eofMarker;
	}

	public synchronized void setFree(long cluster) {
		testCluster(cluster);
		entries[(int)cluster] = 0;
	}

	/**
	 * Print the contents of this FAT to the given writer. Used for debugging
	 * purposes.
	 * 
	 * @param out
	 */
	public void printTo(PrintWriter out) {
		int freeCount = 0;
		out.println("medium descriptor 0x" + Integer.toHexString(getMediumDescriptor()));
		for (int i = 2; i < entries.length; i++) {
			long v = entries[i];
			if (isFreeCluster(v)) {
				freeCount++;
			} else {
				out.print("0x" + Integer.toHexString(i) + " -> ");
				if (isEofCluster(v)) {
					out.println("eof");
				} else if (isReservedCluster(v)) {
					out.println("reserved");
				} else {
					out.println("0x" + Long.toHexString(v));
				}
			}
		}
		out.println("Nr free entries " + freeCount);
	}

	/**
	 * Compare this Fat with another Fat.
	 */
	public boolean equals(Object other) {
		if (other instanceof Fat) {
			Fat of = (Fat)other;
			return Arrays.equals(entries, of.entries);
		} else {
			return false;
		}
	}

	/**
	 * Is the given entry a free cluster?
	 * 
	 * @param entry
	 * @return boolean
	 */
	protected boolean isFreeCluster(long entry) {
		return (entry == 0);
	}

	/**
	 * Is the given entry a reserved cluster?
	 * 
	 * @param entry
	 * @return boolean
	 */
	protected boolean isReservedCluster(long entry) {
		switch (bitSize) {
			case 12 :
				return ((entry >= 0xFF0) && (entry <= 0xFF6));
			case 16 :
				return ((entry >= 0xFFF0) && (entry <= 0xFFF6));
			case 32 :
				return ((entry >= 0xFFFFFFF0) && (entry <= 0xFFFFFFF6));
			default :
				throw new IllegalArgumentException("Invalid bitSize " + bitSize);
		}
	}

	/**
	 * Is the given entry an EOF marker
	 * 
	 * @param entry
	 * @return boolean
	 */
	protected boolean isEofCluster(long entry) {
		switch (bitSize) {
			case 12 :
				return (entry >=0xFF8); 
			case 16 :
				return ((entry >= 0xFFF8));  
			case 32 :
				return (entry >= 0xFFFFFFF8); 
			default :
				throw new IllegalArgumentException("Invalid bitSize " + bitSize);
		}
	}

	protected void testCluster(long cluster) throws IllegalArgumentException {
		if ((cluster < 2) || (cluster >= entries.length)) {
			throw new IllegalArgumentException("Invalid cluster value");
		}
	}

	/**
	 * Returns the dirty.
	 * 
	 * @return boolean
	 */
	public boolean isDirty() {
		return dirty;
	}

}
