/*
 * $Id$
 */
package org.jnode.driver.ps2;

import java.nio.channels.ByteChannel;

import org.apache.log4j.Logger;
import org.jnode.driver.DriverException;
import org.jnode.driver.input.AbstractKeyboardDriver;
import org.jnode.system.IRQResource;
import org.jnode.util.NumberUtils;

/**
 * @author qades
 */
public class PS2KeyboardDriver extends AbstractKeyboardDriver implements PS2Constants {

	private final PS2Bus bus;
	private final PS2ByteChannel channel;
	private IRQResource irq;
	private final Logger log = Logger.getLogger(getClass());

	PS2KeyboardDriver(PS2Bus ps2) {
		this.bus = ps2;
		this.channel = ps2.getKbChannel();
	}

	/**
	 * @see org.jnode.driver.input.AbstractKeyboardDriver#getChannel()
	 */
	protected ByteChannel getChannel() {
		return channel;
	}

	/**
	 * @see org.jnode.driver.Driver#startDevice()
	 */
	protected synchronized void startDevice() throws DriverException {
		// Claim the irq
		irq = bus.claimResources(getDevice(), KB_IRQ);
		// Set the mode
		setEnabled(true);
		// Start the rest
		super.startDevice();
	}

	/**
	 * @see org.jnode.driver.Driver#stopDevice()
	 */
	protected synchronized void stopDevice() throws DriverException {
		// Set the mode
		setEnabled(false);
		// Stop everything
		super.stopDevice();
		irq.release();
		irq = null;
		bus.releaseResources();
	}

	private final void setEnabled(boolean on) {
		// Set the mode
		final int mode = bus.getMode();
		int newMode = mode;
		log.debug("Old mode 0x" + NumberUtils.hex(mode, 2));
		if (on) {
			newMode |= MODE_INT;
			newMode &= ~MODE_DISABLE_KBD;
		} else {
			newMode &= ~MODE_INT;
			newMode |= MODE_DISABLE_KBD;
		}
		if (mode != newMode) {
			bus.setMode(newMode);
			log.info("New mode 0x" + NumberUtils.hex(bus.getMode(), 2));
		}
	}
}
