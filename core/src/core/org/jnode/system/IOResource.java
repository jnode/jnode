/**
 * $Id$
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
