/*
 * $Id$
 */
package org.jnode.awt.swingpeers;

import javax.swing.JComponent;
import java.awt.Component;
import java.awt.peer.ContainerPeer;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface ISwingContainerPeer extends ContainerPeer {

	/**
	 * Add the given peer to this container.
	 * @param awtComponent
	 * @param peer
	 */
	public void addAWTComponent(Component awtComponent, JComponent peer);
}
