/*
 * $Id$
 */
package org.jnode.vm.x86.performance;

import static org.jnode.vm.performance.PresetEvent.*;

import org.jnode.vm.x86.VmX86Processor;

final class Pentium4PerformanceCounters extends P4FamilyPerformanceCounters {

    private static final CounterMSRInfo[] MSR_INFOS = {
            new CounterMSRInfo(0x300, 0x360), new CounterMSRInfo(0x301, 0x361),
            new CounterMSRInfo(0x302, 0x362), new CounterMSRInfo(0x303, 0x363),
            new CounterMSRInfo(0x304, 0x364), new CounterMSRInfo(0x305, 0x365),
            new CounterMSRInfo(0x306, 0x366), new CounterMSRInfo(0x307, 0x367),
            new CounterMSRInfo(0x308, 0x368), new CounterMSRInfo(0x309, 0x369),
            new CounterMSRInfo(0x30A, 0x36A), new CounterMSRInfo(0x30B, 0x36B),
            new CounterMSRInfo(0x30C, 0x36C), new CounterMSRInfo(0x30D, 0x36D),
            new CounterMSRInfo(0x30E, 0x36E), new CounterMSRInfo(0x30F, 0x36F),
            new CounterMSRInfo(0x310, 0x370), new CounterMSRInfo(0x311, 0x371) };

    private static final P4Event[] EVENTS = { 
        new P4Event(TOT_INS, 0x02, 0x01, 0x04, 
                new ESCR_RES(CRU_ESCR0, 12, 13, 16), 
                new ESCR_RES(CRU_ESCR1, 14, 15, 17)), 
    };

    /**
     * @param cpu
     * @param counters
     */
    public Pentium4PerformanceCounters(VmX86Processor cpu) {
        super(cpu, MSR_INFOS, EVENTS);
    }
}
