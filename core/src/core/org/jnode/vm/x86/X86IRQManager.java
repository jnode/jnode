/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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
 
package org.jnode.vm.x86;

import org.jnode.annotation.KernelSpace;
import org.jnode.annotation.Uninterruptible;
import org.jnode.vm.scheduler.IRQManager;

final class X86IRQManager extends IRQManager {

    /**
     * Number of IRQ vectors
     */
    static final int IRQ_COUNT = 16;

    /**
     * Programmable interrupt controller
     */
    private final PIC8259A pic8259a;

    /**
     * @param irqCount
     */
    X86IRQManager(VmX86Processor cpu, PIC8259A pic8259a) {
        super(cpu.getIrqCounters(), cpu);
        this.pic8259a = pic8259a;
    }

    /**
     * Set an End Of Interrupt message to the 8259 interrupt controller(s).
     *
     * @param irq
     */
    @Uninterruptible
    @KernelSpace
    protected final void eoi(int irq) {
        pic8259a.eoi(irq);
    }

}
