/*
 * $Id$
 */
package org.jnode.net.arp;

/**
 * @author epr
 */
public interface ARPConstants {
	
	public static final int ARP_REQUEST = 1;
	public static final int ARP_REPLY = 2;
	public static final int RARP_REQUEST = 3;
	public static final int RARP_REPLY = 4;

	/** Delay between ARP requests */ 
	public static final int ARP_REQUEST_DELAY = 1500;
}
