/*
 * $Id$
 */
package org.jnode.driver.input;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.util.ArrayList;
import java.util.Iterator;

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
	private final Logger log = Logger.getLogger(getClass());
	final ByteBuffer buf = ByteBuffer.allocate(1);
	ByteChannel channel;
	PointerInterpreter interpreter;
	private PointerDaemon daemon;
	private final ArrayList listeners = new ArrayList();

	/**
	 * Add a pointer listener
	 * 
	 * @param l
	 */
	public void addPointerListener(PointerListener l) {
		listeners.add(l);
	}

	/**
	 * Remove a pointer listener
	 * 
	 * @param l
	 */
	public void removePointerListener(PointerListener l) {
		listeners.remove(l);
	}

	/**
	 * Start the pointer device.
	 */
	protected synchronized void startDevice() throws DriverException {
		final Device dev = getDevice();
		log.info("Starting " + dev.getId());
		channel = getChannel();
		interpreter = createInterpreter();

		// start the deamon anyway, so we can register a mouse later
		daemon = new PointerDaemon(dev.getId() + "-daemon");
		daemon.start();
		dev.registerAPI(PointerAPI.class, this);
	}

	protected PointerInterpreter createInterpreter() {
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
		for (Iterator i = listeners.iterator(); i.hasNext();) {
			PointerListener l = (PointerListener) i.next();
			l.pointerStateChanged(event);
			if (event.isConsumed()) {
				break;
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
