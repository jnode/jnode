/*
 * $Id$
 */
package org.jnode.awt.swingpeers;

import java.awt.Component;
import java.awt.Insets;
import java.awt.peer.ContainerPeer;
import java.awt.peer.LightweightPeer;

/**
 * AWT lightweight component peers that does nothing.
 */

final class SwingLightweightPeer extends SwingComponentPeer implements
		LightweightPeer, ContainerPeer, SwingPeer {
	private Insets containerInsets;

	//
	// Construction
	//

	public SwingLightweightPeer(Component component) {
		super(null, component);
	}

	//
	// LightweightPeer
	//
	/**
	 * @see java.awt.peer.ContainerPeer#beginLayout()
	 */
	public void beginLayout() {
		// TODO Auto-generated method stub

	}

	/**
	 * @see java.awt.peer.ContainerPeer#beginValidate()
	 */
	public void beginValidate() {
		// TODO Auto-generated method stub

	}

	/**
	 * @see java.awt.peer.ContainerPeer#endLayout()
	 */
	public void endLayout() {
		// TODO Auto-generated method stub

	}

	/**
	 * @see java.awt.peer.ContainerPeer#endValidate()
	 */
	public void endValidate() {
		// TODO Auto-generated method stub

	}

	/**
	 * @see org.jnode.awt.swingpeers.SwingPeer#getAWTComponent()
	 */
	public Component getAWTComponent() {
		return component;
	}

	/**
	 * @see java.awt.peer.ContainerPeer#getInsets()
	 */
	public Insets getInsets() {
		if (containerInsets == null) {
			containerInsets = new Insets(0, 0, 0, 0);
		}
		return containerInsets;
	}

	/**
	 * @see java.awt.peer.ContainerPeer#insets()
	 */
	public Insets insets() {
		return getInsets();
	}

	/**
	 * @see java.awt.peer.ContainerPeer#isPaintPending()
	 */
	public boolean isPaintPending() {
		// TODO Auto-generated method stub
		return false;
	}
}