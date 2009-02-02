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
 
package org.jnode.vm.x86.performance;

import java.util.Collections;
import java.util.Set;

import org.jnode.vm.performance.PerformanceCounterEvent;
import org.jnode.vm.performance.PerformanceCounters;
import org.jnode.vm.x86.UnsafeX86;
import org.jnode.vm.x86.VmX86Processor;
import org.jnode.vm.x86.VmX86Thread;
import org.jnode.vm.x86.X86CpuID;
import org.vmmagic.pragma.Uninterruptible;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class X86PerformanceCounters extends PerformanceCounters implements Uninterruptible {

    /**
     * Create the appropriate instance for the specific cpu.
     *
     * @param id
     * @return
     */
    public static final X86PerformanceCounters create(VmX86Processor processor,
                                                      X86CpuID id) {
        if (id.hasMSR()) {
            // All known cpu's that have performance counters
            // implement them via MSR's

            if (id.isIntel()) {
                switch (id.getFamily()) {
                    case 0x06:
                        return new P6PerformanceCouters(processor);
                    case 0x0F:
                        return new Pentium4PerformanceCounters(processor);
                }
            } else if (id.isAMD()) {
                switch (id.getFamily()) {
                    case 0x0F:
                        return new Athlon64PerformanceCounters(processor);
                }
            }
        }

        // Fallback, no support for performance counters
        return new X86PerformanceCounters(processor, 0);
    }

    /**
     * Maximum number of simulanious counters
     */
    private final int maxCounters;

    /**
     * The cpu we're attached to
     */
    private final VmX86Processor cpu;

    /**
     * Initialize this instance.
     */
    X86PerformanceCounters(VmX86Processor cpu, int maxCounters) {
        this.maxCounters = maxCounters;
        this.cpu = cpu;
    }

    /**
     * @see org.jnode.vm.performance.PerformanceCounters#getAvailableEvents()()
     */
    public Set<PerformanceCounterEvent> getAvailableEvents() {
        return Collections.emptySet();
    }

    /**
     * @see org.jnode.vm.performance.PerformanceCounters#getAvailableEvent(java.lang.String)
     */
    public PerformanceCounterEvent getAvailableEvent(String id) {
        return null;
    }

    /**
     * @see org.jnode.vm.performance.PerformanceCounters#getMaximumCounters()
     */
    public final int getMaximumCounters() {
        return maxCounters;
    }

    /**
     * @see org.jnode.vm.performance.PerformanceCounters#getCounters(long[])
     */
    public final void getCounterValues(long[] counters) {
        // Force a read of the counters by yielding
        UnsafeX86.saveMSRs();
        // Collect the data
        getCounterValues(counters, (VmX86Thread) cpu.getCurrentThread());
    }

    /**
     * Gets the counters of the given thread.
     *
     * @param counters
     * @param thread
     */
    protected void getCounterValues(long[] counters, VmX86Thread thread) {
        // Override me
    }

    /**
     * @see org.jnode.vm.performance.PerformanceCounters#
     * startCounters(org.jnode.vm.performance.PerformanceCounterEvent[])
     */
    public final void startCounters(PerformanceCounterEvent[] events)
        throws IllegalArgumentException {
        // Test if the events array is not too long
        if (events.length > getMaximumCounters()) {
            throw new IllegalArgumentException(
                "events.length is greater then maximum available counters");
        }
        // Test if given events are valid for me
        final Set<PerformanceCounterEvent> myEvents = getAvailableEvents();
        for (PerformanceCounterEvent e : events) {
            if (!myEvents.contains(e)) {
                throw new IllegalArgumentException("Event " + e
                    + " is not one of the available events");
            }
        }
        // Do the actual start
        startCounters(events, (VmX86Thread) cpu.getCurrentThread());

        // Force a start by syncing 
        UnsafeX86.restoreMSRs();
    }


    /**
     * Stop the counters on the given thread.
     *
     * @see org.jnode.vm.performance.PerformanceCounters#stopCounters()
     */
    protected void startCounters(PerformanceCounterEvent[] events,
                                 VmX86Thread thread) throws IllegalArgumentException {
        // Override me
    }

    /**
     * @see org.jnode.vm.performance.PerformanceCounters#stopCounters()
     */
    public final void stopCounters() {
        // TODO Implement me
        final VmX86Thread thread = (VmX86Thread) cpu.getCurrentThread();
        stopCounters(thread);
        UnsafeX86.saveMSRs();

        thread.setMSRs(null, null);
    }

    /**
     * Start the counters on the given thread.
     *
     * @see org.jnode.vm.performance.PerformanceCounters#
     * startCounters(org.jnode.vm.performance.PerformanceCounterEvent[])
     */
    protected void stopCounters(VmX86Thread thread) throws IllegalArgumentException {
        // Override me
    }
}
