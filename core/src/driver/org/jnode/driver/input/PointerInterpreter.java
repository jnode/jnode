/*
 * $Id$
 */
package org.jnode.driver.input;


/**
 * @author qades
 */
public interface PointerInterpreter {

	/**
	 * Probe for a suitable protocol.
	 * @param d
	 * @return True if an protocol was found, false otherwise.
	 */
	public boolean probe(AbstractPointerDriver d);
	
	/**
	 * Gets the name of this interpreter.
	 * @return String
	 */
	public String getName();
	
	/**
	 * Process a given byte from the device.
	 * @param scancode
	 * @return A valid event, or null
	 */
	public PointerEvent handleScancode(int scancode);

	/**
	 * Reset the state of this interpreter.
	 */
	public void reset();
	
}
