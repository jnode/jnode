/*
 * $Id: PcTextScreen.java 4266 2008-06-19 20:07:25Z fduminy $
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
 * That class is an AbstractPcTextScreen that display nothing to any screen.
 * It's useful to avoid rendering of consoles that aren't focused.  
 * 
 * @author Fabien DUMINY
 */
public class NoDisplayTextScreen extends AbstractPcTextScreen {
    /**
     * Initialize this instance.
     *
     * @param width
     * @param height
     */
    public NoDisplayTextScreen(int width, int height) {
        super(width, height);
    }

    /**
     * @see org.jnode.driver.textscreen.TextScreen#copyContent(int, int, int)
     */
    @Override
    public final void copyContent(int srcOffset, int destOffset, int length) {
        // do nothing
        Unsafe.debug("\nNoDisplayTextScreen.copyContent: srcOffset=" + srcOffset + " destOffset=" + 
                destOffset + " length=" + length);
    }

    /**
     * @see org.jnode.driver.textscreen.TextScreen#getChar(int)
     */
    @Override
    public final char getChar(int offset) {
        Unsafe.debug("\nNoDisplayTextScreen.getChar: offset=" + offset + " return 0");
        return 0;
    }

    /**
     * @see org.jnode.driver.textscreen.TextScreen#getColor(int)
     */
    @Override
    public final int getColor(int offset) {
        Unsafe.debug("\nNoDisplayTextScreen.getColor: offset=" + offset + " return 0");
        return 0;
    }

    /**
     * @see org.jnode.driver.textscreen.TextScreen#set(int, char, int, int)
     */
    @Override
    public final void set(int offset, char ch, int count, int color) {
        // do nothing
        Unsafe.debug("\nNoDisplayTextScreen.set: offset=" + offset + " ch=" + ch + " count=" +
                count + " color=" + color);
    }

    /**
     * @see org.jnode.driver.textscreen.TextScreen#set(int, char[], int, int,
     *      int)
     */
    @Override
    public final void set(int offset, char[] ch, int chOfs, int length, int color) {
        // do nothing
        Unsafe.debug("\nNoDisplayTextScreen.set: offset=" + offset + " chOfs=" +
                chOfs + " length=" + length + " color=" + color);
    }

    /**
     * @see org.jnode.driver.textscreen.TextScreen#set(int, char[], int, int,
     *      int[], int)
     */
    @Override
    public final void set(int offset, char[] ch, int chOfs, int length, int[] colors,
            int colorsOfs) {
        // do nothing
        Unsafe.debug("\nNoDisplayTextScreen.set: offset=" + offset + " chOfs=" +
                chOfs + " length=" + length + " colors=" + colors + " colorsOfs=" + colorsOfs);
    }

    /**
     * Copy the content of the given rawData into this screen.
     * 
     * @param rawData
     * @param rawDataOffset
     */
    @Override
    public final void copyFrom(char[] rawData, int rawDataOffset) {
        // do nothing
        //Unsafe.debug("\nNoDisplayTextScreen.copyFrom: rawDataOffset=" + rawDataOffset);
    }

    /**
     * Copies the entire screen to the given destination. For this operation to
     * succeed, the screens involved must be compatible.
     * 
     * @param dst
     */
    @Override
    public final void copyTo(TextScreen dst, int offset, int length) {
        // do nothing
        //Unsafe.debug("\nNoDisplayTextScreen.copyTo: dst=" + dst + " offset=" + offset + " length=" +
        //        length);
    }

    @Override
    public final int setCursor(int x, int y) {
        Unsafe.debug("\nNoDisplayTextScreen.setCursor: x=" + x + " y=" + y + " return 0");
        return 0;
    }

    @Override
    public final int setCursorVisible(boolean visible) {
        Unsafe.debug("\nNoDisplayTextScreen.setCursorVisible: visible=" + visible + " return 0");
        return 0;
    }
}
