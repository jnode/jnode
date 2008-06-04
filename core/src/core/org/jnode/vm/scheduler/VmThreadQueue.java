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
import org.jnode.vm.VmStackReader;
import org.jnode.vm.VmSystemObject;
import org.jnode.vm.annotation.KernelSpace;
import org.jnode.vm.annotation.Uninterruptible;
import org.vmmagic.pragma.UninterruptiblePragma;

/**
 * Queue of VmThread's.
 * <p/>
 * This class is not synchronized, but protected by PragmaUninterruptible's,
 * since it is used by the scheduler itself.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
abstract class VmThreadQueue extends VmSystemObject {

    protected final String name;

    protected VmThreadQueueEntry first;

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
     * Gets the first thread in the queue.
     *
     * @return VmThread
     * @throws UninterruptiblePragma
     */
    @KernelSpace
    @Uninterruptible
    final VmThread first() {
        final VmThreadQueueEntry first = this.first;
        if (first != null) {
            return first.thread;
        } else {
            return null;
        }
    }

    /**
     * Gets the first thread in the queue that has its currentProcessor field
     * set to null of the given processor.
     *
     * @param currentProcessor The processor making this request.
     * @return VmThread
     */
    @KernelSpace
    @Uninterruptible
    final VmThread first(VmProcessor currentProcessor) {
        VmThreadQueueEntry entry = this.first;
        while (entry != null) {
            final VmThread thread = entry.thread;
            if ((thread.currentProcessor == null)
                || (thread.currentProcessor == currentProcessor)) {
                return thread;
            }
            entry = entry.next;
        }
        return null;
    }

    /**
     * Invoke the visit method of the visitor for all threads in this queue.
     *
     * @param visitor
     * @return false if the last visit returned false, true otherwise.
     */
    public boolean visit(VmThreadVisitor visitor) {
        VmThreadQueueEntry p = first;
        while (p != null) {
            if (!visitor.visit(p.thread)) {
                return false;
            }
            p = p.next;
        }
        return true;
    }

    /**
     * Is this queue empty?
     *
     * @return boolean
     * @throws UninterruptiblePragma
     */
    @KernelSpace
    @Uninterruptible
    final boolean isEmpty() {
        return (first == null);
    }

    /**
     * Add the given thread to the given queue. The thread is added after all
     * thread with equal or higher priority.
     *
     * @param queue
     * @param entry
     * @param ignorePriority If true, the thread is always added to the back of the list,
     *                       regarding its priority.
     * @param caller
     * @return The new queue.
     * @throws UninterruptiblePragma
     */
    @KernelSpace
    @Uninterruptible
    protected final VmThreadQueueEntry addToQueue(VmThreadQueueEntry queue,
                                                  VmThreadQueueEntry entry, boolean ignorePriority, String caller) {
        entry.setInUse(this, caller);
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
                entry.setNext(t);
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
     * Add the given thread to the given queue such that the queue is sorted by
     * wakeup time. The nearest wakeup time is the first element in the queue.
     *
     * @param queue
     * @param entry
     * @param caller
     * @return The new queue.
     * @throws UninterruptiblePragma
     */
    protected final VmThreadQueueEntry addToQueueSortByWakeupTime(
        VmThreadQueueEntry queue, VmThreadQueueEntry entry, String caller)
        throws UninterruptiblePragma {
        entry.setInUse(this, caller);
        if (queue == null) {
            return entry;
        } else {
            long threadWakeup = entry.thread.wakeupTime;
            VmThreadQueueEntry t = queue;

            if (threadWakeup < t.thread.wakeupTime) {
                entry.next = t;
                return entry;
            }

            while (t.next != null) {
                if (threadWakeup < t.next.thread.wakeupTime) {
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
    static VmThreadQueueEntry removeFromQueue(VmThreadQueueEntry queue,
                                              VmThreadQueueEntry entry) {
        if (queue == null) {
            return queue;
        } else if (entry == null) {
            return queue;
        } else if (queue == entry) {
            VmThreadQueueEntry result = entry.next;
            entry.next = null;
            entry.setInUse(null, null);
            return result;
        } else {
            VmThreadQueueEntry t = queue;
            while ((t.next != null) && (t.next != entry)) {
                t = t.next;
            }
            if (t.next == entry) {
                t.next = entry.next;
                entry.next = null;
                entry.setInUse(null, null);
            }
            return queue;
        }
    }

    /**
     * Dump the status of this queue on Unsafe.debug.
     */
    @KernelSpace
    @Uninterruptible
    final void dump(boolean dumpStack, VmStackReader stackReader) {
        Unsafe.debug(name);
        Unsafe.debug("-queue:\n");
        VmThreadQueueEntry e = first;
        if (e == null) {
            Unsafe.debug("Empty\n");
        } else {
            while (e != null) {
                Unsafe.debug(e.thread.getName());
                Unsafe.debug(" id0x");
                Unsafe.debug(e.thread.getId());
                Unsafe.debug(" s0x");
                Unsafe.debug(e.thread.getThreadState());
                Unsafe.debug(" p0x");
                Unsafe.debug(e.thread.priority);
                Unsafe.debug("\n");
                if (dumpStack && (stackReader != null)) {
                    stackReader.debugStackTrace(e.thread);
                    Unsafe.debug("\n");
                }
                e = e.getNext();
            }
        }
        Unsafe.debug("\n");
    }

    /**
     * Queue for all threads.
     *
     * @author Ewout Prangsma (epr@users.sourceforge.net)
     */
    static final class AllThreadsQueue extends VmThreadQueue {

        /**
         * Initialize this instance.
         */
        public AllThreadsQueue(String name) {
            super(name);
        }

        final void add(VmThread thread, String caller)
            throws UninterruptiblePragma {
            first = addToQueue(first, thread.allThreadsEntry, true, caller);
        }

        final void remove(VmThread thread) throws UninterruptiblePragma {
            first = removeFromQueue(first, thread.allThreadsEntry);
        }
    }

    /**
     * Queue for all sleeping threads.
     *
     * @author Ewout Prangsma (epr@users.sourceforge.net)
     */
    static final class SleepQueue extends VmThreadQueue {

        /**
         * Initialize this instance.
         */
        public SleepQueue(String name) {
            super(name);
        }

        @Uninterruptible
        final void add(VmThread thread, String caller) {
            first = addToQueueSortByWakeupTime(first, thread.sleepQueueEntry,
                caller);
        }

        @KernelSpace
        @Uninterruptible
        final void remove(VmThread thread) {
            first = removeFromQueue(first, thread.sleepQueueEntry);
        }
    }

    /**
     * Queue used by the scheduler and monitors.
     *
     * @author Ewout Prangsma (epr@users.sourceforge.net)
     */
    static final class ScheduleQueue extends VmThreadQueue {

        /**
         * Initialize this instance.
         */
        public ScheduleQueue(String name) {
            super(name);
        }

        @KernelSpace
        @Uninterruptible
        final void add(VmThread thread, boolean ignorePriority, String caller) {
            first = addToQueue(first, thread.queueEntry, ignorePriority, caller);
        }

        @KernelSpace
        @Uninterruptible
        final void remove(VmThread thread) throws UninterruptiblePragma {
            first = removeFromQueue(first, thread.queueEntry);
        }
    }
}
