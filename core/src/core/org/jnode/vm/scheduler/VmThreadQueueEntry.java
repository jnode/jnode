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
import org.jnode.vm.annotation.KernelSpace;
import org.jnode.vm.annotation.Uninterruptible;

/**
 * Queue entry for VmThread's.
 * <p/>
 * This class is not synchronized, but protected by PragmaUninterruptible's,
 * since it is used by the scheduler itself.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
@Uninterruptible
final class VmThreadQueueEntry extends VmSystemObject {

    protected VmThreadQueueEntry next;
    private VmThreadQueue inUseByQueue;
    protected final VmThread thread;
    private String lastCaller;

    /**
     * Initialize this instance
     *
     * @param thread
     */
    public VmThreadQueueEntry(VmThread thread) {
        this.thread = thread;
    }

    /**
     * Gets the next entry
     *
     * @return next entry
     * @throws UninterruptiblePragma
     */
    @KernelSpace
    final VmThreadQueueEntry getNext() {
        return next;
    }

    /**
     * Is this entry used on any queue.
     *
     * @return boolean
     * @throws UninterruptiblePragma
     */
    final boolean isInUse() {
        return (inUseByQueue != null);
    }

    /**
     * @param q
     * @param caller
     * @throws UninterruptiblePragma
     */
    @KernelSpace
    @Uninterruptible
    final void setInUse(VmThreadQueue q, String caller) {
        if (this.inUseByQueue != null) {
            // currently in use
            if (q == null) {
                this.inUseByQueue = null;
            } else if (q == this.inUseByQueue) {
                Unsafe.debug("Thread '");
                Unsafe.debug(this.thread.getName());
                Unsafe.debug("' is already on the ");
                Unsafe.debug(q.name);
                Unsafe.debug(" queue by ");
                Unsafe.debug(lastCaller);
                Unsafe.debug(" called by ");
                Unsafe.debug(caller);
                VmProcessor.current().getArchitecture().getStackReader().debugStackTrace();
                Unsafe.die("setInUse");
            } else {
                Unsafe.debug("Thread '");
                Unsafe.debug(this.thread.getName());
                Unsafe.debug("' is already in use by ");
                Unsafe.debug(this.inUseByQueue.name);
                Unsafe.debug(" by ");
                Unsafe.debug(lastCaller);
                Unsafe.debug(" cannot add to ");
                Unsafe.debug(q.name);
                Unsafe.debug(" called by ");
                Unsafe.debug(caller);
                Unsafe.die("setInUse");
            }
        } else {
            // currently NOT in use
            if (q == null) {
                Unsafe.debug("Thread is not in use.");
                Unsafe.die("setInUse");
            } else {
                this.inUseByQueue = q;
                this.lastCaller = caller;
            }
        }
    }

    /**
     * @param entry
     */
    @KernelSpace
    @Uninterruptible
    final void setNext(VmThreadQueueEntry entry) {
        this.next = entry;
    }

    /**
     * Gets the thread of this entry
     *
     * @return The thread
     */
    final VmThread getThread() {
        return thread;
    }
}
