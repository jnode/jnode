/*
 * $Id$
 *
 * Copyright (C) 2003-2010 JNode.org
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
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.awt.swingpeers;

import java.awt.Component;
import java.awt.peer.ContainerPeer;
import javax.swing.JComponent;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface ISwingContainerPeer extends ContainerPeer {

    /**
     * Add the given peer to this container.
     *
     * @param awtComponent
     * @param peer
     */
    public void addAWTComponent(Component awtComponent, JComponent peer);
}
