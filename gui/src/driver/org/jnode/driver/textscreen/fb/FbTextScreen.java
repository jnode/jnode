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

import java.util.Arrays;

import org.jnode.driver.textscreen.TextScreen;
import org.jnode.driver.textscreen.x86.AbstractPcTextScreen;
import org.jnode.driver.video.Surface;


class FbTextScreen extends AbstractPcTextScreen {
    private static final int SCREEN_WIDTH = 80;
    private static final int SCREEN_HEIGHT = 25;

    private final char[] buffer;

    private int cursorOffset;
    private boolean cursorVisible = true;

    private final FbScreenPainter painter;

    public FbTextScreen(Surface g) {
        super(SCREEN_WIDTH, SCREEN_HEIGHT);
        buffer = new char[SCREEN_WIDTH * SCREEN_HEIGHT];
        painter = new FbScreenPainter(this, g);
        Arrays.fill(buffer, ' ');
    }

    public char getChar(int offset) {
        return buffer[offset];
    }

    public int getColor(int offset) {
        return 0;
    }

    public void set(int offset, char ch, int count, int color) {
        char c = (char) (ch & 0xFF);
        buffer[offset] = c == 0 ? ' ' : c;
        sync(offset, count);
    }

    public void set(int offset, char[] ch, int chOfs, int length, int color) {
        char[] cha = new char[ch.length];
        for (int i = 0; i < cha.length; i++) {
            char c = (char) (ch[i] & 0xFF);
            cha[i] = c == 0 ? ' ' : c;
        }
        System.arraycopy(cha, chOfs, buffer, offset, length);
        sync(offset, length);
    }

    public void set(int offset, char[] ch, int chOfs, int length, int[] colors, int colorsOfs) {
        char[] cha = new char[ch.length];
        for (int i = 0; i < cha.length; i++) {
            char c = (char) (ch[i] & 0xFF);
            cha[i] = c == 0 ? ' ' : c;
        }
        System.arraycopy(cha, chOfs, buffer, offset, length);
        sync(offset, length);
    }

    public void copyContent(int srcOffset, int destOffset, int length) {
        System.arraycopy(buffer, srcOffset * 2, buffer, destOffset * 2, length * 2);
        sync(destOffset, length);
    }

    public void copyTo(TextScreen dst, int offset, int length) {

    }

    public void sync(int offset, int length) {
        painter.repaint();
    }

    public int setCursor(int x, int y) {
        cursorOffset = getOffset(x, y);
        return cursorOffset;
    }

    public int setCursorVisible(boolean visible) {
        cursorVisible = visible;
        return cursorOffset;
    }

    /**
     * Copy the content of the given rawData into this screen.
     *
     * @param rawData the data as a char array
     * @param rawDataOffset the offset in the data array
     */
    @Override
    public void copyFrom(char[] rawData, int rawDataOffset) {
        if (rawDataOffset < 0) {
            // Unsafe.die("Screen:rawDataOffset = " + rawDataOffset);
        }
        char[] cha = new char[rawData.length];
        for (int i = 0; i < cha.length; i++) {
            char c = (char) (rawData[i] & 0xFF);
            cha[i] = c == 0 ? ' ' : c;
        }

        final int length = getWidth() * getHeight();
        System.arraycopy(cha, rawDataOffset, buffer, 0, length);
        sync(0, length);
    }

    char[] getBuffer() {
        return buffer;
    }

}
