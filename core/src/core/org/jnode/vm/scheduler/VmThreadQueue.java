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

import org.jnode.vm.VmStackReader;
import org.jnode.vm.VmSystemObject;
import org.jnode.vm.annotation.KernelSpace;
import org.jnode.vm.annotation.Uninterruptible;
import org.vmmagic.pragma.UninterruptiblePragma;

/**
 * Queue of VmThread's.
 * 
 * This class is not synchronized, but protected by PragmaUninterruptible's,
 * since it is used by the scheduler itself.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
abstract class VmThreadQueue extends VmSystemObject {

    protected final String name;

    /**
     * Create a new instance
     * 
     * @param name
     * @param sortByWakeupTime
     */
    protected VmThreadQueue(String name) {
        this.name = name;
    }

    /**
     * Invoke the visit method of the visitor for all threads in this queue.
     * 
     * @param visitor
     * @return false if the last visit returned false, true otherwise.
     */
    public abstract boolean visit(VmThreadVisitor visitor);

    /**
     * Add the given thread to the given queue. The thread is added after all
     * thread with equal or higher priority.
     * 
     * @param queue
     * @param entry
     * @param ignorePriority
     *            If true, the thread is always added to the back of the list,
     *            regarding its priority.
     * @param caller
     * @return The new queue.
     * @throws UninterruptiblePragma
     */
    @KernelSpace
    @Uninterruptible
    protected final VmThreadQueueEntry addToQueue(VmThreadQueueEntry queue,
            VmThreadQueueEntry entry, boolean ignorePriority) {
        entry.setQueue(this);
        if (queue == null) {
            return entry;
        } else if (ignorePriority) {
            VmThreadQueueEntry t = queue;

            while (t.next != null) {
                t = t.next;
            }
            t.next = entry;
            return queue;
        } else {
            final int priority = entry.thread.priority;
            VmThreadQueueEntry t = queue;

            if (priority > t.thread.priority) {
                entry.next = t;
                return entry;
            }

            while (t.next != null) {
                if (priority > t.next.thread.priority) {
                    entry.next = t.next;
                    t.next = entry;
                    return queue;
                }
                t = t.next;
            }
            t.next = entry;
            return queue;
        }
    }

    /**
     * Remove the given thread from the given queue.
     * 
     * @param queue
     * @param entry
     * @return The new queue.
     * @throws UninterruptiblePragma
     */
    @KernelSpace
    @Uninterruptible
    protected final VmThreadQueueEntry removeFromQueue(VmThreadQueueEntry queue,
            VmThreadQueueEntry entry) {
        if (queue == null) {
            return queue;
        } else if (entry == null) {
            return queue;
        } else if (queue == entry) {
            VmThreadQueueEntry result = entry.next;
            entry.next = null;
            entry.resetQueue(this);
            return result;
        } else {
            VmThreadQueueEntry t = queue;
            while ((t.next != null) && (t.next != entry)) {
                t = t.next;
            }
            if (t.next == entry) {
                t.next = entry.next;
                entry.next = null;
                entry.resetQueue(this);
            }
            return queue;
        }
    }

    /**
     * Dump the status of this queue on Unsafe.debug.
     */
    @KernelSpace
    @Uninterruptible
    abstract void dump(boolean dumpStack, VmStackReader stackReader);
}
