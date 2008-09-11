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

import static org.jnode.vm.performance.PresetEvent.BR_INS;
import static org.jnode.vm.performance.PresetEvent.BR_NTK;
import static org.jnode.vm.performance.PresetEvent.BR_NTK_MIS;
import static org.jnode.vm.performance.PresetEvent.BR_TKN;
import static org.jnode.vm.performance.PresetEvent.BR_TKN_MIS;
import static org.jnode.vm.performance.PresetEvent.TLB_DM;
import static org.jnode.vm.performance.PresetEvent.TLB_IM;
import static org.jnode.vm.performance.PresetEvent.TLB_TL;
import static org.jnode.vm.performance.PresetEvent.TOT_CYC;
import static org.jnode.vm.performance.PresetEvent.TOT_INS;

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
        new CounterMSRInfo(0x310, 0x370), new CounterMSRInfo(0x311, 0x371)};

    private static final int MMNP = 0x01; // Branches not taken predicted
    private static final int MMNM = 0x02; // Branches not taken mispredicted
    private static final int MMTP = 0x04; // Branches taken predicted
    private static final int MMTM = 0x08; // Branches taken mispredicted

    private static final ESCR_RES[] CRU_ESCR01 = new ESCR_RES[]{
        new ESCR_RES(CRU_ESCR0, 12, 13, 16),
        new ESCR_RES(CRU_ESCR1, 14, 15, 17)};

    private static final ESCR_RES[] CRU_ESCR23 = new ESCR_RES[]{
        new ESCR_RES(CRU_ESCR2, 12, 13, 16),
        new ESCR_RES(CRU_ESCR3, 14, 15, 17)};

    private static final ESCR_RES[] FSB_ESCR01 = new ESCR_RES[]{
        new ESCR_RES(FSB_ESCR0, 0, 1),
        new ESCR_RES(FSB_ESCR1, 2, 3)};

    private static final ESCR_RES[] PMH_ESCR01 = new ESCR_RES[]{
        new ESCR_RES(PMH_ESCR0, 0, 1),
        new ESCR_RES(PMH_ESCR1, 2, 3)};

    private static final P4Event[] EVENTS = {
        new P4Event(BR_INS, 0x6, MMNP | MMNM | MMTP | MMTM, 0x05, CRU_ESCR23),
        new P4Event(BR_INS, 0x6, MMNM | MMTM, 0x05, CRU_ESCR23),
        new P4Event(BR_TKN, 0x6, MMTP | MMTM, 0x05, CRU_ESCR23),
        new P4Event(BR_TKN_MIS, 0x6, MMTM, 0x05, CRU_ESCR23),
        new P4Event(BR_NTK, 0x6, MMNP | MMNM, 0x05, CRU_ESCR23),
        new P4Event(BR_NTK_MIS, 0x6, MMNM, 0x05, CRU_ESCR23),
        new P4Event(TLB_DM, 0x1, 0x01, 0x04, PMH_ESCR01),
        new P4Event(TLB_IM, 0x1, 0x02, 0x04, PMH_ESCR01),
        new P4Event(TLB_TL, 0x1, 0x03, 0x04, PMH_ESCR01),
        new P4Event(TOT_CYC, 0x13, 0x01, 0x06, FSB_ESCR01),
        new P4Event(TOT_INS, 0x02, 0x01, 0x04, CRU_ESCR01),
    };

    /**
     * @param cpu
     * @param counters
     */
    public Pentium4PerformanceCounters(VmX86Processor cpu) {
        super(cpu, MSR_INFOS, EVENTS);
    }
}
