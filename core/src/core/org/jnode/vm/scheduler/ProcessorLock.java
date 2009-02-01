/*
 * $Id$
 *
 * JNode.org
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
 
package org.jnode.vm.scheduler;

import org.jnode.vm.Unsafe;
import org.jnode.vm.VmMagic;
import org.jnode.vm.annotation.Inline;
import org.jnode.vm.annotation.KernelSpace;
import org.jnode.vm.annotation.MagicPermission;
import org.jnode.vm.annotation.NoFieldAlignments;
import org.jnode.vm.annotation.Uninterruptible;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.ObjectReference;
import org.vmmagic.unboxed.Word;

/**
 * A ProcessorLock is a lock that grants access to a resource from a single
 * processor.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
@MagicPermission
@NoFieldAlignments
public class ProcessorLock {

    /**
     * Current owner of this lock, null if not locked. Keep this field at offset
     * 0!!!
     */
    private VmProcessor owner;

    /**
     * Number of times the lock has been locked
     */
    private Word lockCount;

    /**
     * Claim access to this lock.
     *
     * @see #unlock()
     * @see #lockCount
     */
    @Uninterruptible
    @KernelSpace
    @Inline
    public final void lock() {
        if (this.owner == VmMagic.currentProcessor()) {
            // We already own this lock, increment the lock count.
            lockCount = lockCount.add(Word.one());
        } else {
            final Address ownerAddr = ObjectReference.fromObject(this)
                .toAddress();
            final ObjectReference procRef = ObjectReference.fromObject(VmMagic
                .currentProcessor());
            while (!ownerAddr.attempt(null, procRef)) {
                // Busy wait
            }
            // Now I'm the owner
            lockCount = lockCount.add(Word.one());
        }
    }

    /**
     * Release access to this monitor. A monitor may only be locked for a small
     * amount of time, since this method uses a spinlock.
     *
     * @see #lock()
     * @see #lockCount
     */
    @Uninterruptible
    @KernelSpace
    @Inline
    public final void unlock() {
        if (this.owner == VmMagic.currentProcessor()) {
            // We own this lock, decrement the lock count.
            lockCount = lockCount.sub(Word.one());
            if (lockCount.isZero()) {
                // Release the lock
                this.owner = null;
            }
        } else {
            // We cannot unlock while we do not own the lock.
            // This is a programmers error.
            Unsafe.debug("Cannot unlock ProcessorLock owned by ");
            Unsafe.debug(owner.getIdString());
            Unsafe.die("unlock");
        }
    }
}
