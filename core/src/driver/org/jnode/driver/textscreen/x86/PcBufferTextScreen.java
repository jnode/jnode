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
 
package org.jnode.driver.textscreen.x86;

import org.jnode.driver.textscreen.TextScreen;
import org.jnode.vm.Unsafe;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class PcBufferTextScreen extends AbstractPcTextScreen implements TextScreen {

    /** My parent */
    private final AbstractPcTextScreen parent;

    /** Actual content buffer */
    private final char[] buffer;

    /** Temporary buffer that includes the cursor.  This goes to the video device.
     * Slower, but more likely to be correct than a temporary pointer to the last character
     * the cursor was over, as in previous versions.*/
    private final char[]screenBuffer;
    private int cursorIndex=0;
    private boolean cursorVisible=true;

    /**
     * Initialize this instance.
     * @param width
     * @param height
     */
    public PcBufferTextScreen(int width, int height, AbstractPcTextScreen parent) {
        super(width, height);
        this.buffer = new char[width * height];
        this.screenBuffer=new char[buffer.length];
        this.parent = parent;
    }

    /**
     * @see org.jnode.driver.textscreen.TextScreen#copyContent(int, int, int)
     */
    public void copyContent(int srcOffset, int destOffset, int length) {
        System.arraycopy(buffer, srcOffset, buffer, destOffset, length);
    }

    /**
     * @see org.jnode.driver.textscreen.TextScreen#getChar(int)
     */
    public char getChar(int offset) {
        return (char)(buffer[offset] & 0xFF);
    }

    /**
     * @see org.jnode.driver.textscreen.TextScreen#getColor(int)
     */
    public int getColor(int offset) {
        return (char)((buffer[offset] >> 8) & 0xFF);
    }

    /**
     * @see org.jnode.driver.textscreen.TextScreen#set(int, char, int, int)
     */
    public void set(int offset, char ch, int count, int color) {
        final char v = (char)((ch & 0xFF) | ((color & 0xFF) << 8));
        count = Math.min(count, buffer.length - offset);
        for (int i = 0; i < count; i++) {
            buffer[offset+i] = v;
        }
    }

    /**
     * @see org.jnode.driver.textscreen.TextScreen#set(int, char[], int, int, int)
     */
    public void set(int offset, char[] ch, int chOfs, int length, int color) {
        color = (color & 0xFF) << 8;
        length = Math.min(length, buffer.length - offset);
        for (int i = 0; i < length; i++) {
            final char v = (char)((ch[chOfs+i] & 0xFF) | color);
            buffer[offset+i] = v;
        }
    }

    /**
     * @see org.jnode.driver.textscreen.TextScreen#set(int, char[], int, int, int[], int)
     */
    public void set(int offset, char[] ch, int chOfs, int length, int[] colors,
            int colorsOfs) {
        length = Math.min(length, buffer.length - offset);
        for (int i = 0; i < length; i++) {
            final char v = (char)((ch[chOfs+i] & 0xFF) | (colors[colorsOfs+i] & 0xFF) << 8);
            buffer[offset+i] = v;
        }
    }

    /**
     * Copies the entire screen to the given destination.
     * For this operation to succeed, the screens involved must be
     * compatible.
     * @param dst
     */
    public void copyTo( TextScreen dst ) {
        if( dst instanceof AbstractPcTextScreen ) {
            char[] toScreen = buffer;
            if( cursorVisible&&cursorIndex<buffer.length&&cursorIndex>=0 ) {
                System.arraycopy( buffer, 0, screenBuffer, 0, buffer.length );
                char origValue = buffer[cursorIndex];
                //origValue |= 0x7000;//from december 2003 jnode code.

                //exchange the background with the foreground
                int color = (origValue >>8) & 0xFF;
                color = ((color >> 4) & 0xF) | ((color << 4) & 0xF0);
                origValue &= 0x00FF;
                origValue |= (color << 8) & 0xFF00;
                
                screenBuffer[cursorIndex] = origValue;
                toScreen = screenBuffer;
            }
            ( (AbstractPcTextScreen)dst ).copyFrom( toScreen, getTopOffset() );
        }
        else {
            throw new IllegalArgumentException( "Unknown destination type " + dst.getClass().getName() );
        }
    }

    /**
     * Return the offset in the buffer of the first visible row.
     * @return
     */
    protected int getTopOffset() {
        return 0;
    }

    /**
     * Copy the content of the given rawData into this screen.
     * @param rawData
     * @param rawDataOffset
     */
    public final void copyFrom(char[] rawData, int rawDataOffset) {
        if (rawDataOffset < 0) {
            Unsafe.die("Buffer:rawDataOffset = " + rawDataOffset);
        }
        System.arraycopy(rawData, rawDataOffset, buffer, getTopOffset(), getWidth() * getHeight());
    }

    /**
     * Synchronize the state with the actual device.
     */
    public void sync() {
        copyTo(parent);
    }

    public void setCursor( int x, int y ) {
        this.cursorIndex=getOffset( x,y);
    }

    public void setCursorVisible( boolean visible ) {
        this.cursorVisible=visible;
    }

    /**
     * @return Returns the parent.
     */
    protected final AbstractPcTextScreen getParent() {
        return this.parent;
    }
}
