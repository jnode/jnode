/*
 * $Id$
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
