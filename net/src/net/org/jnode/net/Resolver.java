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
 
package org.jnode.net;

import java.net.UnknownHostException;

/**
 * @author epr
 */
public interface Resolver {

    /**
     * Gets the address(es) of the given hostname.
     * 
     * @param hostname
     * @return All addresses of the given hostname. The returned array is at
     *         least 1 address long.
     * @throws UnknownHostException
     */
    public ProtocolAddress[] getByName(String hostname) throws UnknownHostException;

    /**
     * Gets the hostname of the given address.
     * 
     * @param address
     * @return All hostnames of the given hostname. The returned array is at
     *         least 1 hostname long.
     * @throws UnknownHostException
     */
    public String[] getByAddress(ProtocolAddress address) throws UnknownHostException;

}
