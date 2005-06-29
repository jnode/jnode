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

import java.awt.Component;
import java.awt.Container;
import java.awt.Insets;
import java.awt.peer.ContainerPeer;

import javax.swing.JComponent;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
abstract class SwingContainerPeer<awtT extends Container, swingPeerT extends JComponent>
        extends SwingComponentPeer<awtT, swingPeerT> implements ContainerPeer,
        ISwingContainerPeer {

	/**
	 * @param toolkit
	 * @param component
	 * @param swingPeer
	 */
	public SwingContainerPeer(SwingToolkit toolkit, awtT component,
			swingPeerT swingPeer) {
		super(toolkit, component, swingPeer);
	}

	/**
	 * @see java.awt.peer.ContainerPeer#beginLayout()
	 */
	public final void beginLayout() {
		// Ignore
	}

    /**
     * @see java.awt.peer.ContainerPeer#endLayout()
     */
    public final void endLayout() {
        // Ignore
    }

	/**
	 * @see java.awt.peer.ContainerPeer#beginValidate()
	 */
	public void beginValidate() {
        ((ISwingPeer<awtT>)jComponent).validatePeerOnly();
	}

	/**
	 * @see java.awt.peer.ContainerPeer#endValidate()
	 */
	public void endValidate() {
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
	public final Insets insets() {
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
