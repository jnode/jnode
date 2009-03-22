/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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
import org.jnode.vm.scheduler.VmThread;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Extent;
import org.vmmagic.unboxed.Word;

/**
 * A bitmap that stores its bits in a raw memory region.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
@MagicPermission
@Uninterruptible
public class AllocationBitmap {

    /**
     * Inclusive start address of the bitmap memory region
     */
    private Address start = Address.zero();

    /**
     * Exclusive end address of the bitmap memory region
     */
    private Address end = Address.zero();

    /**
     * Number of bits in the bitmap
     */
    private Word bits;

    /**
     * Lock address for access to the bits in this bitmap
     */
    private Address lock;

    /**
     * Address of bits data
     */
    private Address bitmap;

    /**
     * Size of the bitmap memory region
     */
    private Extent size;

    /**
     * Number of allocated bits
     */
    private Word allocatedBits = Word.zero();

    /**
     * Number of next allocatable bit
     */
    private Word nextBitNr = Word.zero();

    /**
     * Initialize this instance.
     */
    public AllocationBitmap() {
        // Nothing here
    }

    /**
     * Initialize this bitmap.
     *
     * @param start
     * @param bits
     */
    public final void initialize(Address start, Word bits) {
        // Size of the lock (we only use an int)
        final Extent lockSize = Extent.fromIntZeroExtend(Vm.getArch()
            .getReferenceSize());

        // Create a lock and actual bitmap
        final Extent rawBitmapSize = bits.rshl(3).toExtent();
        final Extent bitmapSize = rawBitmapSize.toWord().add(lockSize)
            .toExtent();

        this.start = start;
        this.end = start.add(bitmapSize);
        this.bits = bits;
        this.lock = start;
        this.bitmap = start.add(lockSize);
        this.size = bitmapSize;
        this.allocatedBits = Word.zero();
        this.nextBitNr = Word.zero();

        // Clear the total bitmap + lock region
        Unsafe.clear(start, bitmapSize);
    }

    /**
     * Gets the size of the memory region occupied by this bitmap.
     *
     * @return the size as an Extent.
     */
    public final Extent getSize() {
        return size;
    }

    /**
     * Allocate a new series of bits.
     *
     * @param noBits
     * @return The bit index of the start of the bit series, or Word.max() when
     *         not enough free bits are available.
     */
    public final Word allocateBits(Word noBits) {
        lock();
        try {
            // Find a large enough series of bits
            final Word nr = findFreeBits(noBits);
            if (nr.isMax()) {
                return nr;
            }
            // Mark all blocks as in use
            for (Word i = Word.zero(); i.LT(noBits); i = i.add(1)) {
                set(nr.add(i), true);
            }
            // Return the address of block "nr".
            allocatedBits = allocatedBits.add(noBits);
            nextBitNr = nr.add(noBits);
            return nr;
        } finally {
            unlock();
        }
    }

    /**
     * Free a previously allocated series of bits.
     *
     * @param bit    The bit number as returned by allocateBits.
     * @param noBits The size of the bit series as given to allocateBits.
     */
    public final void freeBits(Word bit, Word noBits) {
        lock();
        try {
            // Mark all blocks as free
            for (Word i = Word.zero(); i.LT(noBits); i = i.add(1)) {
                set(bit.add(i), false);
            }
            allocatedBits = allocatedBits.sub(noBits);
            if (bit.LT(nextBitNr)) {
                nextBitNr = bit;
            }
        } finally {
            unlock();
        }
    }

    /**
     * Mark a series of bits as set.
     *
     * @param bit    The start of the series
     * @param noBits The number of bits in the series
     */
    public final void setBits(Word bit, Word noBits) {
        if (bit.add(noBits).GT(this.bits)) {
            throw new IndexOutOfBoundsException();
        }
        lock();
        try {
            // Mark all bits as set
            for (Word i = Word.zero(); i.LT(noBits); i = i.add(1)) {
                set(bit.add(i), true);
            }
            allocatedBits = allocatedBits.add(noBits);
        } finally {
            unlock();
        }
    }

    /**
     * Find the first free bits that is following by freeBits-1 free bits.
     *
     * @param freeBits
     * @return The bit number of the first block, or Word.max() if not found.
     */
    private final Word findFreeBits(Word freeBits) {
        final Word max = bits;
        Word nr = nextBitNr;
        while (nr.LT(max)) {
            boolean inUse = false;
            Word i;
            for (i = Word.zero(); i.LT(freeBits) && (!inUse); i = i.add(1)) {
                inUse |= isSet(nr.add(i));
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
     * Test if a given bit is set.
     *
     * @param bit the bit to be tested
     * @return {@link true} if the bit is set, {@code false} otherwise.
     */
    private final boolean isSet(Word bit) {
        final Word offset = bit.rshl(3); // we still need a byte offset
        final int mask = (1 << bit.and(Word.fromIntZeroExtend(7)).toInt());
        final Address ptr = bitmap.add(offset);
        final int v = ptr.loadByte() & 0xFF;
        return ((v & mask) == mask);
    }

    /**
     * Set/Reset a given bit.
     *
     * @param bit the bit to be set or reset
     * @param value the new value for the bit
     */
    private final void set(Word bit, boolean value) {
        final Word offset = bit.rshl(3); // we still need a byte offset
        final int mask = (1 << bit.and(Word.fromIntZeroExtend(7)).toInt());
        // final int mask = (1 << blockNr);
        final Address ptr = bitmap.add(offset);
        int v = ptr.loadByte();
        if (value) {
            v |= mask;
        } else {
            v &= ~mask;
        }
        ptr.store((byte) v);
    }

    /**
     * Claim an exclusive access lock the Bitmap data structures. This is done without using java
     * monitorenter/exit instructions, since they may need a memory allocation.
     */
    private final void lock() {
        while (!lock.attempt(0, 1)) {
            // Unsafe.debug(lock.loadInt());
            VmThread.yield();
        }
    }

    /**
     * Release the exclusive access lock to the Bitmap data structures.  Note that no attempt
     * is made to check that the lock is owned by the current thread.
     */
    private final void unlock() {
        lock.store((int) 0);
    }
}
