/*
 * $Id$
 */
package org.jnode.driver.net.ne2000;

/**
 * @author epr
 */
public class Ne2000Flags {
	
	/** Device name */
	private final String name;
	/** Size of internal memory */
	private final int memSize;
	private final boolean b16;
	
	/**
	 * Create a new instance 
	 * @param name Device name
	 */
	public Ne2000Flags(String name) {
		this.name = name;
		this.memSize = 16*1024;
		this.b16 = true;
	}

	/**
	 * Gets the name of the device
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets size of internal NIC memory in bytes
	 */
	public int getMemSize() {
		return memSize;
	}
	
	/**
	 * Use 16-bit data transfer?
	 */
	public boolean is16bit() {
		return b16;
	}

}
