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
import org.jnode.system.IRQResource;
import org.jnode.system.Resource;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.system.ResourceOwner;
import org.jnode.vm.annotation.Internal;
import org.jnode.vm.annotation.KernelSpace;
import org.jnode.vm.annotation.PrivilegedActionPragma;
import org.jnode.vm.annotation.Uninterruptible;

/**
 * IRQ manager implementation.
 *
 * @author epr
 */
public abstract class IRQManager {

    private final Object LOCK = new Object();
    private final IRQThread[] handlers;
    private final int count;
    private final int[] irqCount;
    private final VmProcessor defaultIrqProcessor;

    /**
     * Initialize a new instance
     *
     * @param irqCount
     */
    @PrivilegedActionPragma
    protected IRQManager(int[] irqCount, VmProcessor defaultIrqProcessor) {
        this.irqCount = irqCount;
        this.count = irqCount.length;
        this.defaultIrqProcessor = defaultIrqProcessor;
        this.handlers = new IRQThread[count];
    }

    /**
     * Gets the IRQ counter value for the given IRQ
     *
     * @param irq
     * @return The irq count
     */
    public final int getIrqCount(int irq) {
        return irqCount[irq];
    }

    /**
     * Gets the number of IRQ's this processor supports.
     *
     * @return int
     */
    public final int getNumIRQs() {
        return count;
    }

    public Object getHandlerInfo(int irq) {
        return handlers[irq];
    }

    /**
     * Register an interrupt handler for a given irq number.
     *
     * @param owner
     * @param irq
     * @param handler
     * @param shared
     * @return True is the handler was set, false if there was already a handler for the given irq
     *         number set.
     * @throws ResourceNotFreeException
     */
    @Internal
    public final IRQResource claimIRQ(ResourceOwner owner, int irq, IRQHandler handler, boolean shared)
        throws ResourceNotFreeException {
        IRQThread newThread = null;
        synchronized (LOCK) {
            final IRQThread thread = handlers[irq];
            if (thread == null) {
                newThread = new IRQThread(this, irq, owner, handler, shared, defaultIrqProcessor);
                handlers[irq] = newThread;
            } else {
                if (thread.isShared()) {
                    if (shared) {
                        thread.addHandler(owner, handler);
                    } else {
                        throw new ResourceNotFreeException(
                            "IRQ " + irq + " is already claimed, but you don't want to share");
                    }
                } else {
                    if (shared) {
                        throw new ResourceNotFreeException("IRQ " + irq + " is already claimed, but not shared");
                    } else {
                        //Unsafe.debug("IRQ was already claim");
                        throw new ResourceNotFreeException("IRQ " + irq + " is not free");
                    }
                }
            }
        }
        if (newThread != null) {
            newThread.start();
        }
        return new IRQResourceImpl(owner, irq, handler, shared);
    }

    /**
     * Unregister a given interrupt handler for a given irq number. If the current handler for the
     * given irq number is not equal to the given handler, nothing is done.
     *
     * @param res The IRQ resource
     */
    final void releaseIRQ(IRQResourceImpl res) {
        final int irq = res.getIRQ();
        synchronized (LOCK) {
            final IRQThread thread = handlers[irq];
            if (thread != null) {
                thread.remove(res.getHandler());
                if (thread.isEmpty()) {
                    handlers[irq] = null;
                    thread.stopThread();
                }
            }
        }
    }

    /**
     * Dispatch IRQ events to their corresponding threads. This method should only be called by the
     * Thread scheduler.
     *
     * @param current
     * @throws org.vmmagic.pragma.UninterruptiblePragma
     */
    @KernelSpace
    @Uninterruptible
    final void dispatchInterrupts(VmThread current) {
        final IRQThread[] hlist = handlers;
        final int[] irqCount = this.irqCount;
        final int max = this.count;
        for (int irq = 0; irq < max; irq++) {
            final IRQThread thread = hlist[irq];
            if (thread != null) {
                thread.signalIRQ(irqCount[irq], current);
            } else {
                // No handler, signal and End of Interrupt
                eoi(irq);
            }
        }
    }

    /**
     * Set an End Of Interrupt message to the 8259 interrupt controller(s).
     *
     * @param irq
     */
    @Uninterruptible
    @KernelSpace
    protected abstract void eoi(int irq);

    final class IRQResourceImpl implements IRQResource {

        /**
         * The owner of this resource
         */
        private final ResourceOwner owner;
        /**
         * The IRQ number
         */
        private final int irq;
        /**
         * The handler
         */
        private final IRQHandler handler;
        private final boolean shared;

        /**
         * Create a new instance
         *
         * @param owner
         * @param irq
         * @param handler
         * @param shared
         */
        public IRQResourceImpl(ResourceOwner owner, int irq, IRQHandler handler, boolean shared) {
            this.owner = owner;
            this.irq = irq;
            this.handler = handler;
            this.shared = shared;
        }

        /**
         * @return int
         * @see org.jnode.system.IRQResource#getIRQ()
         */
        public int getIRQ() {
            return irq;
        }

        /**
         * Is this a shared interrupt?
         *
         * @return boolean
         */
        public boolean isShared() {
            return shared;
        }

        /**
         * @return The owner
         * @see org.jnode.system.Resource#getOwner()
         */
        public ResourceOwner getOwner() {
            return owner;
        }

        public IRQHandler getHandler() {
            return handler;
        }

        /**
         * @see org.jnode.system.Resource#release()
         */
        public void release() {
            releaseIRQ(this);
        }

        /**
         * Gets the parent resource if any.
         *
         * @return The parent resource, or null if this resource has no parent.
         */
        public Resource getParent() {
            return null;
        }
    }
}
