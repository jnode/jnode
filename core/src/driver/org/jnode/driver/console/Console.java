/**
 * $Id$
 */
package org.jnode.driver.console;

import org.jnode.driver.input.KeyboardListener;
import org.jnode.system.event.FocusListener;

/**
 * @author epr
 */
public interface Console extends FocusListener {

	public static char MOUSE_CURSOR_CHAR = 'X'; 
	
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
	 * Set a character at a given location.
	 * 
	 * @param x
	 * @param y
	 * @param ch
	 * @param bgColor
	 */
	public void setChar(int x, int y, char ch, int bgColor);

	/**
	 * Set a character at the cursor location.
	 * 
	 * @param ch
	 * @param bgColor
	 */
	public void setChar(char ch, int bgColor);
	
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
	 * Close this console
	 */
	public void close();

	public String getConsoleName();
	
	/**
	 * returns the registered name for this console
	 * 
	 * @return registered name for this console
	 */
	public void setAcceleratorKeyCode(int keyCode); 
	public int getAcceleratorKeyCode();

}
