/*
 * $Id$
 */
package org.jnode.driver.ps2;

import java.nio.channels.ByteChannel;

import org.jnode.driver.DriverException;
import org.jnode.driver.input.AbstractKeyboardDriver;
import org.jnode.system.IRQResource;

/**
 * @author qades
 */
public class PS2KeyboardDriver extends AbstractKeyboardDriver implements PS2Constants {

	private final PS2Bus bus;
	private final PS2ByteChannel channel;
	private IRQResource irq;

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
		irq = bus.claimResources(getDevice(), KB_IRQ);
		super.startDevice();
	}

	/**
	 * @see org.jnode.driver.Driver#stopDevice()
	 */
	protected synchronized void stopDevice() throws DriverException {
		super.stopDevice();
		irq.release();
		irq = null;
		bus.releaseResources();
	}
}
