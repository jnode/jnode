/*
 * $Id$
 */
package org.jnode.driver.ps2;

import java.io.IOException;
import java.nio.channels.ByteChannel;

import org.jnode.driver.DriverException;
import org.jnode.driver.input.AbstractPointerDriver;
import org.jnode.system.IRQResource;
import org.jnode.util.TimeoutException;

/**
 * @author qades
 */
public class PS2PointerDriver extends AbstractPointerDriver implements PS2Constants {

	static final int CMD_SET_RES = 0xE8; /* Set resolution */
	static final int CMD_SET_SCALE11 = 0xE6; /* Set 1:1 scaling */
	static final int CMD_SET_SCALE21 = 0xE7; /* Set 2:1 scaling */
	static final int CMD_GET_SCALE = 0xE9; /* Get scaling factor */
	static final int CMD_SET_STREAM = 0xEA; /* Set stream mode */

	private final PS2Bus bus;
	private final PS2ByteChannel channel;
	private IRQResource irq;

	PS2PointerDriver(PS2Bus ps2) {
		this.bus = ps2;
		this.channel = ps2.getMouseChannel();
	}

	protected int getIRQ() {
		return MOUSE_IRQ;
	}

	protected boolean initPointer() {
		boolean result = enablePointer();
		result &= setRate(100);
		return result;
	}

	protected boolean enablePointer() {
		return bus.writeMouseCommand(CMD_ENABLE);
	}

	protected boolean disablePointer() {
		return bus.writeMouseCommand(CMD_DISABLE);
	}

	protected int getPointerId() throws DriverException {
		if (!bus.writeMouseCommand(CMD_GET_ID)) {
			throw new DriverException("Cannot request Pointer ID");
		}
		try {
			return channel.read(50);
		} catch (IOException ex) {
			throw new DriverException("Error in requesting Pointer ID", ex);
		} catch (TimeoutException ex) {
			throw new DriverException("Timeout in requesting Pointer ID", ex);
		} catch (InterruptedException ex) {
			throw new DriverException("Interrupted in requesting Pointer ID", ex);
		}
	}

	protected boolean setRate(int samples) {
		return bus.writeMouseCommands(new int[] { CMD_SET_RATE, samples });
	}

	/**
	 * @see org.jnode.driver.input.AbstractPointerDriver#getChannel()
	 */
	protected ByteChannel getChannel() {
		return channel;
	}

	/**
	 * @see org.jnode.driver.Driver#startDevice()
	 */
	protected synchronized void startDevice() throws DriverException {
		irq = bus.claimResources(getDevice(), MOUSE_IRQ);
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
