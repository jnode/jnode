/*
 * $Id$
 */
package org.jnode.driver.input;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.log4j.Logger;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class PointerAPIAdapter implements PointerAPI {

	/** My logger */
	private static final Logger log = Logger.getLogger(PointerAPIAdapter.class);
	
	/** All listeners */
	private final ArrayList listeners = new ArrayList();

	/**
	 * Add a pointer listener
	 * @param l
	 */
	public synchronized void addPointerListener(PointerListener l) {
		listeners.add(l);
	}

	/**
	 * Remove a pointer listener
	 * @param l
	 */
	public synchronized void removePointerListener(PointerListener l) {
		listeners.remove(l);
	}

	/**
	 * Remove all listeners.
	 */
	public synchronized void clear() {
		listeners.clear();
	}
	
	/**
	 * Fire a given pointer event to all known listeners.
	 * @param event
	 */
	public synchronized void fireEvent(PointerEvent event) {
		for (Iterator i = listeners.iterator(); i.hasNext();) {
			PointerListener l = (PointerListener) i.next();
			try {
				l.pointerStateChanged(event);
			} catch (Throwable ex) {
				log.error("Exception in PointerListener", ex);
			}
			if (event.isConsumed()) {
				break;
			}
		}
	}
}
