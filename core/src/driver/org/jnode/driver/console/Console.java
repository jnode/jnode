/**
 * $Id$
 */
package org.jnode.driver.console;

import java.io.InputStream;
import java.io.PrintStream;

import org.jnode.driver.input.KeyboardListener;
import org.jnode.system.event.FocusListener;

/**
 * @author epr
 */
public interface Console extends FocusListener {

	/**
	 * Set a character at a given location.
	 * 
	 * @param x
	 * @param y
	 * @param ch
	 * @param bgColor
	 */
	public void setChar(int x, int y, char ch, int bgColor);

	/**
	 * Get the character at a given location.
	 * 
	 * @param x
	 * @param y
	 */
	public char getChar(int x, int y);
	
	/**
	 * Get the bg color at a given location.
	 * 
	 * @param x
	 * @param y
	 */
	public char getBgColor(int x, int y);

	/**
	 * Put a character on the console, using the console as stream
	 * 
	 * @param ch
	 *            The character to put
	 * @param bgColor
	 *            The background color to use
	 */
	public void putChar(char ch, int bgColor);

	/**
	 * Sets the cursor at the given location.
	 * 
	 * @param x
	 * @param y
	 */
	public void setCursor(int x, int y);

	/**
	 * Gets the X position of the cursor
	 * 
	 * @return The position
	 */
	public int getCursorX();

	/**
	 * Gets the Y position of the cursor
	 * 
	 * @return The Y position
	 */
	public int getCursorY();

	/**
	 * Clears the specified line. This method not only clears the current line, but sets the current cursor to the beginning of the line just cleared.
	 * 
	 * @param y
	 *            The line number to clear.
	 */
	public void clearLine(int y);

	/**
	 * Gets the width of the console.
	 * 
	 * @return int
	 */
	public int getWidth();

	/**
	 * Gets the height of the console.
	 * 
	 * @return int
	 */
	public int getHeight();

	/**
	 * Add a keyboard listener
	 * 
	 * @param l
	 */
	public void addKeyboardListener(KeyboardListener l);

	/**
	 * Remove a keyboard listener
	 * 
	 * @param l
	 */
	public void removeKeyboardListener(KeyboardListener l);
	
	/**
	 * returns true if the console is userConsole
	 */
	public boolean isShellConsole(); 

	/**
	 * Close this console
	 */
	public void close();

	/**
	 * Get the errorstream to this console.
	 * 
	 * @return PrintStream
	 */
	public PrintStream getErr();

	/**
	 * Get the inputstream of this console.
	 * 
	 * @return InputStream
	 */
	public InputStream getIn();

	/**
	 * Get the outputstream to this console.
	 * 
	 * @return PrintStream
	 */
	public PrintStream getOut();
	
	public String getConsoleName();
	
	/**
	 * returns the registered name for this console
	 * 
	 * @return registered name for this console
	 */
	public void setAcceleratorKeyCode(int keyCode); 
	public int getAcceleratorKeyCode(); 
}
