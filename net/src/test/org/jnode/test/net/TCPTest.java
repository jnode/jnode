/*
 * $Id$
 */
package org.jnode.test.net;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * A very trivial HTTP server, which actually only receives the HTTP 
 * request and then closes the connection.
 *  
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class TCPTest {

	public static void main(String[] args) 
	throws Exception {
		// Listen to http messages
		final ServerSocket socket = new ServerSocket(80);
		try {
			for (int i = 0; i < 5; i++) {
				Socket s = socket.accept();
				System.out.println("Received call on port 80 from " + s.getRemoteSocketAddress());
				
				final InputStream is = s.getInputStream();
				final BufferedReader in = new BufferedReader(new InputStreamReader(is));
				
				String line;
				while ((line = in.readLine()) != null) {
					System.out.println(line);
					if (line.length() == 0) {
						break;
					}
				}
				System.out.println("Got EOF or blank line");
				
				s.close();
			}
		
			System.out.println("I'm stopping now");
		} finally {
			socket.close();
		}
	}
}
