/*
 * $Id$
 */
package org.jnode.awt.swingpeers;

import java.awt.Component;
import java.awt.Container;
import java.awt.Insets;
import java.awt.peer.ContainerPeer;

import javax.swing.JComponent;

import org.jnode.awt.JNodeToolkit;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
class SwingContainerPeer extends SwingComponentPeer implements ContainerPeer, ISwingContainerPeer {

	/**
	 * @param toolkit
	 * @param component
	 * @param peer
	 */
	public SwingContainerPeer(JNodeToolkit toolkit, Container component,
			JComponent peer) {
		super(toolkit, component, peer);
	}

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
	 * @see java.awt.peer.ContainerPeer#getInsets()
	 */
	public Insets getInsets() {
		return jComponent.getInsets();
	}

	/**
	 * @see java.awt.peer.ContainerPeer#insets()
	 */
	public Insets insets() {
		return jComponent.getInsets();
	}

	/**
	 * @see java.awt.peer.ContainerPeer#isPaintPending()
	 */
	public boolean isPaintPending() {
		return false;
	}
	
	/**
	 * @see org.jnode.awt.swingpeers.ISwingContainerPeer#addAWTComponent(java.awt.Component,
	 *      javax.swing.JComponent)
	 */
	public void addAWTComponent(Component awtComponent, JComponent peer) {
		jComponent.add(peer);
	}
}
