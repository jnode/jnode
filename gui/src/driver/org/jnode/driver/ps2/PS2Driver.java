/*
 * $Id$
 */
package org.jnode.driver.ps2;

import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NonWritableChannelException;

import org.jnode.driver.Device;
import org.jnode.driver.DeviceException;
import org.jnode.driver.Driver;
import org.jnode.driver.DriverException;
import org.jnode.driver.character.ChannelAlreadyOwnedException;
import org.jnode.driver.character.CharacterDeviceAPI;
import org.jnode.util.ByteQueue;

/**
 * @author qades
 */
abstract class PS2Driver extends Driver implements CharacterDeviceAPI, PS2Constants, ByteChannel {

	private final ByteQueue queue = new ByteQueue();
	private Device owner;

	protected final PS2Bus ps2;

	PS2Driver(PS2Bus ps2) {
		this.ps2 = ps2;
	}

	/**
	 * @see org.jnode.system.IRQHandler#handleInterrupt(int)
	 */
	public void handleScancode(int b) {
		queue.push((byte) b);
	}

	/**
	 * Start the PS/2 device.
	 */
	protected synchronized void startDevice() throws DriverException {
		init();
		getDevice().registerAPI(CharacterDeviceAPI.class, this);	// make sure it's at least a character device
	}

	/**
	 * Stop the PS/2 device.
	 */
	protected synchronized void stopDevice() throws DriverException {
		getDevice().unregisterAPI(CharacterDeviceAPI.class);
		deinit();
	}

	public synchronized ByteChannel getChannel(Device owner) throws ChannelAlreadyOwnedException, DeviceException {
		if (this.owner != null) {
			throw new ChannelAlreadyOwnedException(this.owner);
		} else {
			this.owner = owner;
			return this;
		}
	}

	public int read(ByteBuffer dst) throws ClosedChannelException {
		if (!isOpen()) {
			throw new ClosedChannelException();
		}

		// ToDo: proper exception handling (if end of queue -> IOException)
		int i;
		for (i = 0; i < dst.remaining(); i++) {
			dst.put(queue.pop());
		}
		return i;
	}

	public int write(ByteBuffer b) throws NonWritableChannelException {
		throw new NonWritableChannelException();
	}

	public boolean isOpen() {
		return (owner != null);
	}

	public synchronized void close() {
		owner = null;
	}

	protected abstract void init() throws DriverException;
	protected abstract void deinit();

	abstract int getIRQ();

}
