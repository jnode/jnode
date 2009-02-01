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
 
package org.jnode.vm.x86.performance;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import org.jnode.vm.performance.PerformanceCounterEvent;
import org.jnode.vm.performance.PresetEvent;
import org.jnode.vm.x86.MSR;
import org.jnode.vm.x86.VmX86Processor;
import org.jnode.vm.x86.VmX86Thread;
import org.vmmagic.pragma.UninterruptiblePragma;

/**
 * Implementation of PerformanceCounters for models that implement performance
 * monitoring counter using 2 MSR's per counter: a select MSR and a counter MSR.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
class DualMSRPerformanceCounters extends X86PerformanceCounters {

    /**
     * Event implementation.
     *
     * @author Ewout Prangsma (epr@users.sourceforge.net)
     */
    public static class DualMSREvent extends PerformanceCounterEvent {

        /**
         * Value for the select MSR used to select this event
         */
        private final long selectValue;

        /**
         * @param id
         */
        public DualMSREvent(PresetEvent preset, long selectValue) {
            super(preset);
            this.selectValue = selectValue;
        }

        /**
         * @param id
         * @param selectValue
         */
        public DualMSREvent(PresetEvent preset, int eventMask, int unitMask) {
            this(preset, createSelectValue(eventMask, unitMask));
        }

        private static long createSelectValue(int eventMask, int unitMask) {
            long v = 0;
            v |= eventMask & 0xFF;
            v |= (unitMask & 0xFF) << 8;
            v |= 1 << 16; // USR
            v |= 1 << 22; // EN
            return v;
        }

        /**
         * @return Returns the selectValue.
         */
        public final long getSelectValue() {
            return selectValue;
        }
    }

    private final int[] countMSRIndexes;

    private final DualMSREvent[] events;

    private final Set<PerformanceCounterEvent> eventSet;

    private final int[] selectMSRIndexes;

    /**
     * @param id
     */
    public DualMSRPerformanceCounters(VmX86Processor cpu,
                                      int[] selectMSRIndexes, int[] countMSRIndexes, DualMSREvent[] events) {
        super(cpu, selectMSRIndexes.length);
        if (selectMSRIndexes.length != countMSRIndexes.length) {
            throw new IllegalArgumentException(
                "selectMSRs and countMSRs have diffent length");
        }
        this.selectMSRIndexes = selectMSRIndexes;
        this.countMSRIndexes = countMSRIndexes;
        this.events = events;
        this.eventSet = Collections
            .unmodifiableSet(new TreeSet<PerformanceCounterEvent>(Arrays
                .asList(events)));
    }

    /**
     * @see org.jnode.vm.x86.performance.X86PerformanceCounters#getAvailableEvents()
     */
    public final Set<PerformanceCounterEvent> getAvailableEvents() {
        return eventSet;
    }

    /**
     * @see org.jnode.vm.x86.performance.X86PerformanceCounters#getAvailableEvent(java.lang.String)
     */
    public final PerformanceCounterEvent getAvailableEvent(String id) {
        for (PerformanceCounterEvent e : events) {
            if (e.getId().equals(id)) {
                return e;
            }
        }
        return null;
    }

    /**
     * @see org.jnode.vm.x86.performance.X86PerformanceCounters#getCounterValues(long[])
     */
    protected final void getCounterValues(long[] counters, VmX86Thread thread)
        throws UninterruptiblePragma {
        final MSR[] countMSRs = thread.getReadWriteMSRs();
        if (countMSRs == null) {
            throw new IllegalArgumentException("No active counters");
        }
        final int cnt = counters.length;
        if (cnt > countMSRs.length) {
            throw new IllegalArgumentException("counters array is too long");
        }

        // Get the counters
        for (int i = 0; i < cnt; i++) {
            counters[i] = countMSRs[i].getValue();
        }
    }

    /**
     * @see org.jnode.vm.x86.performance.X86PerformanceCounters
     * #startCounters(org.jnode.vm.performance.PerformanceCounterEvent[])
     */
    protected final void startCounters(PerformanceCounterEvent[] events,
                                       VmX86Thread thread) throws IllegalArgumentException {
        final int cnt = events.length;
        final MSR[] countMSRs = new MSR[cnt];
        final MSR[] selectMSRs = new MSR[cnt];

        for (int i = 0; i < cnt; i++) {
            final DualMSREvent e = (DualMSREvent) events[i];
            countMSRs[i] = new MSR(this.countMSRIndexes[i]);
            selectMSRs[i] = new MSR(this.selectMSRIndexes[i], e.getSelectValue());
        }

        // Install MSR's
        thread.setMSRs(countMSRs, selectMSRs);
    }

    /**
     * @see org.jnode.vm.x86.performance.X86PerformanceCounters#stopCounters(org.jnode.vm.x86.VmX86Thread)
     */
    protected void stopCounters(VmX86Thread thread) throws IllegalArgumentException {
        final MSR[] selectMSRs = thread.getWriteOnlyMSRs();
        if (selectMSRs != null) {
            final int cnt = selectMSRs.length;
            // Reset the counter selectors
            for (int i = 0; i < cnt; i++) {
                selectMSRs[i].setValue(0);
            }
        }
    }
}
