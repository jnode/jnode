/*
 * $Id$
 *
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
 
package org.jnode.apps.telnetd;

import java.io.IOException;

import net.wimpi.telnetd.io.TelnetIO;
import net.wimpi.telnetd.io.TerminalIO;

import org.jnode.driver.textscreen.TextScreen;
import org.jnode.driver.textscreen.x86.AbstractPcTextScreen;

/**
 *
 * @author Fabien DUMINY (fduminy at jnode.org)
 *
 */
public class RemoteTextScreen extends AbstractPcTextScreen {
    private final TerminalIO terminalIO;
    private final char[] buffer;
    private int cursorOffset;

    /**
     * Construct a remote text screen for the given terminal.
     * 
     * @param terminalIO
     */
    public RemoteTextScreen(TerminalIO terminalIO) {
        super(terminalIO.getColumns(), terminalIO.getRows());
        this.terminalIO = terminalIO;

        buffer = new char[terminalIO.getColumns() * terminalIO.getRows()];
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = ' ';
        }
    }

    /**
     * Copy the content of the given rawData into this screen.
     *
     * @param rawData
     * @param rawDataOffset
     */
    @Override
    public void copyFrom(char[] rawData, int rawDataOffset) {
        if (rawDataOffset < 0) {
            // Unsafe.die("Screen:rawDataOffset = " + rawDataOffset);
        }
        char[] cha = new char[rawData.length];
        for (int i = 0; i < cha.length; i++) {
            cha[i] = getCharacter(rawData[i]);
        }
        System.arraycopy(cha, rawDataOffset, buffer, 0, buffer.length);
        sync(0, buffer.length);
    }

    /**
     * {@inheritDoc}
     */
    public void copyContent(int srcOffset, int destOffset, int length) {
        System.arraycopy(buffer, srcOffset * 2, buffer, destOffset * 2, length * 2);
        sync(destOffset * 2, length * 2);
    }

    /**
     * {@inheritDoc}
     */
    public void copyTo(TextScreen dst, int offset, int length) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    public char getChar(int offset) {
        return buffer[offset];
    }

    /**
     * {@inheritDoc}
     */
    public int getColor(int offset) {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public void set(int offset, char ch, int count, int color) {
        buffer[offset] = getCharacter(ch);
        sync(offset, 1);
    }

    private char getCharacter(char ch) {
        char c = (char) (ch & 0xFF);
        return (c == 0) ? ' ' : c;
    }

    /**
     * {@inheritDoc}
     */
    public void set(int offset, char[] ch, int chOfs, int length, int color) {
        char[] cha = new char[ch.length];
        for (int i = 0; i < cha.length; i++) {
            cha[i] = getCharacter(ch[i]);
        }
        System.arraycopy(cha, chOfs, buffer, offset, length);
        sync(offset, length);
    }

    /**
     * {@inheritDoc}
     */
    public void set(int offset, char[] ch, int chOfs, int length, int[] colors, int colorsOfs) {
        set(offset, ch, chOfs, length, 0);
    }

    /**
     * {@inheritDoc}
     */
    public int setCursor(int x, int y) {
        try {
            terminalIO.setCursor(y, x);
            cursorOffset = getOffset(x, y);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return cursorOffset;
    }

    /**
     * {@inheritDoc}
     */
    public int setCursorVisible(boolean visible) {
        // ignore : cursor will allways be visible
        return cursorOffset;
    }

    /**
     * {@inheritDoc}
     */
    public void sync(int offset, int length) {
        try {
            final int y = offset / getWidth();
            final int x = offset % getWidth();
            terminalIO.setCursor(y, x);

            final TelnetIO telnetIO = terminalIO.getTelnetIO();

            int offs = offset;
            for (int i = 0; i < length; i++) {
                telnetIO.write(buffer[offs++]);
            }
            if (terminalIO.isAutoflushing()) {
                terminalIO.flush();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
