/*
 * $Id$
 */
package org.jnode.awt;

import java.awt.Dimension;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceUtils;
import org.jnode.driver.input.PointerAPI;
import org.jnode.driver.input.PointerEvent;
import org.jnode.driver.input.PointerListener;
import org.jnode.driver.video.HardwareCursorAPI;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class MouseHandler implements PointerListener {

	/** My logger */
	private final Logger log = Logger.getLogger(getClass());
	private int x;
	private int y;
	private final HardwareCursorAPI hwCursor;
	private final PointerAPI pointerAPI;
	private final Dimension screenSize;

	/**
	 * Create a new instance
	 * 
	 * @param fbDevice
	 * @param screenSize
	 */
	public MouseHandler(Device fbDevice, Dimension screenSize) {
		HardwareCursorAPI hwCursor = null;
		Device pointerDevice = null;
		PointerAPI pointerAPI = null;
		try {
			hwCursor = (HardwareCursorAPI) fbDevice.getAPI(HardwareCursorAPI.class);
		} catch (ApiNotFoundException ex) {
			log.info("No hardware-cursor found on device " + fbDevice.getId());
		}
		if (hwCursor != null) {
			try {
				final Collection pointers = DeviceUtils.getDevicesByAPI(PointerAPI.class);
				if (!pointers.isEmpty()) {
					pointerDevice = (Device) pointers.iterator().next();
					pointerAPI = (PointerAPI) pointerDevice.getAPI(PointerAPI.class);
				}
			} catch (ApiNotFoundException ex) {
				log.error("Strange...", ex);
			}
		}
		this.hwCursor = hwCursor;
		this.pointerAPI = pointerAPI;
		this.screenSize = screenSize;
		if (pointerAPI != null) {
			log.info("Using PointerDevice " + pointerDevice.getId());
			hwCursor.setCursorVisible(true);
			hwCursor.setCursorPosition(x, y);
			pointerAPI.addPointerListener(this);
		}
	}

	/**
	 * Close this handler
	 */
	public void close() {
		if (pointerAPI != null) {
			pointerAPI.removePointerListener(this);
		}
	}

	/**
	 * @param event
	 * @see org.jnode.driver.input.PointerListener#pointerStateChanged(org.jnode.driver.input.PointerEvent)
	 */
	public void pointerStateChanged(PointerEvent event) {
		x = Math.min(screenSize.width - 1, Math.max(0, x + event.getX()));
		y = Math.min(screenSize.height - 1, Math.max(0, y + event.getY()));
		hwCursor.setCursorPosition(x, y);
	}
}
