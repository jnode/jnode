/*
 * $Id$
 * 
 * Serial port test application
 * Oct 15 2003, mgeisse
 */
package org.jnode.test;

import org.apache.log4j.Logger;
import org.jnode.driver.DeviceUtils;
import org.jnode.driver.DeviceException;
import org.jnode.driver.character.ChannelAlreadyOwnedException;
import org.jnode.driver.serial.SerialPortAPI;

import java.io.IOException;
import java.nio.channels.ByteChannel;
import java.io.PrintWriter;
import java.io.OutputStreamWriter;
import org.jnode.util.ChannelOutputStream;
import java.io.Reader;
import java.io.InputStreamReader;
import org.jnode.util.ChannelInputStream;

/**
 * @author mgeisse
 * Serial port test application.
 */
public class SerialPortTest {
	
	/** My logger */
	private static final Logger log = Logger.getLogger(SerialPortTest.class);

	public static void main(String[] args) {
		try {
			final SerialPortAPI api = (SerialPortAPI)
				(DeviceUtils.getAPI ("serial0", SerialPortAPI.class));
			final ByteChannel channel = api.getChannel (null);

			System.out.println ("Writing a test string to the serial port.");
			PrintWriter w = new PrintWriter (new OutputStreamWriter (
			  new ChannelOutputStream (channel, 1000)));
			w.print ("Hello World!\r\n");
			w.print ("This is a second line of text!\r\n");
			w.print ("Just for fun, a third line!\r\n");
			w.flush ();
			
			System.out.println ("Now waiting for one incoming line of text.");
			Reader r = new InputStreamReader (new ChannelInputStream (channel));
			while (true) {
				int c = r.read ();
				if (c >= 32) System.out.write (c);
				if (c == '\n') break;
			}
			
			System.out.println ("\nserial port test finished.");
		
		} catch (IOException ex) {
			log.error("serial0 I/O exception: ", ex);
		} catch (ChannelAlreadyOwnedException ex) {
			log.error("serial0 channel already in use", ex);
		} catch (DeviceException ex) {
			log.error("serial0 device exception: ", ex);
		}
	}
}
