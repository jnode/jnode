/*
 * $Id$
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
