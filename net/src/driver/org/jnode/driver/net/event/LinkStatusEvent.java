/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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
 
package org.jnode.driver.net.event;

import org.jnode.driver.Device;
import org.jnode.driver.net.NetDeviceEvent;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class LinkStatusEvent extends NetDeviceEvent {

    /**
     * The link status has changed
     */
    public static final int LINK_STATUS_CHANGED = 100;

    /**
     * The connection status of the link
     */
    private final boolean connected;

    /**
     * @param source
     * @param connected
     */
    public LinkStatusEvent(Device source, boolean connected) {
        super(source, LINK_STATUS_CHANGED);
        this.connected = connected;
    }

    /**
     * @return Returns the connected.
     */
    public final boolean isConnected() {
        return connected;
    }
}
