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
 
package org.jnode.system;

/**
 * I/O port resource.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface IOResource extends Resource {

	/**
	 * Returns the length.
	 * @return int
	 */
	public abstract int getLength();

	/**
	 * Returns the startPort.
	 * @return int
	 */
	public abstract int getStartPort();

	/**
	 * Get the value of a 8-bit I/O port.
	 * @param portNr Absolute port number (not relative to startPort)
	 * @return The port value
	 */
	public abstract int inPortByte(int portNr);


	/**
	 * Get the value of a 16-bit I/O port.
	 * @param portNr Absolute port number (not relative to startPort)
	 * @return The port value
	 */
	public abstract int inPortWord(int portNr);

	/**
	 * Get the value of a 32-bit I/O port.
	 * @param portNr Absolute port number (not relative to startPort)
	 * @return The port value
	 */
	public abstract int inPortDword(int portNr);

	/**
	 * Set the value of a 8-bit I/O port.
	 * @param portNr Absolute port number (not relative to startPort)
	 * @param value
	 */
	public abstract void outPortByte(int portNr, int value);

	/**
	 * Set the value of a 16-bit I/O port.
	 * @param portNr Absolute port number (not relative to startPort)
	 * @param value
	 */
	public abstract void outPortWord(int portNr, int value);

	/**
	 * Set the value of a 32-bit I/O port.
	 * @param portNr Absolute port number (not relative to startPort)
	 * @param value
	 */
	public abstract void outPortDword(int portNr, int value);
}
