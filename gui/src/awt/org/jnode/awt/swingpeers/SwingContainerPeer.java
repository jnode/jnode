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
 
package org.jnode.awt.swingpeers;

import org.jnode.awt.JNodeToolkit;

import javax.swing.JComponent;
import java.awt.Component;
import java.awt.Container;
import java.awt.Insets;
import java.awt.peer.ContainerPeer;

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
		return getInsets();
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
