/*
 * $Id$
 * 
 * Serial port driver Oct 15 2003, mgeisse
 */

package org.jnode.driver.serial;

import javax.naming.NameNotFoundException;

import org.jnode.driver.Driver;
import org.jnode.driver.Device;
import org.jnode.driver.DriverException;
import org.jnode.driver.character.ChannelAlreadyOwnedException;
import org.jnode.naming.InitialNaming;
import org.jnode.system.IOResource;
import org.jnode.system.ResourceManager;
import org.jnode.system.ResourceNotFreeException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;

/**
 * @author mgeisse
 */
public class SerialPortDriver extends Driver implements SerialPortAPI, ByteChannel
{
	private Device channelOwner;
	private IOResource port;
	private int basePort;

	/**
	 * Create new driver instance of this driver
	 */
	public SerialPortDriver(int basePort) {
		this.channelOwner = null;
		this.port = null;
		this.basePort = basePort;
	}

	protected void startDevice() throws DriverException {
		try {
			final ResourceManager rm = (ResourceManager) InitialNaming.lookup(ResourceManager.NAME);
			port = rm.claimIOResource(getDevice(), basePort, 8);
			configure(BAUD9600);
			getDevice().registerAPI(SerialPortAPI.class, this);
		} catch (NameNotFoundException nnfex) {
			throw new DriverException(nnfex);
		} catch (ResourceNotFreeException rnfex) {
			throw new DriverException(rnfex);
		}
	}

	protected void stopDevice() {
		port.release();
	}

	public ByteChannel getChannel(Device owner)
		throws ChannelAlreadyOwnedException
	{
		if (channelOwner != null)
			throw new ChannelAlreadyOwnedException (channelOwner);
		channelOwner = owner;
		return this;
	}

	public boolean isOpen() {
		return true;
	}

	public void close() {
	}

	public void configure(int divisor, int bits, boolean longStop, boolean parity, boolean pEven) {
		if (divisor < 1 || divisor > 65535)
			throw new IllegalArgumentException("invalid baud rate divisor: " + divisor);

		int control;
		switch (bits) {
			case 5 :
				control = 0;
				break;
			case 6 :
				control = 1;
				break;
			case 7 :
				control = 2;
				break;
			case 8 :
				control = 3;
				break;
			default :
				throw new IllegalArgumentException("invalid data block bits: " + bits);
		}

		if (longStop)
			control |= 4;
		if (parity)
			control |= 8;
		if (pEven)
			control |= 16;

		flush();
		port.outPortByte(basePort + 3, control | 128);
		port.outPortByte(basePort + 0, divisor);
		port.outPortByte(basePort + 1, divisor >> 8);
		port.outPortByte(basePort + 3, control);
	}

	public void configure(int divisor) {
		configure(divisor, 8, false, false, false);
	}

	public int readSingle() {
		// should detect overruns, maybe parity errors, framing errors, and
		// breaks.

		// FIXME: busy waiting for a bit block to arrive
		while ((port.inPortByte(basePort + 5) & 1) == 0);
		return port.inPortByte(basePort);
	}

	public void writeSingle(int value) {
		// FIXME: busy waiting for the transmitter buffer to be empty
		while ((port.inPortByte(basePort + 5) & 32) == 0);
		port.outPortByte(basePort, value);
	}

	/**
	 * Wait for all physically cached bit blocks to be sent. This flushes only
	 * the hardware buffers, but no waiting data from the input channel.
	 */
	private void flushHardware() {
		// FIXME: busy waiting for the holding and shift registers to be empty
		int b;
		do b = port.inPortByte(basePort + 5);
		while ((b & 96) != 96);
	}

	public void flush() {
		// FIXME: currently flushes only the hardware as input buffers are not
		// yet used. This is only relevant to non-blocking I/O.
		flushHardware();
	}

	/**
	 * @see java.nio.channels.ByteChannel#read(java.nio.ByteBuffer)
	 */
	public int read (ByteBuffer dst)
	{
		int n = dst.remaining ();
		for (int i=0; i<n; i++)
			dst.put ((byte)readSingle ());
		return n;
	}

	/**
	 * @see java.nio.channels.ByteChannel#write(ByteBuffer)
	 */
	public int write (ByteBuffer src)
	{
		int n = src.remaining ();
		for (int i=0; i<n; i++)
			writeSingle (src.get ());
		return n;
	}
}
