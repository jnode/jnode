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

package org.jnode.vm.scheduler;

import org.jnode.vm.Unsafe;
import org.jnode.vm.VmSystemObject;
import org.jnode.vm.annotation.Inline;
import org.jnode.vm.annotation.MagicPermission;
import org.jnode.vm.annotation.NoFieldAlignments;
import org.jnode.vm.annotation.Uninterruptible;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.ObjectReference;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
@NoFieldAlignments
@MagicPermission
public class SpinLock extends VmSystemObject {

    /**
     * Lock counter. THIS FIELD MUST BE AT OFFSET 0!!
     */
    private int lockCount;
    /**
     * Owner used for deadlock detection
     */
    private VmProcessor owner;

    /**
     * Claim access to this monitor. A monitor may only be locked for a small
     * amount of time, since this method uses a spinlock.
     *
     * @see #unlock()
     * @see #lockCount
     */
    @Inline
    @Uninterruptible
    public final void lock() {
        final VmProcessor current = VmProcessor.current();

        // Test for obvious deadlock
        if (owner == current) {
            Unsafe.debugStackTrace();
            Unsafe.die("Deadlock in SpinLock#lock");
        }

        // Do the spinlock
        final Address mlAddr = ObjectReference.fromObject(this).toAddress();
        while (!mlAddr.attempt(0, 1)) {
            current.yield(true);
        }
        this.owner = current;
    }

    /**
     * Release access to this monitor. A monitor may only be locked for a small
     * amount of time, since this method uses a spinlock.
     *
     * @see #lock()
     * @see #lockCount
     */
    @Inline
    @Uninterruptible
    public final void unlock() {
        this.owner = null;
        this.lockCount = 0;
    }

    /**
     * Is this lock locked.
     *
     * @return
     */
    @Inline
    @Uninterruptible
    protected final boolean isLocked() {
        return (lockCount != 0);
    }
}
