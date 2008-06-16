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

final class FbScrollableTextScreen extends FbTextScreen implements ScrollableTextScreen {

    /** Offset of top visible row */
    private int ofsY;
    
    /** Height of the parent screen */
    private final int parentHeight;
    
    /** Maximum row that has valid data */
    private int maxValidY;

    /**
     * @param width
     * @param height
     */
    public FbScrollableTextScreen(int width, int height, FbTextScreen parent) {
        super(width, height, parent);
        this.parentHeight = parent.getHeight();
    }

    /**
     * @see org.jnode.driver.textscreen.ScrollableTextScreen#ensureVisible(int)
     */
    public void ensureVisible(int row) {
        if (row < ofsY) {
            ofsY = row;
        } else if (row >= ofsY + parentHeight) {
            ofsY = (row - parentHeight) + 1;
        }
    }

    /**
     * @see org.jnode.driver.textscreen.ScrollableTextScreen#scrollDown(int)
     */
    public void scrollDown(int rows) {
        if (rows < 0) {
            throw new IllegalArgumentException("rows < 0");
        }
        final int height = Math.min(maxValidY + 1, getHeight());
        if (ofsY + parentHeight < height) {
            ofsY = ofsY + Math.min(rows, height - (ofsY + parentHeight));
        }
    }

    /**
     * @see org.jnode.driver.textscreen.ScrollableTextScreen#scrollUp(int)
     */
    public void scrollUp(int rows) {
        if (rows < 0) {
            throw new IllegalArgumentException("rows < 0");
        }
        if (ofsY > 0) {
            ofsY = ofsY - Math.min(ofsY, rows);
        }
    }

    /**
     * Return the offset in the buffer of the first visible row.
     * 
     * @return
     */
    protected int getTopOffset() {
        return ofsY * getWidth();
    }  
    
    /**
     * @see org.jnode.driver.textscreen.TextScreen#set(int, char, int, int)
     */
    public void set(int offset, char ch, int count, int color) {
        maxValidY = Math.max(maxValidY, offset / getWidth());
        super.set(offset, ch, count, color);
    }
    /**
     * @see org.jnode.driver.textscreen.TextScreen#set(int, char[], int, int, int)
     */
    public void set(int offset, char[] ch, int chOfs, int length, int color) {
        maxValidY = Math.max(maxValidY, (offset + length - 1) / getWidth());
        super.set(offset, ch, chOfs, length, color);
    }
    /**
     * @see org.jnode.driver.textscreen.TextScreen#set(int, char[], int, int, int[], int)
     */
    public void set(int offset, char[] ch, int chOfs, int length, int[] colors,
            int colorsOfs) {
        maxValidY = Math.max(maxValidY, (offset + length - 1) / getWidth());
        super.set(offset, ch, chOfs, length, colors, colorsOfs);
    }
}
