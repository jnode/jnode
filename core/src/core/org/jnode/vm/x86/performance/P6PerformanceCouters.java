/*
 * $Id$
 */
package org.jnode.vm.x86.performance;

import static org.jnode.vm.performance.PresetEvent.*;

import org.jnode.vm.x86.VmX86Processor;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class P6PerformanceCouters extends DualMSRPerformanceCounters {

    private static final int[] SELECT_MSR_INDEXES = { 0x186, 0x187 };

    private static final int[] COUNT_MSR_INDEXES = { 0xC1, 0xC2 };

    private static final DualMSREvent[] EVENTS = {
            new DualMSREvent(BR_INS, 0xC4, 0x00),
            new DualMSREvent(BR_MIS, 0xC5, 0x00),
            new DualMSREvent(BR_TKN, 0xC9, 0x00),
            new DualMSREvent(BR_TKN_MIS, 0xCA, 0x00), };

    /**
     * @param cpu
     * @param selectMSRIndexes
     * @param countMSRIndexes
     * @param events
     */
    public P6PerformanceCouters(VmX86Processor cpu) {
        super(cpu, SELECT_MSR_INDEXES, COUNT_MSR_INDEXES, EVENTS);
    }

}
