/*
 * $Id$
 */
package org.jnode.driver.video.vgahw;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class VgaUtils {

	public static void screenOff(VgaIO io) {
		io.setSEQ(0, 1);
		io.setSEQ(1, io.getSEQ(1) | 0x20);
		io.setSEQ(0, 3);
		io.getSTAT();
		io.setATTIndex(0);
	}

	public static void screenOn(VgaIO io) {
		io.setSEQ(0, 1);
		io.setSEQ(1, io.getSEQ(1) & 0xDF);
		io.setSEQ(0, 3);
		io.getSTAT();
		io.setATTIndex(0x20);
	}

	public static void setColorMode(VgaIO io) {
		io.setMISC(io.getMISC() | 0x01);
	}

	public static void setMonoMode(VgaIO io) {
		io.setMISC(io.getMISC() & 0xFE);
	}

	/**
	 * Disable access to CRTC registers 0x00-0x07
	 * @param io
	 */	
	public static void lockCRTC(VgaIO io) {
		io.setCRT(0x11, io.getCRT(0x11) | 0x80);
	}

	/**
	 * Enable access to CRTC registers 0x00-0x07
	 * @param io
	 */	
	public static void unlockCRTC(VgaIO io) {
		io.setCRT(0x11, io.getCRT(0x11) & ~0x80);
	}

}
