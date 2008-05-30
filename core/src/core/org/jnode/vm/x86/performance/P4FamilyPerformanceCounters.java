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

package org.jnode.vm.x86.performance;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.jnode.vm.performance.PerformanceCounterEvent;
import org.jnode.vm.performance.PresetEvent;
import org.jnode.vm.x86.MSR;
import org.jnode.vm.x86.VmX86Processor;
import org.jnode.vm.x86.VmX86Thread;

abstract class P4FamilyPerformanceCounters extends X86PerformanceCounters {

    private final CounterMSRInfo[] counters;

    private final P4Event[] events;

    private final Set<PerformanceCounterEvent> eventSet;

    // ------------------------------------------
    // ESCR MSR indexes
    // ------------------------------------------   
    public static final int BPU_ESCR0 = 0x3B2;
    public static final int BPU_ESCR1 = 0x3B3;
    public static final int BSU_ESCR0 = 0x3A0;
    public static final int BSU_ESCR1 = 0x3A1;
    public static final int CR_ESCR0 = 0;
    public static final int CR_ESCR1 = 0;
    public static final int CRU_ESCR0 = 0x3B8;
    public static final int CRU_ESCR1 = 0x3B9;
    public static final int CRU_ESCR2 = 0x3CC;
    public static final int CRU_ESCR3 = 0x3CD;
    public static final int CRU_ESCR4 = 0x3E0;
    public static final int CRU_ESCR5 = 0x3E1;
    public static final int FSB_ESCR0 = 0x3A2;
    public static final int FSB_ESCR1 = 0x3A3;
    public static final int IS_ESCR0 = 0x3B4;
    public static final int IS_ESCR1 = 0x3B5;
    public static final int ITLB_ESCR0 = 0x3B6;
    public static final int ITLB_ESCR1 = 0x3B7;
    public static final int IX_ESCR0 = 0x3C8;
    public static final int IX_ESCR1 = 0x3C9;
    public static final int MOP_ESCR0 = 0x3AA;
    public static final int MOP_ESCR1 = 0x3AB;
    public static final int MS_ESCR0 = 0x3C0;
    public static final int MS_ESCR1 = 0x3C1;
    public static final int PMH_ESCR1 = 0x3AD;
    public static final int PMH_ESCR0 = 0x3AC;
    public static final int TBPU_ESCR0 = 0x3C2;
    public static final int TBPU_ESCR1 = 0x3C3;
    public static final int TC_ESCR0 = 0x3C4;
    public static final int TC_ESCR1 = 0x3C5;

    /**
     * @param cpu
     * @param maxCounters
     */
    public P4FamilyPerformanceCounters(VmX86Processor cpu,
                                       CounterMSRInfo[] counters, P4Event[] events) {
        super(cpu, counters.length);
        this.counters = counters;
        this.events = events;
        this.eventSet = Collections
            .unmodifiableSet(new TreeSet<PerformanceCounterEvent>(Arrays
                .asList(events)));
    }

    /**
     * Class holding MSR information for a single performance counter.
     *
     * @author Ewout Prangsma (epr@users.sourceforge.net)
     */
    public static class CounterMSRInfo {
        final char counterMSRIndex;

        final char cccrMSRIndex;

        /**
         * @param counterMSRIndex
         * @param cccrMSRIndex
         * @param escrNrs
         * @param escrMSRIndexes
         */
        public CounterMSRInfo(int counterMSRIndex, int cccrMSRIndex) {
            this.counterMSRIndex = (char) counterMSRIndex;
            this.cccrMSRIndex = (char) cccrMSRIndex;
        }
    }

    /**
     * ESCR restriction.
     *
     * @author Ewout Prangsma (epr@users.sourceforge.net)
     */
    public static final class ESCR_RES {
        /**
         * The ESCR MSR index
         */
        final char escr;

        /**
         * The Counters number (0..17) to which this event is restricted
         */
        final byte[] counters;

        /**
         * @param escr
         * @param counters
         */
        public ESCR_RES(int escr, byte[] counters) {
            this.escr = (char) escr;
            this.counters = counters;
        }

        /**
         * @param escr
         * @param cnt0
         */
        public ESCR_RES(int escr, int cnt0) {
            this(escr, new byte[]{(byte) cnt0});
        }

        /**
         * @param escr
         * @param cnt0
         * @param cnt1
         */
        public ESCR_RES(int escr, int cnt0, int cnt1) {
            this(escr, new byte[]{(byte) cnt0, (byte) cnt1});
        }

        /**
         * @param escr
         * @param cnt0
         * @param cnt1
         * @param cnt2
         */
        public ESCR_RES(int escr, int cnt0, int cnt1, int cnt2) {
            this(escr, new byte[]{(byte) cnt0, (byte) cnt1, (byte) cnt2});
        }
    }

    /**
     * Event class for P4 familiy events.
     *
     * @author Ewout Prangsma (epr@users.sourceforge.net)
     */
    public static final class P4Event extends PerformanceCounterEvent {

        /**
         * ESCR bits 30-25
         */
        private final byte escrEventSelect;

        /**
         * ESCR bits 24-9
         */
        private final char escrEventMask;

        /**
         * ESCR restrictions
         */
        private final ESCR_RES[] escrRestr;

        /**
         * CCCR select value
         */
        private final byte cccrSelect;

        /**
         * @param preset
         */
        public P4Event(PresetEvent preset, int escrEventSelect,
                       int escrEventMask, int cccrSelect, ESCR_RES res0) {
            this(preset, escrEventSelect, escrEventMask, cccrSelect,
                new ESCR_RES[]{res0});
        }

        /**
         * @param preset
         */
        public P4Event(PresetEvent preset, int escrEventSelect,
                       int escrEventMask, int cccrSelect, ESCR_RES res0, ESCR_RES res1) {
            this(preset, escrEventSelect, escrEventMask, cccrSelect,
                new ESCR_RES[]{res0, res1});
        }

        /**
         * @param preset
         */
        public P4Event(PresetEvent preset, int escrEventSelect,
                       int escrEventMask, int cccrSelect, ESCR_RES[] ress) {
            super(preset);
            this.escrEventSelect = (byte) escrEventSelect;
            this.escrEventMask = (char) escrEventMask;
            this.cccrSelect = (byte) cccrSelect;
            this.escrRestr = ress;
        }

        /**
         * @param id
         * @param description
         */
        public P4Event(String id, String description, int escrEventSelect,
                       int escrEventMask, int cccrSelect, ESCR_RES[] ress) {
            super(id, description);
            this.escrEventSelect = (byte) escrEventSelect;
            this.escrEventMask = (char) escrEventMask;
            this.cccrSelect = (byte) cccrSelect;
            this.escrRestr = ress;
        }

        /**
         * Creates the value for the ESCR MSR.
         *
         * @return
         */
        final long createESCRValue() {
            // USR | eventMask | eventSelect
            return (1 << 2) | (escrEventMask << 9) | (escrEventSelect << 25);
        }

        /**
         * Creates the value for the CCCR MSR.
         *
         * @return
         */
        final long createCCCRValue() {
            // EN | ESCR select | Rsvd
            return (1 << 12) | (cccrSelect << 13) | (3 << 16);
        }
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
     * @see org.jnode.vm.x86.performance.X86PerformanceCounters#getAvailableEvents()
     */
    public final Set<PerformanceCounterEvent> getAvailableEvents() {
        return eventSet;
    }

    /**
     * @see org.jnode.vm.x86.performance.X86PerformanceCounters#getCounterValues(long[],
     *      org.jnode.vm.x86.VmX86Thread)
     */
    protected void getCounterValues(long[] counters, VmX86Thread thread) {
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
     * @see org.jnode.vm.x86.performance.X86PerformanceCounters#
     * startCounters(org.jnode.vm.performance.PerformanceCounterEvent[],
     *      org.jnode.vm.x86.VmX86Thread)
     */
    protected void startCounters(PerformanceCounterEvent[] events,
                                 VmX86Thread thread) throws IllegalArgumentException {

        final Map<Integer, P4Event> escrState = new HashMap<Integer, P4Event>();
        final P4Event[] counterState = new P4Event[counters.length];

        final int eventCnt = events.length;
        final MSR[] selectMSRs = new MSR[eventCnt * 2];
        final MSR[] counterMSRs = new MSR[eventCnt];

        int i = 0;
        for (PerformanceCounterEvent e : events) {
            final P4Event evt = (P4Event) e;
            // Find a free ESCR
            final ESCR_RES escr = findFreeESCR(escrState, counterState, evt);
            final CounterMSRInfo cntInfo = counters[indexOf(counterState, evt)];

            // Create the MSRs
            selectMSRs[i * 2 + 0] = new MSR(escr.escr, evt.createESCRValue());
            selectMSRs[i * 2 + 1] = new MSR(cntInfo.cccrMSRIndex, evt.createCCCRValue());
            counterMSRs[i] = new MSR(cntInfo.counterMSRIndex);

            i++;
        }

        thread.setMSRs(counterMSRs, selectMSRs);
    }

    /**
     * Gets the index of the given object in the given array.
     *
     * @param <T>
     * @param arr
     * @param obj
     * @return
     */
    private final <T> int indexOf(T[] arr, T obj) {
        final int max = arr.length;
        for (int i = 0; i < max; i++) {
            if (arr[i] == obj) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Find a free ESCR that can be used for the given event.
     *
     * @param escrState
     * @param evt
     * @return
     */
    private final ESCR_RES findFreeESCR(Map<Integer, P4Event> escrState,
                                        P4Event[] counters, P4Event evt) throws IllegalArgumentException {
        for (ESCR_RES res : evt.escrRestr) {
            final int escrIndex = res.escr;
            if (!escrState.containsKey(escrIndex)) {
                final int cnt;
                if ((cnt = findFreeCounter(counters, res)) >= 0) {
                    escrState.put(escrIndex, evt);
                    counters[cnt] = evt;
                    return res;
                }
            }
        }
        throw new IllegalArgumentException("No free ESCR for event " + evt);
    }

    /**
     * Find a free counter for the given ESCR.
     *
     * @param counters
     * @param escr
     * @return -1 If not found
     */
    private final int findFreeCounter(P4Event[] counters, ESCR_RES escr) {
        for (byte cnt : escr.counters) {
            if (counters[cnt] == null) {
                return cnt;
            }
        }
        return -1;
    }

    /**
     * @see org.jnode.vm.x86.performance.X86PerformanceCounters#stopCounters(org.jnode.vm.x86.VmX86Thread)
     */
    protected void stopCounters(VmX86Thread thread)
        throws IllegalArgumentException {
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
