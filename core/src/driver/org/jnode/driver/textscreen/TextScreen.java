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

package org.jnode.driver.textscreen;

/**
 * Abstract class that represents different kinds of screens : - physical screen
 * (video memory) - buffered screen (system memory, faster that video memory) -
 * remote screen, shared screen, multiple screens ... - recording screen (movies
 * for demos or tutorials)
 *
 * @author epr
 */
public interface TextScreen {

    /**
     * Gets the letter at the given offset.
     */
    public abstract char getChar(int offset);

    /**
     * Gets the color of the letter at the given offset.
     */
    public abstract int getColor(int offset);

    /**
     * Set a series of of the same character with a given color at a given
     * offset.
     *
     * @param offset
     * @param ch
     * @param color
     */
    public abstract void set(int offset, char ch, int count, int color);

    /**
     * Set an series of characters with a given color at a given offset.
     *
     * @param offset
     * @param ch
     * @param color
     */
    public abstract void set(int offset, char[] ch, int chOfs, int length,
            int color);

    /**
     * Set an series of characters with a given series of colors at a given
     * offset.
     *
     * @param offset
     * @param ch
     * @param colors
     * @param colorsOfs
     */
    public abstract void set(int offset, char[] ch, int chOfs, int length,
            int[] colors, int colorsOfs);

    /**
     * Copy the content of the screen from a given source to a given
     * destination offset.
     *
     * @param srcOffset
     * @param destOffset
     * @param length
     */
    public abstract void copyContent(int srcOffset, int destOffset, int length);

    /**
     * Copies the entire screen to the given destination.
     * For this operation to succeed, the screens involved must be
     * compatible.
     *
     * @param dst
     * @param offset
     * @param length
     */
    public abstract void copyTo(TextScreen dst, int offset, int length);

    /**
     * Gets the height of the screen in letters.
     *
     * @return Returns the height.
     */
    public int getHeight();

    /**
     * Gets the width of the screen in letters.
     *
     * @return Returns the width.
     */
    public int getWidth();

    /**
     * Calculate the offset for a given x,y coordinate.
     *
     * @param x
     * @param y
     * @return
     */
    public int getOffset(int x, int y);

    /**
     * Synchronize the state with the actual device.
     * @param offset
     * @param length
     */
    public void sync(int offset, int length);

    /**
     * Create an in-memory buffer text screen that is compatible
     * with this screen.
     *
     * @return
     */
    public TextScreen createCompatibleBufferScreen();

    /**
     * Create an in-memory buffer text screen that is compatible
     * with this, but larger and supports scrolling.
     *
     * @return
     */
    public ScrollableTextScreen createCompatibleScrollableBufferScreen(int height);

    /**
     * Ensure that the given row is visible.
     *
     * @param row
     * @param sync true if screen should synchronize
     */
    public void ensureVisible(int row, boolean sync);

    /**
     * 
     * @param x
     * @param y
     * @return offset of the cursor
     */
    public int setCursor(int x, int y);

    /**
     * 
     * @param visible
     * @return offset of the cursor
     */
    int setCursorVisible(boolean visible);
}
