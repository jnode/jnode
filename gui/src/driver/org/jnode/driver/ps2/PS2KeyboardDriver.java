/*
 * $Id$
 */
package org.jnode.driver.ps2;

import java.nio.channels.ByteChannel;

import org.apache.log4j.Logger;
import org.jnode.driver.DeviceException;
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
		try {
			// Set the mode
			setEnabled(true);
		} catch (DeviceException ex) {
			throw new DriverException("Cannot enable keyboard", ex);
		}
		// Start the rest
		super.startDevice();
		// Make sure all queues are empty
		bus.processQueues();
	}

	/**
	 * @see org.jnode.driver.Driver#stopDevice()
	 */
	protected synchronized void stopDevice() throws DriverException {
		// Set the mode		
		try {
			setEnabled(false);
		} catch (DeviceException ex) {
			log.debug("Error disabling keyboard", ex);
		}
		// Stop everything
		super.stopDevice();
		irq.release();
		irq = null;
		bus.releaseResources();
	}

	private final void setEnabled(boolean on) throws DeviceException {
		log.debug("Old mode 0x" + NumberUtils.hex(bus.getMode(), 2));
		bus.setKeyboardEnabled(on);
		log.debug("New mode 0x" + NumberUtils.hex(bus.getMode(), 2));
	}
}
