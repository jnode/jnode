/*
 * $Id$
 */
package org.jnode.driver.net.lance;

import org.jnode.driver.net.ethernet.Flags;

/**
 * @author epr
 */
public class LanceFlags implements Flags {

	private final String name;
	private String chipName;
	private boolean fullDuplex;
	private boolean autoSelectEnabled;
	private boolean mustUnreset;

	/**
	 * Create a new instance
	 */
	public LanceFlags(String name) {
		this.name = name;
		this.chipName = "Unknown";
		this.fullDuplex = false;
		this.autoSelectEnabled = false;
		this.mustUnreset = false;
	}

	public void setForVersion(int chipVersion) {
		// TODO the flags for the different versions are not completed and the LanceCore doesn't use these
		switch (chipVersion) {
			case 0x2420 :
				chipName = "PCnet/PCI 79C970";
				break;
			case 0x2430 :
				chipName = "PCnet/PCI 79C970 or PCnet/32 79C965";
				break;
			case 0x2621 :
				chipName = "PCnet/PCI II 79C970A";
				fullDuplex = true;
				break;
			case 0x2623 :
				chipName = "PCnet/FAST 79C971";
				fullDuplex = true;
				break;
			case 0x2624 :
				chipName = "PCnet/FAST+ 79C972";
				fullDuplex = true;
				break;
			case 0x2625 :
				chipName = "PCnet/FAST III 79C973";
				fullDuplex = true;
				break;
			case 0x2626 :
				chipName = "PCnet/Home 79C978";
				fullDuplex = true;
				break;
			case 0x2627 :
				chipName = "PCnet/FAST III 79C970";
				fullDuplex = true;
				break;
			default :
				chipName = "no device !";
		}

	}

	/**
	 * Gets the name of the device
	 */
	public String getName() {
		return name;
	}

	public String getChipName() {
		return chipName;
	}

	public boolean isFullDuplex() {
		return fullDuplex;
	}

	public boolean isAutoSelectEnabled() {
		return autoSelectEnabled;
	}

	public boolean isMustUnreset() {
		return mustUnreset;
	}

}
