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
 
package org.jnode.net.ipv4;

/**
 * @author JPG
 */
public class IPv4IfAddress {
	private IPv4Address net;
	private IPv4Address address;
	private IPv4Address mask;
	private IPv4Address broadcast;
	
	public IPv4IfAddress(IPv4Address address, IPv4Address mask){
		byte[] ip = address.getBytes();
		byte[] subnet_mask = mask.getBytes();
		
		byte[] bcast = new byte[4];
		byte[] net = new byte[4];
		
		for (int i=0; i<=3; i++){
			int a = ip[i] & subnet_mask[i];
			int b = ip[i] ^ subnet_mask[i];
			int c = ~b;
			net[i] = (byte)a;
			bcast[i] = (byte)c;
		}
		
		this.net = new IPv4Address(net, 0);
		this.address = address;
		this.mask = mask;
		this.broadcast = new IPv4Address(bcast, 0);
	}
	
	/**
	 * Tests if the given IP mathces this interface
	 */
	public boolean matches(IPv4Address other){
		if (address.equals(other) || broadcast.equals(other))
			return true;
		else
			return false;
	}
	
	/**
	 * Returns the network address of this subnet
	 */
	public IPv4Address getNetworkAddress(){
		return this.net;
	}
	
	/**
	 * Returns the IP address of this interface
	 */
	public IPv4Address getAddress(){
		return this.address;
	}
	
	/**
	 * Returns the subnet mask
	 */
	public IPv4Address getSubnetMask(){
		return this.mask;
	}
	
	/**
	 * Returns the broadcast address of this subnet
	 */
	public IPv4Address getBroadcast(){
		return this.broadcast;
	}

  public String toString()
  {
    return address.toString();
  }
}
