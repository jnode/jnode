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
 
package org.jnode.net;

import java.net.UnknownHostException;

/**
 * @author epr
 */
public interface Resolver {
	
	/**
	 * Gets the address(es) of the given hostname.
	 * @param hostname
	 * @return All addresses of the given hostname. The returned array is at least 1 address long.
	 * @throws UnknownHostException
	 */
	public ProtocolAddress[] getByName(String hostname)
	throws UnknownHostException;

	/**
	 * Gets the hostname of the given address.
	 * @param address
	 * @return All hostnames of the given hostname. The returned array is at least 1 hostname long.
	 * @throws UnknownHostException
	 */
	public String[] getByAddress(ProtocolAddress address)
	throws UnknownHostException;	
	

}
