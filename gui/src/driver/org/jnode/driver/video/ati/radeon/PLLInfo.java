/*
 * $Id$
 */
package org.jnode.driver.video.ati.radeon;

import org.jnode.system.MemoryResource;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class PLLInfo implements RadeonConstants {

	private final int maxPllFreq;

	private final int minPllFreq;

	private final int xclk;

	private final int ref_div;

	private final int ref_clk;

	/**
	 * Initialize this instance with default values based on the architecture.
	 * 
	 * @param architecture
	 */
	PLLInfo(int architecture) {
		// Depends on card, use the lowest for now
		this.maxPllFreq = 35000;
		this.minPllFreq = 12500;
		switch (architecture) {
		case Architecture.R200:
			this.ref_clk = 2700;
			this.ref_div = 12;
			this.xclk = 27500;
			break;
		case Architecture.RV250:
			this.ref_clk = 2700;
			this.ref_div = 12;
			this.xclk = 24975;
			break;
		case Architecture.RV200:
			this.ref_clk = 2700;
			this.ref_div = 12;
			this.xclk = 23000;
			break;
		default:
			this.ref_clk = 2700;
			this.ref_div = 67;
			this.xclk = 16615;
		}
	}

	/**
	 * Initialize this instance from the BIOS ROM.
	 * 
	 * @param biosRom
	 */
	PLLInfo(MemoryResource biosRom) {
		final int biosHdr = biosRom.getChar(0x48);
		final int pllInfo = biosRom.getChar(biosHdr + 0x30);

		this.xclk = biosRom.getChar(pllInfo + 0x08);
		this.ref_clk = biosRom.getChar(pllInfo + 0x0e);
		this.ref_div = biosRom.getChar(pllInfo + 0x10);
		this.minPllFreq = biosRom.getInt(pllInfo + 0x12);
		this.maxPllFreq = biosRom.getInt(pllInfo + 0x16);
	}

	/**
	 * @return Returns the ppll_max.
	 */
	public final int getMaxPllFreq() {
		return this.maxPllFreq;
	}

	/**
	 * @return Returns the ppll_min.
	 */
	public final int getMinPllFreq() {
		return this.minPllFreq;
	}

	/**
	 * @return Returns the ref_clk.
	 */
	public final int getRef_clk() {
		return this.ref_clk;
	}

	/**
	 * @return Returns the ref_div.
	 */
	public final int getRef_div() {
		return this.ref_div;
	}

	/**
	 * @return Returns the xclk.
	 */
	public final int getXclk() {
		return this.xclk;
	}
	
	/**
	 * Convert to a string representation.
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "ref_clk:" + ref_clk + 
		", ref_div:" + ref_div + 
		", minPllFreq:" + minPllFreq +
		", maxPllFreq:" + maxPllFreq +
		", xclk:" + xclk;
	}
}