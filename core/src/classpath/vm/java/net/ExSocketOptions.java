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
 
package java.net;


/**
 * A JNode specific extension on SocketOptions
 * 
 * @see java.net.SocketOptions
 * @author epr
 */
public interface ExSocketOptions extends SocketOptions {
	
	/**
	 * Sets the network interface to use in transmission.
	 * Values must be of the type NetworkInterface.
	 * 
	 * @see java.net.NetworkInterface
	 */
	public static final int SO_TRANSMIT_IF = 0xFFFF0001;

}
