/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2004 JNode.org
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
package org.jnode.vm;

import org.vmmagic.pragma.Uninterruptible;

/**
 * This class is used to allocate and free fixed size blocks of memory. This
 * memory is not garbage collected, nor is each block addressable as a java
 * object. Since this manager is used by the Object allocation, do not allocate
 * object in this class.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class MemoryBlockManager extends VmSystemObject implements
		Uninterruptible {

	/** Size of a memory block. */
	static final int BLOCK_SIZE_SHIFT = 16;

	static final int BLOCK_SIZEa = (1 << BLOCK_SIZE_SHIFT);

	/** Inclusive start address of first block */
	private static long startPtr;

	/** Exclusive end address of last block */
	private static long endPtr;

	/** Address of lock to my structures */
	private static Address lockPtr;

	/** Address of usage bitmap */
	private static Address bitmapPtr;

	/** Total number of blocks */
	private static long blockCount;

	/** Has this class be initialized yet */
	private static boolean initialized;

	/** Number of allocated blocks */
	private static long allocatedBlocks;

	/** Number of next allocatable block */
	private static long nextBlockNr;

	/**
	 * Allocate a new block of memory at blockSize large. The actual size is
	 * aligned on BLOCK_SIZE.
	 * 
	 * @param blockSize
	 * @return The address of the start of the block, or null when no memory is
	 *         available.
	 */
	static final Address allocateBlock(int blockSize) {
		if (!initialized) {
			initialize();
		}
		if (false) {
			Unsafe.debug("allocateBlock");
			Unsafe.debug(Unsafe.addressToInt(lockPtr));
		}
		enter();
		try {
			// Calculate the number of blocks needed
			final int reqBlockCount = (int) (blockAlign(blockSize, true) >>> BLOCK_SIZE_SHIFT);
			// Find a large enough series of blocks
			final long nr = findFreeBlocks(reqBlockCount);
			if (nr == -1L) {
				//Unsafe.debug("ret null."); Unsafe.debug(blockSize);
				//Unsafe.debug("allocated blocks");
				// Unsafe.debug(allocatedBlocks);
				//Unsafe.debug("total blocks"); Unsafe.debug(blockCount);
				//Unsafe.getCurrentProcessor().getArchitecture().getStackReader().debugStackTrace();
				//Unsafe.die("allocateBlock");
				return null;
			}
			// Mark all blocks as in use
			for (int i = 0; i < reqBlockCount; i++) {
				setInUse(nr + i, true);
			}
			// Return the address of block "nr".
			allocatedBlocks += reqBlockCount;
			nextBlockNr = nr + reqBlockCount;
			return Unsafe.longToAddress(startPtr + (nr << BLOCK_SIZE_SHIFT));
		} finally {
			exit();
		}
	}

	/**
	 * Free a previously allocated new block of memory at blockSize large. The
	 * actual size is aligned on BLOCK_SIZE.
	 * 
	 * @param ptr
	 *            The block address as returned by allocateBlock .
	 * @param blockSize
	 *            The size of the block as given to allocateBlock.
	 */
	static final void freeBlock(Address ptr, int blockSize) {
		enter();
		try {
			// Calculate the block number
			final long nr = (Unsafe.addressToLong(ptr) - startPtr) >>> BLOCK_SIZE_SHIFT;
			// Calculate the number of blocks
			final long reqBlockCount = blockAlign(blockSize, true) >>> BLOCK_SIZE_SHIFT;
			// Mark all blocks as free
			for (long i = 0; i < reqBlockCount; i++) {
				setInUse(nr + i, false);
			}
			allocatedBlocks -= reqBlockCount;
			nextBlockNr = Math.min(nextBlockNr, nr);
		} finally {
			exit();
		}
	}

	/**
	 * Gets the size of non-allocated memory blocks.
	 * 
	 * @return The free size in bytes
	 */
	public static long getFreeMemory() {
		return (blockCount - allocatedBlocks) << BLOCK_SIZE_SHIFT;
	}

	/**
	 * Find the first free memory block that is following by freeBlockCount-1
	 * free memory blocks.
	 * 
	 * @param freeBlockCount
	 * @return The block number of the first block, or -1 if not found.
	 */
	private static long findFreeBlocks(int freeBlockCount) {
		final long max = blockCount;
		long nr = nextBlockNr;
		while (nr < max) {
			boolean inUse = false;
			int i;
			for (i = 0; (i < freeBlockCount) && (!inUse); i++) {
				inUse |= isInUse(nr + i);
			}
			if (!inUse) {
				// We found it
				return nr;
			} else {
				//Unsafe.debug("nr"); Unsafe.debug(nr);
				//Unsafe.debug("i"); Unsafe.debug(i);
				// We came across an used block
				nr += (i + 1);
			}
		}
		//Unsafe.debug("ret -1"); Unsafe.die();
		return -1;
	}

	/**
	 * Is a block identified by it blockNr [0..blockCount-1] already used.
	 * 
	 * @param blockNr
	 * @return boolean
	 */
	private static final boolean isInUse(long blockNr) {
		final long offset = blockNr >>> 3; // we still need a byte offset
		final int mask = (1 << (blockNr & 7));
		final Address ptr = Unsafe.add(bitmapPtr, Unsafe.longToAddress(offset));
		final int v = Unsafe.getByte(ptr) & 0xFF;
		return ((v & mask) == mask);
	}

	/**
	 * Is a block identified by it blockNr [0..blockCount-1] already used.
	 * 
	 * @param blockNr
	 */
	private static final void setInUse(long blockNr, boolean inUse) {
		final long offset = blockNr >>> 3; // we still need a byte offset
		final int mask = (1 << (blockNr & 7));
		//final int mask = (1 << blockNr);
		final Address ptr = Unsafe.add(bitmapPtr, Unsafe.longToAddress(offset));
		int v = Unsafe.getByte(ptr);
		if (inUse) {
			v |= mask;
		} else {
			v &= ~mask;
		}
		Unsafe.setByte(ptr, (byte) v);
	}

	/**
	 * Initialize this manager.
	 */
	private final static void initialize() {
		Unsafe.debug("initialize.");

		startPtr = blockAlign(Unsafe.addressToLong(Unsafe.getMemoryStart()),
				true);
		endPtr = blockAlign(Unsafe.addressToLong(Unsafe.getMemoryEnd()), false);
		final long size = endPtr - startPtr;
		Unsafe.debug(size);
		blockCount = (size >>> BLOCK_SIZE_SHIFT);
		// Create a lock (4 bytes) and usage bitmap at the front of the memory
		// region
		final long rawBitmapSize = (blockCount >>> 3);
		//final long rawBitmapSize = blockCount;
		final long bitmapSize = blockAlign(4 + rawBitmapSize, true);
		if (false) {
			Unsafe.debug("startPtr:");
			Unsafe.debug(startPtr);
		}
		lockPtr = Unsafe.longToAddress(startPtr);
		if (false) {
			Unsafe.debug("lockPtr:");
			Unsafe.debug(Unsafe.addressToInt(lockPtr));
		}
		bitmapPtr = Unsafe.longToAddress(startPtr + 4);
		// Clear the lock & bitmap size
		clear(lockPtr, bitmapSize);
		// Now shift the startptr.
		startPtr += bitmapSize;
		blockCount -= bitmapSize >>> BLOCK_SIZE_SHIFT;
		allocatedBlocks = 0;
		// Mark as initialized
		initialized = true;

		//Unsafe.debug("BitmapPtr ");
		// Unsafe.debug(Unsafe.addressToLong(bitmapPtr));
		//Unsafe.debug("LockPtr ");
		// Unsafe.debug(Unsafe.addressToLong(lockPtr));
		//Unsafe.debug("StartPtr "); Unsafe.debug(startPtr);
		//Unsafe.debug("BitmapSize "); Unsafe.debug(bitmapSize);
		//Unsafe.debug("Block count "); Unsafe.debug(blockCount);
		//Unsafe.die();
		if (false) {
			Unsafe.debug("end of initialize.");
		}
	}

	/**
	 * Claim access to my structures. This is done without using java
	 * monitorenter/exit instructions, since they may need a memory allocation.
	 */
	private static final void enter() {
		while (!Unsafe.atomicCompareAndSwap(lockPtr, 0, 1)) {
			Unsafe.debug(Unsafe.getInt(lockPtr));
			VmThread.yield();
		}
	}

	/**
	 * Release access to my structures.
	 */
	private static final void exit() {
		Unsafe.setInt(lockPtr, 0);
	}

	/**
	 * Align the given address on the boundary on BLOCK_SIZE.
	 * 
	 * @param ptr
	 * @param roundup
	 * @return The aligned address as long
	 */
	private static long blockAlign(long ptr, boolean roundup) {
		final long blockSizeM1 = BLOCK_SIZEa - 1;
		if (roundup) {
			ptr += blockSizeM1;
		}
		return ptr & ~blockSizeM1;
	}

	/**
	 * Clear a large area of memory. Fill the memory with zeros.
	 * 
	 * @param ptr
	 * @param size
	 */
	private static void clear(Address ptr, long size) {
		//Unsafe.debug("clear");
		//Unsafe.debug(size);
		while (size != 0) {
			final int part = (int) Math.min(size, 0x7fffffffL);
			//Unsafe.debug(size);
			Unsafe.clear(ptr, part);
			ptr = Unsafe.add(ptr, part);
			size -= part;
		}
	}
}