/*
 * Created on 2 Ìבס 2004
 */
package org.jnode.net.ipv4.icmp;

import org.jnode.net.SocketBuffer;

/**
 * @author JPG
 */
public interface ICMPListener {
	public void packetReceived(SocketBuffer skbuf);
}
