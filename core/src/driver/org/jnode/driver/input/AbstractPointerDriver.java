/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.driver.input;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceException;
import org.jnode.driver.Driver;
import org.jnode.driver.DriverException;

/**
 * @author qades
 */
public abstract class AbstractPointerDriver extends Driver implements PointerAPI {

	/** My logger */
	private static final Logger log = Logger.getLogger(AbstractPointerDriver.class);
	private ByteChannel channel;
	private PointerInterpreter interpreter;
	private PointerDaemon daemon;
	private final ArrayList<PointerListener> listeners = new ArrayList<PointerListener>();

	/**
	 * Add a pointer listener
	 * 
	 * @param l
	 */
	public synchronized void addPointerListener(PointerListener l) {
		listeners.add(l);
	}

	/**
	 * Remove a pointer listener
	 * 
	 * @param l
	 */
	public synchronized void removePointerListener(PointerListener l) {
		listeners.remove(l);
	}

	/**
	 * Start the pointer device.
	 */
	protected synchronized void startDevice() throws DriverException {
		final Device dev = getDevice();
		log.debug("Starting " + dev.getId());
		this.channel = getChannel();
		this.interpreter = createInterpreter();
		try {
			setRate(80);
		} catch (DeviceException ex) {
			log.error("Cannot set default rate", ex);
		}

		// start the deamon anyway, so we can register a mouse later
		daemon = new PointerDaemon(dev.getId() + "-daemon");
		daemon.start();
		dev.registerAPI(PointerAPI.class, this);
	}

	protected PointerInterpreter createInterpreter() {
	    log.debug("createInterpreter");
		try {
			initPointer(); // bring mouse into stable state
		} catch (DeviceException ex) {
			log.error("Cannot initialize pointer", ex);
			return null;
		}

		PointerInterpreter i = new MouseInterpreter();
		if (i.probe(this)) {
			log.info("Found " + i.getName());
			return i;
		} else {
		    try {
                Thread.sleep(20000);
            } catch (InterruptedException ex1) {
            }
			// here goes the tablet stuff
			return null;
		}
	}

	/**
	 * Stop the pointer device.
	 */
	protected synchronized void stopDevice() throws DriverException {
		getDevice().unregisterAPI(PointerAPI.class);
		PointerDaemon daemon = this.daemon;
		this.daemon = null;
		if (daemon != null) {
			daemon.interrupt();
		}

		try {
			channel.close();
			channel = null;
		} catch (IOException ex) {
			System.err.println("Error closing Pointer channel: " + ex.toString());
		}
	}

	/**
	 * Dispatch a given pointer event to all known listeners.
	 * 
	 * @param event
	 */
	protected void dispatchEvent(PointerEvent event) {
		for (PointerListener l : listeners) {
			l.pointerStateChanged(event);
			if (event.isConsumed()) {
				break;
			}
		}
	}

	/**
	 * Read scancodes from the input channel and dispatch them as events.
	 */
	final void processChannel() {
		final ByteBuffer buf = ByteBuffer.allocate(1);
		while ((channel != null) && channel.isOpen()) {
			try {
				buf.rewind();
				if (channel.read(buf) != 1) {
					continue;
				}
				final byte scancode = buf.get(0);
				if (interpreter != null) {
					final PointerEvent event = interpreter.handleScancode(scancode & 0xff);
					if (event != null) {
						//log.debug(event);
						dispatchEvent(event);
					}
				}
			} catch (Throwable ex) {
				ex.printStackTrace();
			}
		}
	}
	
	/**
	 * PointerDaemon that translates scancodes to MouseEvents and dispatches those events.
	 */
	class PointerDaemon extends Thread {
	
		public PointerDaemon(String name) {
			super(name);
		}

		public void run() {
			processChannel();
		}
	}

	/**
	 * @return PointerInterpreter
	 */
	public PointerInterpreter getPointerInterpreter() {
		return interpreter;
	}

	/**
	 * Sets the Interpreter.
	 * 
	 * @param interpreter
	 *            the Interpreter
	 */
	public void setPointerInterpreter(PointerInterpreter interpreter) {
		if (interpreter == null)
			throw new NullPointerException();
		this.interpreter = interpreter;
	}

	/**
	 * Gets the byte channel. This is implementation specific
	 * 
	 * @return The byte channel
	 */
	protected abstract ByteChannel getChannel();
	protected abstract int getPointerId() throws DriverException;
	protected abstract boolean initPointer() throws DeviceException;
	protected abstract boolean enablePointer() throws DeviceException;
	protected abstract boolean disablePointer() throws DeviceException;
	protected abstract boolean setRate(int samples) throws DeviceException;

}
