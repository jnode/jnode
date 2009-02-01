/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
 *
 * JNode.org
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
 
package org.jnode.driver.console;

import java.io.Reader;
import java.io.Writer;

import org.jnode.driver.input.KeyboardEvent;
import org.jnode.driver.input.KeyboardListener;
import org.jnode.driver.input.PointerEvent;
import org.jnode.driver.input.PointerListener;
import org.jnode.driver.textscreen.TextScreen;
import org.jnode.system.event.FocusEvent;

/**
 * This is the base class for virtual text consoles; e.g. ones that
 * redirect to other text consoles.
 * 
 * @author crawley@jnode.org
 */
public abstract class VirtualTextConsole implements TextConsole {

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clearRow(int row) {
        throw new UnsupportedOperationException();
    }

    @Override
    public char getChar(int x, int y) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getColor(int x, int y) {
        throw new UnsupportedOperationException();
    }

    @Override
    public InputCompleter getCompleter() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getCursorX() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getCursorY() {
        throw new UnsupportedOperationException();
    }

    @Override
    public abstract Writer getErr();

    @Override
    public abstract int getHeight();

    @Override
    public abstract Reader getIn();

    @Override
    public abstract Writer getOut();

    @Override
    public abstract int getTabSize();

    @Override
    public abstract int getWidth();

    @Override
    public boolean isCursorVisible() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putChar(char v, int color) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putChar(char[] v, int offset, int length, int color) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setChar(int x, int y, char ch, int color) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setChar(int x, int y, char[] cbuf, int color) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setChar(int x, int y, char[] cbuf, int offset, int length, int color) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCompleter(InputCompleter completer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCursor(int x, int y) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCursorVisible(boolean visible) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setTabSize(int tabSize) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addConsoleListener(ConsoleListener l) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addKeyboardListener(KeyboardListener l) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addPointerListener(PointerListener l) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
    }

    @Override
    public int getAcceleratorKeyCode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public abstract String getConsoleName();
    
    @Override
    public abstract ConsoleManager getManager();

    @Override
    public boolean isFocused() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeConsoleListener(ConsoleListener l) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeKeyboardListener(KeyboardListener l) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removePointerListener(PointerListener l) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAcceleratorKeyCode(int keyCode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void systemScreenChanged(TextScreen textScreen) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void focusGained(FocusEvent event) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void focusLost(FocusEvent event) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void keyPressed(KeyboardEvent event) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void keyReleased(KeyboardEvent event) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void pointerStateChanged(PointerEvent event) {
        throw new UnsupportedOperationException();
    }

}
