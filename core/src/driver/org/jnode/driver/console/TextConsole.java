/*
 * $Id$
 */
package org.jnode.driver.console;

import java.io.PrintStream;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface TextConsole extends Console {

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
     * @param color
     */
    public void setChar(int x, int y, char ch, int color);

    /**
     * Get the character at a given location.
     * 
     * @param x
     * @param y
     */
    public char getChar(int x, int y);

    /**
     * Get the color at a given location.
     * 
     * @param x
     * @param y
     */
    public int getColor(int x, int y);

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
     * Clear the console.
     */
	public void clear();
	
	/**
	 * Clear a given row.
	 * @param row
	 */
	public void clearRow(int row);

	/** 
	 * Append a character to the current line.
	 * @param v
	 * @param color
	 */
	public void putChar(char v, int color);

	/**
	 * @return Returns the tabSize.
	 */
	public int getTabSize();
	
	/**
	 * @param tabSize The tabSize to set.
	 */
	public void setTabSize(int tabSize);
	
    /**
     * Ensure that the given row is visible.
     * @param row
     */
    public void ensureVisible(int row);
    
	/**
	 * Gets the error stream of this console.
	 * @return
	 */
	public PrintStream getErr();
	
	/**
	 * Gets the output stream of this console.
	 * @return
	 */
	public PrintStream getOut();
	
	/**
	 * Is the cursor visible.
	 */
	public boolean isCursorVisible();
	
	/**
	 * Make the cursor visible or not visible.
	 * @param visible
	 */
	public void setCursorVisible(boolean visible);
}
