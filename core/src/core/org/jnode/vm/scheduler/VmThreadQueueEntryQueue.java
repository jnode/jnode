/*
 * $Id: $
 */
package org.jnode.vm.scheduler;

import org.jnode.vm.Unsafe;
import org.jnode.vm.VmStackReader;
import org.jnode.vm.annotation.KernelSpace;
import org.jnode.vm.annotation.Uninterruptible;

abstract class VmThreadQueueEntryQueue extends VmThreadQueue {

    protected VmThreadQueueEntry first;

    /**
     * @param name
     */
    VmThreadQueueEntryQueue(String name) {
        super(name);
        // TODO Auto-generated constructor stub
    }

    /**
     * Invoke the visit method of the visitor for all threads in this queue.
     * 
     * @param visitor
     * @return false if the last visit returned false, true otherwise.
     */
    public final boolean visit(VmThreadVisitor visitor) {
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
                e = e.next;
            }
        }
        Unsafe.debug("\n");
    }
}