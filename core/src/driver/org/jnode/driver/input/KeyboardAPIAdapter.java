/*
 * $Id$
 */
package org.jnode.driver.input;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class KeyboardAPIAdapter implements KeyboardAPI {

	/** All listeners */
	private final ArrayList listeners = new ArrayList();
	/** The interpreter */
	private KeyboardInterpreter interpreter = null/*new KeyboardInterpreter()*/;

	/**
	 * @see org.jnode.driver.input.KeyboardAPI#addKeyboardListener(org.jnode.driver.input.KeyboardListener)
	 */
	public synchronized void addKeyboardListener(KeyboardListener l) {
		listeners.add(l);
	}

	/**
	 * @see org.jnode.driver.input.KeyboardAPI#getKbInterpreter()
	 */
	public KeyboardInterpreter getKbInterpreter() {
		return interpreter;
	}

	/**
	 * @see org.jnode.driver.input.KeyboardAPI#removeKeyboardListener(org.jnode.driver.input.KeyboardListener)
	 */
	public synchronized void removeKeyboardListener(KeyboardListener l) {
		listeners.remove(l);
	}

	/**
	 * @see org.jnode.driver.input.KeyboardAPI#setKbInterpreter(org.jnode.driver.input.KeyboardInterpreter)
	 */
	public void setKbInterpreter(KeyboardInterpreter kbInterpreter) {
		if (kbInterpreter == null) {
			throw new IllegalArgumentException("kbInterpreter==null");
		}
		this.interpreter = kbInterpreter;
	}

	/**
	 * Remove all listeners.
	 */
	public synchronized void clear() {
		listeners.clear();
	}

	/**
	 * Fire a given pointer event to all known listeners.
	 *
	 * @param event
	 */
	public synchronized void fireEvent(KeyboardEvent event) {
		if (event != null) {
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
	}
}
