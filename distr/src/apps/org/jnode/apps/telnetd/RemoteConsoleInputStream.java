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

import java.io.IOException;
import java.io.InputStream;

import org.jnode.driver.console.textscreen.ScrollableTextScreenConsole;

import net.wimpi.telnetd.io.BasicTerminalIO;

/**
 *
 * @author Fabien DUMINY (fduminy at jnode.org)
 *
 */
public class RemoteConsoleInputStream extends InputStream {

	private static final int CR = 0x0D;
	private static final int LF = BasicTerminalIO.ENTER;

	private final BasicTerminalIO terminalIO;
	private boolean echo = true;
	private final ScrollableTextScreenConsole scrollable;
	private boolean foundLF = false;

	public RemoteConsoleInputStream(BasicTerminalIO terminalIO, ScrollableTextScreenConsole scrollable) {
		this.terminalIO = terminalIO;
		this.scrollable = scrollable;
	}

	/**
	 * @see java.io.InputStream#read()
	 * @return int
	 * @throws IOException
	 */
	@Override
	public int read() throws IOException {
		while (true) {
			// In the private method TelnetIO.stripCRSeq (in telnetd library)
			// is transforming a CR followed by NULL or LF in
			// an ENTER represented by LF internally
			//
			// In order to satisfy the current JNode shell, we will transform
			// that to a sequence of CR+LF. TODO should we fix that in the JNode shell ?
			int ch;
//			// begin of workaround
//			if(foundLF)
//			{
//				ch = LF;
//				foundLF = false;
//			}
//			else
//			{
				ch = terminalIO.read();
//				if(ch == LF)
//				{
//					ch = CR;
//					foundLF = true;
//				}
//			}
//			// end of workaround

//			System.err.println("ch="+ch);
			if (ch != 0) {
//				if(ch == BasicTerminalIO.UP)
//				{
//					scrollable.scrollUp(1);
//				}
//				else if(ch == BasicTerminalIO.DOWN)
//				{
//					scrollable.scrollDown(1);
//				}
//				else
//				{
					if (echo) {
						terminalIO.write((char) ch);
					}
//				}

				if(ch == BasicTerminalIO.ENTER)
				{
					//TODO it seems we need that to be able to launch JNode commands
					ch = -1;
				}

				return ch;
			}
		}
	}
}
