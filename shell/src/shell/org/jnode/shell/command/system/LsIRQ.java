/*
 * $Id$
 */
package org.jnode.shell.command.system;

import org.jnode.vm.IRQManager;
import org.jnode.vm.Unsafe;
import org.jnode.vm.VmProcessor;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class LsIRQ {

	public static void main(String[] args) {
		final VmProcessor proc = Unsafe.getCurrentProcessor();
		final IRQManager irqMgr = proc.getIRQManager();
		final int max = irqMgr.getNumIRQs();
		for (int i = 0; i < max; i++) {
			System.out.println("IRQ" + i + "\t" + irqMgr.getIrqCount(i) + "\t" + irqMgr.getHandlerInfo(i));
		}
	}
}
