/*
 * Written by Michael Klaus <qades@users.fourceforge.net>
 * 
 * This work is partially based on work by Ewout Prangsma <epr@users.sourceforge.net>
 * 
 * other parts are derived from the Linux source code linux/drivers/char/pc_keyb.c by Geert
 * Uytterhoeven Martin Mares Johan Myreen <jem@iki.fi> C. Scott Ananian
 * <cananian@alumni.princeton.edu>
 * 
 * $Id$
 */
package org.jnode.driver.ps2;

import java.io.IOException;

import javax.naming.NameNotFoundException;

import org.apache.log4j.Logger;
import org.jnode.driver.Bus;
import org.jnode.driver.DeviceException;
import org.jnode.driver.DriverException;
import org.jnode.naming.InitialNaming;
import org.jnode.system.IOResource;
import org.jnode.system.IRQHandler;
import org.jnode.system.IRQResource;
import org.jnode.system.ResourceManager;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.system.ResourceOwner;
import org.jnode.util.NumberUtils;
import org.jnode.util.TimeoutException;

/**
 * Provides common functionality shared by the drivers (read/write data/status)
 * 
 * @author qades
 */
public class PS2Bus extends Bus implements IRQHandler, PS2Constants {

	/** My logger */
	private final Logger log = Logger.getLogger(getClass());
	private IOResource ioResData;
	private IOResource ioResCtrl;
	private int activeCount = 0;
	private final PS2ByteChannel kbChannel = new PS2ByteChannel();
	private final PS2ByteChannel mouseChannel = new PS2ByteChannel();
	/** If true, the interrupt handler will read and process the input queue */
	private boolean irqReadQueue = true;

	/**
	 * Create a PS2 object.
	 */
	PS2Bus(Bus parent) {
		super(parent);
	}

	/**
	 * All necessary resources are claimed in this method.
	 * 
	 * @param the
	 *            driver for which the resources are to be claimed
	 * @return the IRQResource for the driver in question
	 */
	final synchronized IRQResource claimResources(ResourceOwner owner, int irq) throws DriverException {
		try {
			final ResourceManager rm;
			try {
				rm = (ResourceManager) InitialNaming.lookup(ResourceManager.NAME);
			} catch (NameNotFoundException ex) {
				throw new DriverException("Cannot find ResourceManager: ", ex);
			}
			if (ioResData == null) {
				ioResData = rm.claimIOResource(owner, PS2_DATA_PORT, 1);
				ioResCtrl = rm.claimIOResource(owner, PS2_CTRL_PORT, 1);
			}
			final IRQResource irqRes = rm.claimIRQ(owner, irq, this, true);
			if (activeCount == 0) {
				processQueues();
			}
			activeCount++;
			return irqRes;
		} catch (ResourceNotFreeException ex) {
			throw new DriverException("Cannot claim necessairy resources: ", ex);
		}
	}

	/**
	 * Release all resource held by this bus, only if all devices that depend on this bus have been
	 * stopped.
	 */
	final synchronized void releaseResources() {
		activeCount--;
		if (activeCount == 0) {
			ioResData.release();
			ioResCtrl.release();
			ioResData = null;
			ioResCtrl = null;
		}
	}

	/**
	 * Handles a PS/2 interrupt
	 * 
	 * @see org.jnode.system.IRQHandler#handleInterrupt(int)
	 */
	public synchronized final void handleInterrupt(int irq) {
		if (irqReadQueue) {
			processQueues();
		}
	}

	/**
	 * Read the queue until it is empty and process the read data.
	 */
	final void processQueues() {
		int status;
		//System.out.print('<');
		while (((status = readStatus()) & AUX_STAT_OBF) != 0) {
			final int data = readData();

			// determine which driver shall handle the scancode
			final PS2ByteChannel channel;
			if ((status & STAT_MOUSE_OBF) != 0) {
				channel = mouseChannel;
			} else {
				channel = kbChannel;
			}
			// if this driver is not registered, merely exit
			if (channel == null) {
				log.debug("Unhandled scancode 0x" + Integer.toHexString(data) + " status=0x" + Integer.toHexString(status));
			} else {
				// let the driver handle the scancode
				channel.handleScancode(data);
			}
		}
		//System.out.print('>');
	}

	/**
	 * Get the status of the PS/2 port.
	 * 
	 * @return the status byte
	 */
	final int readStatus() {
		return ioResCtrl.inPortByte(PS2_STAT_PORT) & 0xff;
	}

	/**
	 * Write a byte to the control port
	 * 
	 * @param b
	 */
	private void writeController(int b) throws DeviceException {
		waitWrite();
		ioResCtrl.outPortByte(PS2_CTRL_PORT, b);
	}

	/**
	 * Write a byte to the data port
	 * 
	 * @param b
	 */
	private final void writeData(int b) throws DeviceException {
		waitWrite();
		ioResData.outPortByte(PS2_DATA_PORT, b);
	}

	/**
	 * Wait for a non-ready inputbuffer.
	 */
	private final void waitWrite() throws DeviceException {
		int count = 0;
		while (count < 1000) {
			if ((readStatus() & STAT_IBF) == 0) {
				return;
			} else {
				count++;
				Thread.yield();
			}
		}
		throw new DeviceException("InputBuffer full");
	}

	/**
	 * Read a byte from the data port
	 * 
	 * @return
	 */
	private final int readData() {
		return ioResData.inPortByte(PS2_DATA_PORT) & 0xff;
	}

	/**
	 * Gets the mode register
	 * 
	 * @return int
	 */
	final int getMode() throws DeviceException {
		writeController(CCMD_READ_MODE);
		return readData();
	}

	/**
	 * Sets the mode register
	 */
	final void setMode(int mode) throws DeviceException {
		writeController(CCMD_WRITE_MODE);
		writeData(mode);
	}

	/**
	 * Test for the presence of a connected mouse device
	 * 
	 * @return true if a mouse is present, false if not
	 */
	final boolean testMouse() throws DeviceException {
		irqReadQueue = false;
		try {
			writeController(CCMD_TEST_MOUSE);
			final int status = readStatus();
			final int rc = readData();
			log.debug("testMouse rc=0x" + NumberUtils.hex(rc, 2) + ", status 0x" + NumberUtils.hex(status, 2));
			return (rc != 0xFF);
		} finally {
			irqReadQueue = true;
			processQueues();
		}
	}

	/**
	 * Activate/Deactivate the mouse
	 */
	final void setMouseEnabled(boolean enable) throws DeviceException {
		writeController(enable ? CCMD_MOUSE_ENABLE : CCMD_MOUSE_DISABLE);
	}

	/**
	 * Activate/Deactivate the keyboard
	 */
	final void setKeyboardEnabled(boolean enable) throws DeviceException {
		writeController(enable ? CCMD_KB_ENABLE : CCMD_KB_DISABLE);
	}

	/**
	 * Write a command to the mouse
	 * 
	 * @param cmd
	 * @return
	 */
	final boolean writeMouseCommand(int cmd) throws DeviceException {
		// First clear the mouse channel, otherwise we might read
		// old data back
		mouseChannel.clear();
		// Transmit the command
		writeController(CCMD_WRITE_MOUSE);
		writeData(cmd);

		int data;
		try {
			data = mouseChannel.read(50);
		} catch (IOException ex) {
			log.debug("IOException in readMouse", ex);
			return false;
		} catch (TimeoutException ex) {
			log.debug("Timeout in readMouse");
			return false;
		} catch (InterruptedException ex) {
			log.debug("Interrupted in readMouse");
			return false;
		}

		if (data == REPLY_ACK) {
			return true; // command acknowledged
		} else if (data == REPLY_RESEND) {
			log.debug("Mouse replied with RESEND");
		} else {
			log.debug("Invalid reply 0x" + Integer.toHexString(data));
		}

		return false; // on error
	}

	/**
	 * Write a series of commands to the mouse
	 * 
	 * @param cmds
	 * @return
	 */
	final boolean writeMouseCommands(int[] cmds) throws DeviceException {
		boolean ok = true;
		for (int i = 0; i < cmds.length; i++) {
			ok &= writeMouseCommand(cmds[i]);
		}
		return ok;
	}

	/**
	 * @return
	 */
	final PS2ByteChannel getKbChannel() {
		return this.kbChannel;
	}

	/**
	 * @return
	 */
	final PS2ByteChannel getMouseChannel() {
		return this.mouseChannel;
	}

}
