/*
 * $Id$
 */
package org.jnode.awt.swingpeers;

import java.awt.Component;
import java.awt.peer.ContainerPeer;

import javax.swing.JComponent;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface SwingContainerPeer extends ContainerPeer {

	/**
	 * Add the given peer to this container.
	 * @param awtComponent
	 * @param peer
	 */
	public void addAWTComponent(Component awtComponent, JComponent peer);
}
