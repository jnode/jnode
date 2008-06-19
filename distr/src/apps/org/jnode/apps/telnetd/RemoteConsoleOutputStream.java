/*
 * $Id: ConsoleInputStream.java 2224 2006-01-01 12:49:03Z epr $
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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


/**
 * 
 * @author Fabien DUMINY (fduminy at jnode.org)
 * 
 */
public class RemoteConsoleOutputStream {
    /*
     * public class RemoteConsoleOutputStream extends OutputStream {
     * 
     * private final TerminalIO terminalIO;
     * 
     * public RemoteConsoleOutputStream(TerminalIO terminalIO) { this.terminalIO =
     * terminalIO; }
     * 
     * @Override public void write(byte[] b, int off, int len) throws
     * IOException { if (b == null) { throw new NullPointerException("byte array
     * is null"); } else if ((off < 0) || (off > b.length) || (len < 0) || ((off +
     * len) > b.length) || ((off + len) < 0)) { throw new
     * IndexOutOfBoundsException("bad offset or length"); } else if (len == 0) {
     * return; }
     * 
     * TelnetUtils.write(terminalIO, b, off, len); }
     * 
     * @Override public void write(byte[] b) throws IOException { if (b == null) {
     * throw new NullPointerException("byte array is null"); }
     * 
     * TelnetUtils.write(terminalIO, b); }
     * 
     * @Override public void write(int b) throws IOException {
     * terminalIO.write((byte) b); }
     * 
     * @Override public void flush() throws IOException { terminalIO.flush(); }
     * 
     * @Override public void close() throws IOException { //terminalIO.close(); }
     */
}
