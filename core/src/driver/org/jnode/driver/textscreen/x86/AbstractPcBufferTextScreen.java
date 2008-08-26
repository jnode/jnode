/*
 * $Id: PcBufferTextScreen.java 2764 2006-09-23 17:56:19Z lsantha $
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
 
package org.jnode.driver.textscreen.x86;

import org.jnode.driver.textscreen.TextScreen;
import org.jnode.vm.Unsafe;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author Fabien DUMINY (fduminy at jnode.org)
 */
public abstract class AbstractPcBufferTextScreen extends AbstractPcTextScreen {

    /**
     * Actual content buffer
     */
    private final char[] buffer;

    /**
     * Temporary buffer that includes the cursor.  This goes to the video device.
     * Slower, but more likely to be correct than a temporary pointer to the last character
     * the cursor was over, as in previous versions.
     */
    private final char[]screenBuffer;
    private int cursorIndex = 0;
    private boolean cursorVisible = true;

    /**
     * Initialize this instance.
     *
     * @param width
     * @param height
     */
    public AbstractPcBufferTextScreen(int width, int height) {
        super(width, height);
        this.buffer = new char[width * height];
        this.screenBuffer = new char[buffer.length];
    }

    /**
     * @see org.jnode.driver.textscreen.TextScreen#copyContent(int, int, int)
     */
    @Override
    public final void copyContent(int srcOffset, int destOffset, int length) {
        System.arraycopy(buffer, srcOffset, buffer, destOffset, length);
    }

    /**
     * @see org.jnode.driver.textscreen.TextScreen#getChar(int)
     */
    @Override
    public final char getChar(int offset) {
        return (char) (buffer[offset] & 0xFF);
    }

    /**
     * @see org.jnode.driver.textscreen.TextScreen#getColor(int)
     */
    @Override
    public final int getColor(int offset) {
        return (char) ((buffer[offset] >> 8) & 0xFF);
    }

    /**
     * @see org.jnode.driver.textscreen.TextScreen#set(int, char, int, int)
     */
    @Override
    public void set(int offset, char ch, int count, int color) {
        final char v = (char) ((ch & 0xFF) | ((color & 0xFF) << 8));
        count = Math.min(count, buffer.length - offset);
        //todo apparently count is negative sometimes, investigate it
        for (int i = 0; i < count; i++) {
            buffer[offset + i] = v;
        }
    }

    /**
     * @see org.jnode.driver.textscreen.TextScreen#set(int, char[], int, int,
     *      int)
     */
    @Override
    public void set(final int offset, final char[] ch, final int chOfs, int length, int color) {
        color = (color & 0xFF) << 8;
        length = Math.min(length, buffer.length - offset);
        
        int bufOffset = offset;
        int chOffset = chOfs;
        for (int i = 0; i < length; i++) {
            buffer[bufOffset++] = (char) ((ch[chOffset++] & 0xFF) | color);
        }
    }

    /**
     * @see org.jnode.driver.textscreen.TextScreen#set(int, char[], int, int,
     *      int[], int)
     */
    @Override
    public void set(final int offset, char[] ch, final int chOfs, int length, int[] colors, int colorsOfs) {
        length = Math.min(length, buffer.length - offset);
        
        int bufOffset = offset;
        int chOffset = chOfs;
        int colorsOffset = colorsOfs;
        for (int i = 0; i < length; i++) {
            buffer[bufOffset++] = (char) ((ch[chOffset++] & 0xFF) | (colors[colorsOffset++] & 0xFF) << 8);
        }
    }

    /**
     * Copies the entire screen to the given destination. For this operation to
     * succeed, the screens involved must be compatible.
     *
     * @param dst
     */
    @Override
    public final void copyTo(TextScreen dst, int offset, int length) {
        if (dst instanceof AbstractPcTextScreen) {
            char[] toScreen = buffer;
            if (cursorVisible && cursorIndex < buffer.length && cursorIndex >= 0) {
                System.arraycopy(buffer, 0, screenBuffer, 0, buffer.length);
                char origValue = buffer[cursorIndex];
                // origValue |= 0x7000;//from december 2003 jnode code.

                // exchange the background with the foreground
                int color = (origValue >> 8) & 0xFF;
                color = ((color >> 4) & 0xF) | ((color << 4) & 0xF0);
                origValue &= 0x00FF;
                origValue |= (color << 8) & 0xFF00;

                screenBuffer[cursorIndex] = origValue;
                toScreen = screenBuffer;
            }
            ((AbstractPcTextScreen) dst).copyFrom(toScreen, getTopOffset());
        } else {
            throw new IllegalArgumentException("Unknown destination type " +
                    dst.getClass().getName());
        }
    }

    /**
     * Return the offset in the buffer of the first visible row.
     *
     * @return the offset
     */
    protected int getTopOffset() {
        return 0;
    }

    /**
     * Copy the content of the given rawData into this screen.
     *
     * @param rawData
     * @param rawDataOffset
     */
    @Override
    public final void copyFrom(char[] rawData, final int rawDataOffset) {
        if (rawDataOffset < 0) {
            Unsafe.die("Buffer:rawDataOffset = " + rawDataOffset);
        }
        System.arraycopy(rawData, rawDataOffset, buffer, getTopOffset(), getWidth() * getHeight());
    }

    /**
     * Synchronize the state with the actual device.
     * @param offset
     * @param length
     */
    public abstract void sync(int offset, int length);

    @Override
    public final int setCursor(int x, int y) {
        this.cursorIndex = getOffset(x, y);
        setParentCursor(x, y);
        return cursorIndex;
    }

    protected abstract void setParentCursor(int x, int y);

    @Override
    public final int setCursorVisible(boolean visible) {
        this.cursorVisible = visible;
        return cursorIndex;
    }
}
