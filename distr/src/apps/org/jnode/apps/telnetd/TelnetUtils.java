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
 
package org.jnode.apps.telnetd;

import java.io.IOException;

import net.wimpi.telnetd.io.TerminalIO;

/**
 * Utility methods for the telnet daemon.
 * 
 * @author Fabien DUMINY (fduminy@jnode.org)
 *
 */
public class TelnetUtils {

    /**
     * Write a buffer to the given terminal.
     * 
     * @param terminalIO the terminal to which buffer data should be sent.
     * @param b the buffer
     * @param off offset of the first byte to get from the buffer
     * @param len number of bytes to get from the buffer
     * @throws IOException
     */
    public static void write(TerminalIO terminalIO, byte[] b, int off, int len) throws IOException {
        int offset = off;
        for (int i = 0; i < len; i++) {
            terminalIO.getTelnetIO().write(b[offset++]);
        }
        if (terminalIO.isAutoflushing()) {
            terminalIO.flush();
        }
    }

    /**
     * Write a buffer to the given terminal.
     * 
     * @param terminalIO the terminal to which buffer data should be sent.
     * @param b the buffer
     * @throws IOException
     */
    public static void write(TerminalIO terminalIO, byte[] b) throws IOException {
        terminalIO.getTelnetIO().write(b);
        if (terminalIO.isAutoflushing()) {
            terminalIO.flush();
        }
    }

    private TelnetUtils() {
    }
}
