/*
 * $Id$
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

    /**
     * Initialize this instance.
     * @param width
     * @param height
     */
    public PcBufferTextScreen(int width, int height, AbstractPcTextScreen parent) {
        super(width, height);
        this.buffer = new char[width * height];
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
        for (int i = 0; i < count; i++) {
            buffer[offset+i] = v;
        }
    }
    
    /**
     * @see org.jnode.driver.textscreen.TextScreen#set(int, char[], int, int, int)
     */
    public void set(int offset, char[] ch, int chOfs, int length, int color) {
        color = (color & 0xFF) << 8;
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
    public void copyTo(TextScreen dst) {
        if (dst instanceof AbstractPcTextScreen) {
            ((AbstractPcTextScreen)dst).copyFrom(buffer, getTopOffset());
        } else {
            throw new IllegalArgumentException("Unknown destionation type " + dst.getClass().getName());
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
    final void copyFrom(char[] rawData, int rawDataOffset) {
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
    
    /**
     * @return Returns the parent.
     */
    protected final AbstractPcTextScreen getParent() {
        return this.parent;
    }
}
