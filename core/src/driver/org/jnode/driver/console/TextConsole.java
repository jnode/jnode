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
 
package org.jnode.driver.console;

import java.io.Reader;
import java.io.Writer;

import org.jnode.driver.console.textscreen.ConsoleKeyEventBindings;



/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author Levente S\u00e1ntha (lsantha@users.sourceforge.net)
 */
public interface TextConsole extends Console {

    /**
     * Sets the cursor to the given location.
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
     * Set the character at a given location.
     *
     * @param x
     * @param y
     * @param ch
     * @param color
     */
    public void setChar(int x, int y, char ch, int color);

    /**
     * Set characters starting at the given location.
     *
     * @param x
     * @param y
     * @param cbuf
     * @param color
     */
    public void setChar(int x, int y, char[] cbuf, int color);

    /**
     * Set characters starting at the given location.
     *
     * @param x
     * @param y
     * @param cbuf
     * @param offset
     * @param length
     * @param color
     */
    public void setChar(int x, int y, char[] cbuf, int offset, int length, int color);

    /**
     * Get the character at a given location.
     *
     * @param x
     * @param y
     */
    public char getChar(int x, int y);

    /**
     * Get the color of at a given location.
     *
     * @param x
     * @param y
     */
    public int getColor(int x, int y);

    /**
     * Gets the virtual width of the console's screen.
     *
     * @return int
     */
    public int getWidth();

    /**
     * Gets the virtual height of the console's screen.
     *
     * @return int
     */
    public int getHeight();

    /**
     * Gets the physical width of the console's screen.
     *
     * @return int
     */
    public int getDeviceWidth();

    /**
     * Gets the physical height of the console's screen.
     *
     * @return int
     */
    public int getDeviceHeight();

    /**
     * Clear the console.
     */
    public void clear();

    /**
     * Clear a given row.
     *
     * @param row
     */
    public void clearRow(int row);

    /**
     * Append a character to the current line.
     *
     * @param v
     * @param color
     */
    public void putChar(char v, int color);

    /**
     * Append characters to the current line.
     *
     * @param v
     * @param offset
     * @param length
     * @param color
     */
    public void putChar(char v[], int offset, int length, int color);

    /**
     * @return Returns the tabSize.
     */
    public int getTabSize();

    /**
     * @param tabSize The tabSize to set.
     */
    public void setTabSize(int tabSize);

    /**
     * Gets the Reader that delivers input characters from this console.  These
     * characters are the result of processing keyboard events, performing line
     * editing and completion.
     *
     * @return
     */
    public Reader getIn();

    /**
     * Gets the Writer that receives 'error' output for this console.
     *
     * @return
     */
    public Writer getErr();

    /**
     * Gets the Writer that receives 'normal' output for this console.
     *
     * @return
     */
    public Writer getOut();

    /**
     * Is the cursor visible.
     */
    public boolean isCursorVisible();

    /**
     * Make the cursor visible or not visible.
     *
     * @param visible
     */
    public void setCursorVisible(boolean visible);


    /**
     * Get the console's input completer
     *
     * @return The completer or <code>null</code>.
     */
    public InputCompleter getCompleter();

    /**
     * Set the console's input completer
     *
     * @param The new completer or <code>null</code>.
     */
    public void setCompleter(InputCompleter completer);
    
    /**
     * Get a snapshot of the console's key event bindings.
     * 
     * @return a copy of the current bindings.
     */
    public ConsoleKeyEventBindings getKeyEventBindings();
    
    /**
     * Set the console's key event bindings.
     * 
     * @param bindings the new bindings.
     */
    public void setKeyEventBindings(ConsoleKeyEventBindings bindings);
    
}
