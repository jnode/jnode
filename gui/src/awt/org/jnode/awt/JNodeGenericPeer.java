/*
 * $Id$
 */
package org.jnode.awt;

import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.Component;

/**
 * @author epr
 */
public class JNodeGenericPeer {

	protected final JNodeToolkit toolkit;
	protected final Object component;

	// Global event queue.
	protected static EventQueue eventQueue;

	public JNodeGenericPeer(JNodeToolkit toolkit, Object component) {
		this.toolkit = toolkit;
		this.component = component;
		toolkit.incRefCount();
	}

	static void enableQueue(EventQueue sq) {
		if (eventQueue == null) {
			eventQueue = sq;
		}
	}
	
	/**
	 * @return
	 */
	public final Object getComponent() {
		return this.component;
	}

	/**
	 * @see java.awt.peer.ComponentPeer#getToolkit()
	 * @return The toolkit
	 */
	public final Toolkit getToolkit() {
		return toolkit;
	}

	/**
	 * Gets the implementation toolkit
	 * @return The toolkit
	 */
	public final JNodeToolkit getToolkitImpl() {
		return toolkit;
	}

	/**
	 * Destroy the peer and release all resource
	 */
	public void dispose() {
		toolkit.decRefCount(false);
	}
}
