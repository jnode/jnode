/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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
 
package org.jnode.net.util;

import javax.naming.NameNotFoundException;

import org.jnode.driver.net.NetworkException;
import org.jnode.naming.InitialNaming;
import org.jnode.net.NetworkLayerManager;
import org.jnode.net.SocketBuffer;

/**
 * Utility class for network devices
 * 
 * @author epr
 */
public class NetUtils {

    /**
     * A packet has just been received, send it to the packet-type-manager.
     * 
     * @param skbuf
     */
    public static void sendToPTM(SocketBuffer skbuf) throws NetworkException {
        final NetworkLayerManager ptm = getNLM();
        ptm.receive(skbuf);
    }

    /**
     * Gets the packet-type-manager
     */
    public static NetworkLayerManager getNLM() throws NetworkException {
        try {
            return InitialNaming.lookup(NetworkLayerManager.NAME);
        } catch (NameNotFoundException ex) {
            throw new NetworkException("Cannot find NetworkLayerManager", ex);
        }
    }
}
