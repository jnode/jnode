/*
 * $Id$
 */
package org.jnode.system;

/**
 * Hardware Interrupt Handler interface.
 * 
 * An interrupt handler is called from the kernel with interrupts disabled. So keep and handling
 * here as short as possible!
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface IRQHandler {

	/**
	 * Handle a given hardware interrupt. This method is called from the kernel with interrupts
	 * disabled. So keep and handling here as short as possible!
	 * @param irq
	 */
	public void handleInterrupt(int irq);

}
