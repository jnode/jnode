/*
 * $Id$
 */
package org.jnode.vm;

/**
 * This class is used to allocate and free fixed size blocks of memory. This memory is not garbage
 * collected, nor is each block addressable as a java object. Since this manager is used by the
 * Object allocation, do not allocate object in this class.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class MemoryBlockManager extends VmSystemObject implements Uninterruptible {

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
	 * Allocate a new block of memory at blockSize large. The actual size is aligned on BLOCK_SIZE.
	 * 
	 * @param blockSize
	 * @return The address of the start of the block, or null when no memory is available.
	 */
	static final Address allocateBlock(int blockSize) {
		if (!initialized) {
			initialize();
		}
		enter();
		try {
			// Calculate the number of blocks needed
			final int reqBlockCount = (int)(blockAlign(blockSize, true) >>> BLOCK_SIZE_SHIFT);
			// Find a large enough series of blocks
			final long nr = findFreeBlocks(reqBlockCount);
			if (nr == -1L) {
				Unsafe.debug("ret null."); Unsafe.debug(blockSize);
				Unsafe.debug("allocated blocks"); Unsafe.debug(allocatedBlocks);
				Unsafe.die();
				return null;
			}
			// Mark all blocks as in use
			for (long i = 0; i < reqBlockCount; i++) {
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
	 * Free a previously allocated new block of memory at blockSize large. 
	 * The actual size is aligned on BLOCK_SIZE.
	 *
	 * @param ptr The block address as returned by allocateBlock .
	 * @param blockSize The size of the block as given to allocateBlock.
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
	 * @return The free size in bytes
	 */
	static long getFreeMemory() {
		return (blockCount - allocatedBlocks) << BLOCK_SIZE_SHIFT;
	}

	/**
	 * Find the first free memory block that is following by freeBlockCount-1 free memory blocks.
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
				// We came across an used block
				nr += (i + 1);
			}
		}
		return -1;
	}

	/**
	 * Is a block identified by it blockNr [0..blockCount-1] already used.
	 * 
	 * @param blockNr
	 * @return boolean
	 */
	private static final boolean isInUse(long blockNr) {
		// 32-bits per int, so shift=5, mask=31
		final long offset = blockNr >>> 5;
		final int mask = (1 << (int) (blockNr & 31));
		final Address ptr = Unsafe.add(bitmapPtr, Unsafe.longToAddress(offset));
		final int v = Unsafe.getInt(ptr);
		return ((v & mask) == mask);
	}

	/**
	 * Is a block identified by it blockNr [0..blockCount-1] already used.
	 * 
	 * @param blockNr
	 * @return boolean
	 */
	private static final void setInUse(long blockNr, boolean inUse) {
		// 32-bits per int, so shift=5, mask=31
		final long offset = blockNr >>> 5;
		final int mask = (1 << (int) (blockNr & 31));
		final Address ptr = Unsafe.add(bitmapPtr, Unsafe.longToAddress(offset));
		int v = Unsafe.getInt(ptr);
		if (inUse) {
			v |= mask;
		} else {
			v &= ~mask;
		}
		Unsafe.setInt(ptr, v);
	}

	/**
	 * Initialize this manager.
	 */
	private final static void initialize() {
		Unsafe.debug("initialize.");
		
		startPtr = blockAlign(Unsafe.addressToLong(Unsafe.getMemoryStart()), true);
		endPtr = blockAlign(Unsafe.addressToLong(Unsafe.getMemoryEnd()), false);
		final long size = endPtr - startPtr;
		Unsafe.debug(size);
		blockCount = (size >>> BLOCK_SIZE_SHIFT);
		// Create a lock (4 bytes) and usage bitmap at the front of the memory region
		final long rawBitmapSize = (blockCount >>> 3);
		final long bitmapSize = blockAlign(4 + rawBitmapSize, true);
		lockPtr = Unsafe.longToAddress(startPtr);
		bitmapPtr = Unsafe.longToAddress(startPtr + 4);
		// Clear the lock & bitmap size
		clear(lockPtr, bitmapSize);
		// Now shift the startptr.
		startPtr += bitmapSize;
		blockCount -= bitmapSize >>> BLOCK_SIZE_SHIFT;
		allocatedBlocks = 0;
		// Mark as initialized
		initialized = true;
	
		Unsafe.debug("Block count ");
		Unsafe.debug(blockCount);
		//Unsafe.debug("end of initialize.");
	}

	/**
	 * Claim access to my structures. This is done without using java monitorenter/exit
	 * instructions, since they may need a memory allocation.
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
	 * @param ptr
	 * @param size
	 */
	private static void clear(Address ptr, long size) {
		Unsafe.debug("clear");
		Unsafe.debug(size);
		while (size != 0) {
			final int part = (int)Math.min(size, 0x7fffffffL);
			Unsafe.debug(size);
			Unsafe.clear(ptr, part);
			ptr = Unsafe.add(ptr, part);
			size -= part;
		}
	}
}
