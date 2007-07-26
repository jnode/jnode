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
 
package org.jnode.vm;

import org.jnode.vm.annotation.MagicPermission;
import org.jnode.vm.annotation.Uninterruptible;
import org.jnode.vm.annotation.SharedStatics;
import org.jnode.vm.scheduler.VmProcessor;
import org.jnode.vm.scheduler.VmThread;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Extent;
import org.vmmagic.unboxed.Word;

/**
 * This class is used to allocate and free fixed size blocks of memory. This
 * memory is not garbage collected, nor is each block addressable as a java
 * object. Since this manager is used by the Object allocation, do not allocate
 * object in this class.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
@MagicPermission
@Uninterruptible
@SharedStatics
public final class MemoryBlockManager extends VmSystemObject {

	/** Size of a memory block. */
	static final int BLOCK_SIZE_SHIFT = 16;

	static final int BLOCK_SIZEa = (1 << BLOCK_SIZE_SHIFT);

	/** Inclusive start address of first block */
	private static Address startPtr;

	/** Exclusive end address of last block */
	private static Address endPtr;

	/** Address of lock to my structures */
	private static Address lockPtr;

	/** Address of usage bitmap */
	private static Address bitmapPtr;

	/** Total number of blocks */
	private static Word blockCount;

	/** Has this class be initialized yet */
	private static boolean initialized;

	/** Number of allocated blocks */
	private static Word allocatedBlocks;

	/** Number of next allocatable block */
	private static Word nextBlockNr;

	/**
	 * Allocate a new block of memory at blockSize large. The actual size is
	 * aligned on BLOCK_SIZE.
	 * 
	 * @param blockSize
	 * @return The address of the start of the block, or null when no memory is
	 *         available.
	 */
	static final Address allocateBlock(Extent blockSize) {
		if (!initialized) {
			initialize();
		}
		if (false) {
			Unsafe.debug("allocateBlock");
			Unsafe.debug(lockPtr.toInt());
		}
		enter();
		try {
			// Calculate the number of blocks needed
			final Word reqBlockCount = blockAlign(blockSize.toWord(), true).rshl(BLOCK_SIZE_SHIFT);
			// Find a large enough series of blocks
			final Word nr = findFreeBlocks(reqBlockCount);
			if (nr.isMax()) {
				Unsafe.debug("ret null."); Unsafe.debug(blockSize.toInt());
				// Unsafe.debug("allocated blocks");
				// Unsafe.debug(allocatedBlocks);
				// Unsafe.debug("total blocks"); Unsafe.debug(blockCount);
                VmProcessor.current().getArchitecture().getStackReader().debugStackTrace();
				// Unsafe.die("allocateBlock");
				return null;
			}
			// Mark all blocks as in use
			for (Word i = Word.zero(); i.LT(reqBlockCount); i = i.add(1)) {
				setInUse(nr.add(i), true);
			}
			// Return the address of block "nr".
			allocatedBlocks = allocatedBlocks.add(reqBlockCount);
			nextBlockNr = nr.add(reqBlockCount);
			return startPtr.add(nr.lsh(BLOCK_SIZE_SHIFT));
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
	static final void freeBlock(Address ptr, Extent blockSize) {
		enter();
		try {
			// Calculate the block number
			final Word nr = ptr.toWord().sub(startPtr.toWord()).rshl(BLOCK_SIZE_SHIFT);
			// Calculate the number of blocks
			final Word reqBlockCount = blockAlign(blockSize.toWord(), true).rshl(BLOCK_SIZE_SHIFT);
			// Mark all blocks as free
			for (Word i = Word.zero(); i.LT(reqBlockCount); i = i.add(1)) {
				setInUse(nr.add(i), false);
			}
			allocatedBlocks = allocatedBlocks.sub(reqBlockCount);
			if (nr.LT(nextBlockNr)) {
				nextBlockNr = nr;
			}
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
		return blockCount.sub(allocatedBlocks).lsh(BLOCK_SIZE_SHIFT).toLong();
	}

	/**
	 * Find the first free memory block that is following by freeBlockCount-1
	 * free memory blocks.
	 * 
	 * @param freeBlockCount
	 * @return The block number of the first block, or Word.max() if not found.
	 */
	private static Word findFreeBlocks(Word freeBlockCount) {
		final Word max = blockCount;
		Word nr = nextBlockNr;
		while (nr.LT(max)) {
			boolean inUse = false;
			Word i;
			for (i = Word.zero(); i.LT(freeBlockCount) && (!inUse); i = i.add(1)) {
				inUse |= isInUse(nr.add(i));
			}
			if (!inUse) {
				// We found it
				return nr;
			} else {
				// Unsafe.debug("nr"); Unsafe.debug(nr);
				// Unsafe.debug("i"); Unsafe.debug(i);
				// We came across an used block
				nr = nr.add(i).add(1);
			}
		}
		// Unsafe.debug("ret -1"); Unsafe.die();
		return Word.max();
	}

	/**
	 * Is a block identified by it blockNr [0..blockCount-1] already used.
	 * 
	 * @param blockNr
	 * @return boolean
	 */
	private static final boolean isInUse(Word blockNr) {
		final Word offset = blockNr.rshl(3); // we still need a byte offset
		final int mask = (1 << blockNr.and(Word.fromIntZeroExtend(7)).toInt());
		final Address ptr = bitmapPtr.add(offset);
		final int v = ptr.loadByte() & 0xFF;
		return ((v & mask) == mask);
	}

	/**
	 * Is a block identified by it blockNr [0..blockCount-1] already used.
	 * 
	 * @param blockNr
	 */
	private static final void setInUse(Word blockNr, boolean inUse) {
		final Word offset = blockNr.rshl(3); // we still need a byte offset
		final int mask = (1 << blockNr.and(Word.fromIntZeroExtend(7)).toInt());
		// final int mask = (1 << blockNr);
		final Address ptr = bitmapPtr.add(offset);
		int v = ptr.loadByte();
		if (inUse) {
			v |= mask;
		} else {
			v &= ~mask;
		}
		ptr.store((byte) v);
	}

	/**
	 * Initialize this manager.
	 */
	private final static void initialize() {
		Unsafe.debug("Initialize MemoryBlockManager\n");

		startPtr = blockAlign(Unsafe.getMemoryStart().toWord(), true).toAddress();
		endPtr = blockAlign(Unsafe.getMemoryEnd().toWord(), false).toAddress();
		
        Unsafe.debug("Start end: ");
		Unsafe.debug(startPtr);
		Unsafe.debug(endPtr);
        Unsafe.debug('\n');
		
		final Extent size = endPtr.toWord().sub(startPtr.toWord()).toExtent();
        Unsafe.debug("Size     : ");
		Unsafe.debug(size);
        Unsafe.debug('\n');

        blockCount = size.toWord().rshl(BLOCK_SIZE_SHIFT);
		// Create a lock (4 bytes) and usage bitmap at the front of the memory
		// region
		final Extent rawBitmapSize = blockCount.rshl(3).toExtent();
		// final long rawBitmapSize = blockCount;
		final Extent bitmapSize = blockAlign(rawBitmapSize.toWord().add(4), true).toExtent();
		if (false) {
			Unsafe.debug("startPtr:");
			Unsafe.debug(startPtr);
		}
		lockPtr = startPtr;
		if (false) {
			Unsafe.debug("lockPtr:");
			Unsafe.debug(lockPtr);
			Unsafe.debug("bitmapSize:");
			Unsafe.debug(bitmapSize);
		}
		bitmapPtr = startPtr.add(4);
		// Clear the lock & bitmap size
		clear(lockPtr, bitmapSize);
		// Now shift the startptr.
		startPtr = startPtr.add(bitmapSize);
		blockCount = blockCount.sub(bitmapSize.toWord().rshl(BLOCK_SIZE_SHIFT));
		allocatedBlocks = Word.zero();
		// Mark as initialized
		initialized = true;

		// Unsafe.debug("BitmapPtr ");
		// Unsafe.debug(Unsafe.addressToLong(bitmapPtr));
		// Unsafe.debug("LockPtr ");
		// Unsafe.debug(Unsafe.addressToLong(lockPtr));
		// Unsafe.debug("StartPtr "); Unsafe.debug(startPtr);
		// Unsafe.debug("BitmapSize "); Unsafe.debug(bitmapSize);
		// Unsafe.debug("Block count "); Unsafe.debug(blockCount);
		// Unsafe.die();
		if (true) {
			Unsafe.debug("end of initialize.");
		}
        Unsafe.debug('\n');
	}

	/**
	 * Claim access to my structures. This is done without using java
	 * monitorenter/exit instructions, since they may need a memory allocation.
	 */
	private static final void enter() {
		while (!lockPtr.attempt(0, 1)) {
			Unsafe.debug(lockPtr.loadInt());
			VmThread.yield();
		}
	}

	/**
	 * Release access to my structures.
	 */
	private static final void exit() {
		lockPtr.store((int)0);
	}

	/**
	 * Align the given address on the boundary on BLOCK_SIZE.
	 * 
	 * @param ptr
	 * @param roundup
	 * @return The aligned address as long
	 */
	private static Word blockAlign(Word ptr, boolean roundup) {
		final Word blockSizeM1 = Word.fromIntSignExtend(BLOCK_SIZEa - 1);
		if (roundup) {
			ptr = ptr.add(blockSizeM1);
		}
		return ptr.and(blockSizeM1.not());
	}

	/**
	 * Clear a large area of memory. Fill the memory with zeros.
	 * 
	 * @param ptr
	 * @param size
	 */
	private static void clear(Address ptr, Extent size) {
		Unsafe.clear(ptr, size);
	}
}
