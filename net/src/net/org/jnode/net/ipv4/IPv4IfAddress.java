/*
 * Created on 4 Ìבת 2004
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
