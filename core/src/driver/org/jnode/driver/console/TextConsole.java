/*
 * $Id$
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

package org.jnode.driver.console;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author Levente S\u00e1ntha (lsantha@users.sourceforge.net)
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
     * Set the characters at the given location.
     *
     * @param x
     * @param y
     * @param ch
     * @param color
     */
    public void setChar(int x, int y, char[] ch, int color);

    /**
     * Set the characters at the given location.
     *
     * @param x
     * @param y
     * @param ch
     * @param cOfset
     * @param cLength
     * @param color
     */
    public void setChar(int x, int y, char[] ch, int cOfset, int cLength, int color);

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
     * @param lenght
     * @param color
     */
    public void putChar(char v[], int offset, int lenght, int color);

    /**
     * @return Returns the tabSize.
     */
    public int getTabSize();

    /**
     * @param tabSize The tabSize to set.
     */
    public void setTabSize(int tabSize);

    /**
     * Gets the input stream of this console.
     *
     * @return
     */
    public InputStream getIn();

    /**
     * Gets the error stream of this console.
     *
     * @return
     */
    public OutputStream getErr();

    /**
     * Gets the output stream of this console.
     *
     * @return
     */
    public OutputStream getOut();

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
     * Ges the console's input completer
     *
     * @param The new completer or <code>null</code>.
     */
    public void setCompleter(InputCompleter completer);
}
