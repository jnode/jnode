/*
 * Created on 3 Ìבס 2004
 */
package org.jnode.net.ipv4.icmp;

import org.jnode.net.ipv4.IPv4Address;

/**
 * @author JPG
 */
public class ICMPListenerCriteria implements ICMPConstants{
	IPv4Address src;
	int icmp_type;
	
	public static final int ANY_TYPE = -1;
	
	public ICMPListenerCriteria(IPv4Address src){
		this.src = src;
		this.icmp_type = ANY_TYPE;
	}
	public ICMPListenerCriteria(IPv4Address src, int icmp_type){
		this.src = src;
		this.icmp_type = icmp_type;
	}
	public ICMPListenerCriteria(int icmp_type){
		this.icmp_type = icmp_type;
	}
	
	public boolean equals(Object obj) {
		ICMPListenerCriteria c = (ICMPListenerCriteria)obj;
		
		boolean result = true;
		
		if (src != null)
		result = result & src.equals(c.getSrc());
		if (icmp_type != ANY_TYPE)
		result = result & icmp_type == c.getIcmpType(); 
		
		return result;
	}
	
	public IPv4Address getSrc() {
		return src;
	}
	public int getIcmpType() {
		return icmp_type;
	}
}