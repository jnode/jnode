/*
 * $Id$
 */
package org.jnode.driver.input;


/**
 * @author qades
 */
public interface PointerInterpreter {

	public boolean probe(AbstractPointerDriver d);
	public String getName();
	public PointerEvent handleScancode(int scancode);

}
