/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.driver.console.spi;

import java.io.IOException;
import java.io.OutputStream;

import org.jnode.driver.console.TextConsole;

/**
 * @author epr
 */
public class ConsoleOutputStream extends OutputStream {

	private TextConsole console;
	private int fgColor;

	/**
	 * Create a new instance
	 * @param console
	 * @param fgColor
	 */
	public ConsoleOutputStream(TextConsole console, int fgColor) {
		this.console = console;
		this.fgColor = fgColor;
	}

	/**
	 * @param b
	 * @see java.io.OutputStream#write(int)
	 * @throws IOException
	 */
	public void write(int b) throws IOException {
		console.putChar((char)b, fgColor);
	}
	
	/**
	 * @return int
	 */
	public int getFgColor() {
		return fgColor;
	}

	/**
	 * Sets the fgColor.
	 * @param fgColor The fgColor to set
	 */
	public void setFgColor(int fgColor) {
		this.fgColor = fgColor;
	}

}
