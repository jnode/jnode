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

import org.jnode.system.IRQHandler;
import org.jnode.system.ResourceOwner;
import org.jnode.vm.annotation.KernelSpace;
import org.jnode.vm.annotation.Uninterruptible;
import org.vmmagic.pragma.UninterruptiblePragma;

/**
 * Thread for IRQ handler.
 * 
 * @author epr
 */
final class IRQThread extends Thread implements SystemThread {

    private final IRQManager mgr;

    private final int irq;

    private boolean stop;

    private int irqCount;

    private int handledIrqCount;

    /** Is this a shared IRQ? */
    private final boolean shared;

    private IRQAction actions;

    private final VmThread vmThread;

    private final Object ACTION_LOCK = new Object();

    public IRQThread(IRQManager mgr, int irq, ResourceOwner owner,
            IRQHandler handler, boolean shared, VmProcessor processor) {
        super("IRQ-" + irq);
        this.mgr = mgr;
        this.irq = irq;
        this.setPriority(Thread.MAX_PRIORITY);
        this.stop = false;
        this.irqCount = 0;
        this.handledIrqCount = 0;
        this.shared = shared;
        this.actions = new IRQAction(owner, handler);
        this.vmThread = getVmThreadKS();
        this.vmThread.setRequiredProcessor(processor);
    }

    /**
     * Add an IRQ handler
     * 
     * @param owner
     * @param handler
     */
    public final void addHandler(ResourceOwner owner, IRQHandler handler) {
        synchronized (ACTION_LOCK) {
            final IRQAction a = new IRQAction(owner, handler);
            if (actions == null) {
                actions = a;
            } else {
                actions.add(a);
            }
        }
    }

    /**
     * Remove a given handler
     * 
     * @param handler
     */
    public final void remove(IRQHandler handler) {
        synchronized (ACTION_LOCK) {
            if (this.actions != null) {
                this.actions = this.actions.remove(handler);
            }
        }
    }

    public final boolean isEmpty() {
        return (this.actions == null);
    }

    /**
     * Stop this IRQ thread.
     * 
     */
    final void stopThread() {
        this.stop = true;
        interrupt();
        vmThread.unsecureResume();
    }

    /**
     * Continue to run until i'm stopped.
     * 
     * @see java.lang.Runnable#run()
     */
    public final void run() {
        while (!stop) {
            doHandle();
        }
    }

    /**
     * Wait to be notified or then handle exactly 1 interrupt.
     */
    private final void doHandle() {
        if (irqCount == handledIrqCount) {
            vmThread.unsecureSuspend();
            // try {
            // wait();
            // } catch (InterruptedException ex) {
            // // Ignore
            // }
            // Also set by signalIRQ, but just in case...
        } else {
            try {
                try {
                    /*
                     * if (irq == 10) { Screen.debug(" <handle IRQ 10/> ");
                     */
                    IRQAction action = this.actions;
                    while (action != null) {
                        try {
                            if (action.isEnabled()) {
                                action.getHandler().handleInterrupt(irq);
                            }
                        } catch (Throwable ex) {
                            action.incErrorCount();
                            ex.printStackTrace();
                        }
                        action = action.getNext();
                    }
                    handledIrqCount++;
                } finally {
                    mgr.eoi(irq);
                }
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Tell this thread how many IRQ's have been received, so this thread can
     * start the IRQ handler if needed.
     * 
     * @param count
     * @param current
     * @throws UninterruptiblePragma
     */
    @KernelSpace
    @Uninterruptible
    final void signalIRQ(int count, VmThread current) {
        this.irqCount = count;
        if ((irqCount != handledIrqCount) && (current != vmThread)){
            vmThread.unsecureResume();
        }
    }

    /**
     * @return int
     */
    public final int getIrq() {
        return irq;
    }

    /**
     * Convert to a String representation.
     * 
     * @see java.lang.Object#toString()
     * @return String
     */
    public String toString() {
        final StringBuilder b = new StringBuilder();
        if (shared) {
            b.append("shared ");
        }
        IRQAction a = this.actions;
        boolean first = true;
        while (a != null) {
            if (!first) {
                b.append(", ");
            }
            first = false;
            b.append(a);
            a = a.getNext();
        }
        return b.toString();
    }

    static class IRQAction {
        private final ResourceOwner owner;

        private final IRQHandler handler;

        private IRQAction next;

        private int errorCount;

        public IRQAction(ResourceOwner owner, IRQHandler handler) {
            this.owner = owner;
            this.handler = handler;
        }

        public final IRQHandler getHandler() {
            return handler;
        }

        public final IRQAction getNext() {
            return next;
        }

        public final void add(IRQAction action) {
            IRQAction p = this;
            while (p.next != null) {
                p = p.next;
            }
            p.next = action;
        }

        public final IRQAction remove(IRQHandler handler) {
            if (this.handler == handler) {
                return this.next;
            } else if (this.next != null) {
                this.next = this.next.remove(handler);
                return this;
            } else {
                return this;
            }
        }

        public final void incErrorCount() {
            errorCount++;
        }

        public final boolean isEnabled() {
            return errorCount < 10;
        }

        public final String toString() {
            return owner.getShortDescription()
                    + ((errorCount > 0) ? " errors " + errorCount : "");
        }
    }

    /**
     * @return Returns the shared.
     */
    public final boolean isShared() {
        return this.shared;
    }

}
