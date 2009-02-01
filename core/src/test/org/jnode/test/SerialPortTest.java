/*
 * $Id$
 *
 * JNode.org
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
 
package org.jnode.test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.channels.ByteChannel;
import org.apache.log4j.Logger;
import org.jnode.driver.DeviceException;
import org.jnode.driver.DeviceUtils;
import org.jnode.driver.character.ChannelAlreadyOwnedException;
import org.jnode.driver.serial.SerialPortAPI;
import org.jnode.util.ChannelInputStream;
import org.jnode.util.ChannelOutputStream;

/**
 * @author mgeisse
 *         Serial port test application.
 */
public class SerialPortTest {

    /**
     * My logger
     */
    private static final Logger log = Logger.getLogger(SerialPortTest.class);

    public static void main(String[] args) {
        try {
            final SerialPortAPI api = (SerialPortAPI)
                (DeviceUtils.getAPI("serial0", SerialPortAPI.class));
            final ByteChannel channel = api.getChannel(null);

            System.out.println("Writing a test string to the serial port.");
            PrintWriter w = new PrintWriter(new OutputStreamWriter(
                new ChannelOutputStream(channel, 1000)));
            w.print("Hello World!\r\n");
            w.print("This is a second line of text!\r\n");
            w.print("Just for fun, a third line!\r\n");
            w.flush();

            System.out.println("Now waiting for one incoming line of text.");
            Reader r = new InputStreamReader(new ChannelInputStream(channel));
            while (true) {
                int c = r.read();
                if (c >= 32) System.out.write(c);
                if (c == '\n') break;
            }

            System.out.println("\nserial port test finished.");

        } catch (IOException ex) {
            log.error("serial0 I/O exception: ", ex);
        } catch (ChannelAlreadyOwnedException ex) {
            log.error("serial0 channel already in use", ex);
        } catch (DeviceException ex) {
            log.error("serial0 device exception: ", ex);
        }
    }
}
