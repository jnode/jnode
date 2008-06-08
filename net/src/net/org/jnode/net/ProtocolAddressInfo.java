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
 
package org.jnode.net;

import java.net.InetAddress;
import java.util.Set;

/**
 * Each network device can be configured to hold address information for a
 * specific protocol. Objects that hold this information, must implement this
 * interface.
 * 
 * @author epr
 */
public interface ProtocolAddressInfo {

    /**
     * Is the given address one of the addresses of this object?
     * @param address
     */
    public boolean contains(ProtocolAddress address);

    /**
     * Is the given address one of the addresses of this object?
     * @param address
     */
    public boolean contains(InetAddress address);

    /**
     * Gets the default protocol address
     */
    public ProtocolAddress getDefaultAddress();

    /**
     * Gets a collection of all protocol addresses of this interface.
     * @return A Set of ProtocolAddress instances
     */
    public Set<ProtocolAddress> addresses();
}
