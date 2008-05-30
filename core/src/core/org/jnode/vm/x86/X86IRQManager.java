/*
 * $Id$
 */
package org.jnode.vm.x86;

import org.jnode.vm.annotation.KernelSpace;
import org.jnode.vm.annotation.Uninterruptible;
import org.jnode.vm.scheduler.IRQManager;

final class X86IRQManager extends IRQManager {

    /**
     * Number of IRQ vectors
     */
    final static int IRQ_COUNT = 16;

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
