/*
 * $Id$
 */
package org.jnode.driver.video.spi;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class DpmsState {
    
	public static final DpmsState OFF = new DpmsState(false, false, false);
	
	private final boolean display;
	private final boolean hsync;
	private final boolean vsync;
	
	public DpmsState(boolean display, boolean hsync, boolean vsync) {
		this.display = display;
		this.hsync = hsync;
		this.vsync = vsync;
	}
	
	/**
	 * @return True if the display is enabled, false otherwise
	 */
	public final boolean isDisplay() {
		return this.display;
	}

	/**
	 * @return True if the hsync is enabled, false otherwise
	 */
	public final boolean isHsync() {
		return this.hsync;
	}

	/**
	 * @return True if the vsync is enabled, false otherwise
	 */
	public final boolean isVsync() {
		return this.vsync;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "DPMS[display:" + display + ", hsync:" + hsync + ", vsync:" + vsync + "]";
	}


}
