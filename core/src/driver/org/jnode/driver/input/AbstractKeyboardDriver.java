/*
 * $Id$
 */
package org.jnode.driver.input;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.util.ArrayList;
import java.util.Iterator;

import org.jnode.driver.Device;
import org.jnode.driver.Driver;
import org.jnode.driver.DriverException;

/**
 * @author epr
 */
public abstract class AbstractKeyboardDriver extends Driver implements KeyboardAPI {

	final ByteBuffer buf = ByteBuffer.allocate(1);
	ByteChannel channel;
	KeyboardInterpreter kbInterpreter;
	private KeyboardDaemon daemon;
	private InputStream kis;
	private final ArrayList listeners = new ArrayList();

	/**
	 * Add a keyboard listener
	 * @param l
	 */
	public void addKeyboardListener(KeyboardListener l) {
		listeners.add(l);
	}

	/**
	 * Remove a keyboard listener
	 * @param l
	 */
	public void removeKeyboardListener(KeyboardListener l) {
		listeners.remove(l);
	}

	/**
	 * Start the keyboard device.
	 */
	protected synchronized void startDevice() throws DriverException {
		this.channel = getChannel();
		this.kbInterpreter = createKeyboardInterpreter();

		final Device dev = getDevice();
		this.daemon = new KeyboardDaemon(dev.getId() + "-daemon");
		daemon.start();
		dev.registerAPI(KeyboardAPI.class, this);

		// If no inputstream has been defined, create and set one.
		if (System.in == null) {
			if (channel == null) {
				// even for keyboardless operation, we do need a System.in
				kis = new InputStream() {
					public int read() {
						return -1;
					}
				};
			} else {
				kis = new KeyboardInputStream(this);
			}
			System.setIn(kis);
		}
	}

	/**
	 * Gets the byte channel.
	 * This is implementation specific
	 * @return The byte channel
	 */
	protected abstract ByteChannel getChannel();
	
	/**
	 * Create an interpreter for this keyboard device
	 * @return The created interpreter
	 */
	protected KeyboardInterpreter createKeyboardInterpreter() {
		return KeyboardInterpreterFactory.getDefaultKeyboardInterpreter();
	}

	/**
	 * Stop the keyboard device.
	 */
	protected synchronized void stopDevice() throws DriverException {
		getDevice().unregisterAPI(KeyboardAPI.class);
		KeyboardDaemon daemon = this.daemon;
		if (System.in == kis) {
			System.setIn(null);
		}
		if (daemon != null) {
			daemon.interrupt();
		}
		this.daemon = null;

		try {
			channel.close();
		} catch (IOException ex) {
			System.err.println("Error closing Keyboard channel: " + ex.toString());
		}
	}

	/**
	 * Dispatch a given keyboard event to all known listeners.
	 * @param event
	 */
	protected void dispatchEvent(KeyboardEvent event) {
		//Syslog.debug("Dispatching event to " + listeners.size());
		for (Iterator i = listeners.iterator(); i.hasNext();) {
			KeyboardListener l = (KeyboardListener) i.next();
			if (event.isKeyPressed()) {
				l.keyPressed(event);
			} else if (event.isKeyReleased()) {
				l.keyReleased(event);
			}
			if (event.isConsumed()) {
				break;
			}
		}
	}

	/**
	 * KeyboardDaemon that translated scancodes to KeyboardEvents and dispatches
	 * those events.
		 * @author epr
	 */
	class KeyboardDaemon extends Thread {

		public KeyboardDaemon(String name) {
			super(name);
		}
		
		public void run() {
			while ((channel != null) && channel.isOpen()) {
				try {
					buf.rewind();
					if (channel.read(buf) != 1) {
						continue;
					}
					byte scancode = buf.get(0);
					//Syslog.debug("Interpreting " + scancode);
					KeyboardEvent event = kbInterpreter.interpretScancode(scancode & 0xff);
					if (event != null) {
						dispatchEvent(event);
					}
				} catch (Throwable ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	/**
	 * @return KeyboardInterpreter
	 */
	public KeyboardInterpreter getKbInterpreter() {
		return kbInterpreter;
	}

	/**
	 * Sets the kbInterpreter.
	 * @param kbInterpreter The kbInterpreter to set
	 */
	public void setKbInterpreter(KeyboardInterpreter kbInterpreter) {
		if (kbInterpreter == null) {
			throw new IllegalArgumentException("kbInterpreter==null");
		}
		this.kbInterpreter = kbInterpreter;
	}
}
