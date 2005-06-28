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

import java.awt.AWTEvent;
import java.awt.Component;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface ISwingPeer<T extends Component> {

	public T getAWTComponent();
    
    /**
     * Process an event within this swingpeer
     * @param event
     */
    public void processAWTEvent(AWTEvent event);
    
    /**
     * Validate only this peer, do not validate the AWT component.
     */
    public void validatePeerOnly();
}
