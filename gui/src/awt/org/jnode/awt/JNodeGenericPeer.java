/*
 * $Id$
 */
package org.jnode.awt;

import java.awt.EventQueue;
import java.awt.Toolkit;

/**
 * @author epr
 */
public class JNodeGenericPeer {

	protected final JNodeToolkit toolkit;
	private final Object awtObject;

	// Global event queue.
	protected static EventQueue q;

	public JNodeGenericPeer(JNodeToolkit toolkit, Object awtObject) {
		this.toolkit = toolkit;
		this.awtObject = awtObject;
		toolkit.incRefCount();
	}

	static void enableQueue(EventQueue sq) {
		if (q == null) {
			q = sq;
		}
	}
	
	/**
	 * @return
	 */
	public final Object getAwtObject() {
		return this.awtObject;
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
