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

package org.jnode.driver.textscreen.fb;

import org.jnode.driver.textscreen.ScrollableTextScreen;
import org.jnode.driver.textscreen.TextScreen;

class FbTextScreen implements TextScreen {

    private final int width;
    private final int height;
    private final char[] data;
    private final FbTextScreen parent;

    public FbTextScreen(int width, int height, FbTextScreen parent) {
        this.width = width;
        this.height = height;
        this.data = new char[width * height];
        this.parent = parent;
    }

    /**
     * @see org.jnode.driver.textscreen.TextScreen#copyContent(int, int, int)
     */
    public final void copyContent(int srcOffset, int destOffset, int length) {
        System.arraycopy(data, srcOffset, data, destOffset, length);
    }

    /**
     * @see org.jnode.driver.textscreen.TextScreen#copyTo(org.jnode.driver.textscreen.TextScreen,
     *      int, int)
     */
    public void copyTo(TextScreen dst, int offset, int length) {
        // TODO Auto-generated method stub

    }

    /**
     * @see org.jnode.driver.textscreen.TextScreen#createCompatibleBufferScreen()
     */
    public TextScreen createCompatibleBufferScreen() {
        return new FbTextScreen(getWidth(), getHeight(), this);
    }

    /**
     * @see org.jnode.driver.textscreen.TextScreen#createCompatibleScrollableBufferScreen(int)
     */
    public ScrollableTextScreen createCompatibleScrollableBufferScreen(int height) {
        return new FbScrollableTextScreen(getWidth(), height, this);
    }

    /**
     * @see org.jnode.driver.textscreen.TextScreen#ensureVisible(int, boolean)
     */
    public void ensureVisible(int row, boolean sync) {
        // TODO Auto-generated method stub
    }

    /**
     * @see org.jnode.driver.textscreen.TextScreen#getChar(int)
     */
    public char getChar(int offset) {
        return data[offset];
    }

    /**
     * @see org.jnode.driver.textscreen.TextScreen#getColor(int)
     */
    public int getColor(int offset) {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * @see org.jnode.driver.textscreen.TextScreen#getHeight()
     */
    public final int getHeight() {
        return height;
    }

    /**
     * @see org.jnode.driver.textscreen.TextScreen#getOffset(int, int)
     */
    public final int getOffset(int x, int y) {
        return (y * width) + x;
    }

    /**
     * @see org.jnode.driver.textscreen.TextScreen#getWidth()
     */
    public final int getWidth() {
        return width;
    }

    /**
     * @see org.jnode.driver.textscreen.TextScreen#set(int, char, int, int)
     */
    public void set(int offset, char ch, int count, int color) {
        count = Math.min(count, data.length - offset);
        for (int i = count - 1; i >= 0; i--) {
            data[offset + i] = ch;
        }
        // TODO Set color
    }

    /**
     * @see org.jnode.driver.textscreen.TextScreen#set(int, char[], int, int,
     *      int)
     */
    public void set(int offset, char[] ch, int chOfs, int length, int color) {
        length = Math.min(length, data.length - offset);
        for (int i = length - 1; i >= 0; i--) {
            data[offset + i] = ch[chOfs + i];
        }
        // TODO Set color
    }

    /**
     * @see org.jnode.driver.textscreen.TextScreen#set(int, char[], int, int,
     *      int[], int)
     */
    public void set(int offset, char[] ch, int chOfs, int length, int[] colors, int colorsOfs) {
        length = Math.min(length, data.length - offset);
        for (int i = length - 1; i >= 0; i--) {
            data[offset + i] = ch[chOfs + i];
        }
        // TODO Set color
    }

    /**
     * @see org.jnode.driver.textscreen.TextScreen#setCursor(int, int)
     */
    public int setCursor(int x, int y) {
        // TODO Auto-generated method stub
        return 0; // TODO find proper offset
    }

    /**
     * @see org.jnode.driver.textscreen.TextScreen#setCursorVisible(boolean)
     */
    public int setCursorVisible(boolean visible) {
        // TODO Auto-generated method stub
        return 0; // TODO find proper offset
    }

    /**
     * @see org.jnode.driver.textscreen.TextScreen#sync(int, int)
     */
    public void sync(int offset, int length) {
        // TODO Auto-generated method stub

    }
}
