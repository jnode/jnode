/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
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
