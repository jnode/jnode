/*
 * $Id$
 *
 * Copyright (C) 2003-2012 JNode.org
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
 
package org.jnode.driver.console.textscreen;

import java.io.PrintStream;
import java.io.Reader;
import java.io.Writer;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.jnode.driver.console.ConsoleManager;
import org.jnode.driver.console.InputCompleter;
import org.jnode.driver.console.TextConsole;
import org.jnode.driver.console.spi.AbstractConsole;
import org.jnode.driver.console.spi.ConsoleWriter;
import org.jnode.driver.textscreen.ScrollableTextScreen;
import org.jnode.driver.textscreen.TextScreen;
import org.jnode.system.event.FocusEvent;
import org.jnode.system.event.FocusListener;
import org.jnode.util.WriterOutputStream;
import org.jnode.vm.VmSystem;
import org.jnode.vm.isolate.VmIsolate;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author Levente S\u00e1ntha (lsantha@users.sourceforge.net)
 */
public class TextScreenConsole extends AbstractConsole implements TextConsole {

    /**
     * The screen I'm writing on
     */
    private TextScreen screen;

    /**
     * Width of the screen
     */
    private final int scrWidth;

    /**
     * Height of the screen
     */
    private final int scrHeight;

    /**
     * Current tab size
     */
    private int tabSize = 4;

    /**
     * Current X position
     */
    private int curX;

    /**
     * Current Y position
     */
    private int curY;

    private Reader in;

    private final Writer out;

    private final Writer err;

    private PrintStream savedOut;

    private PrintStream savedErr;

    private boolean cursorVisible = true;

    private final boolean claimSystemOutErr;

    private VmIsolate myIsolate;
    
    /**
     * The options used to create this {@link TextScreenConsole}
     */
    private final int options;

    /**
     * @param mgr
     * @param name
     * @param screen
     */
    public TextScreenConsole(ConsoleManager mgr, String name, TextScreen screen, int options) {
        super(mgr, name);
        this.options = options;
        this.screen = screen;
        this.scrWidth = screen.getWidth();
        this.scrHeight = screen.getHeight();
        this.out = new ConsoleWriter(this, 0x07);
        this.err = new ConsoleWriter(this, 0x04);
        this.savedOut = new PrintStream(new WriterOutputStream(this.out, false), true);
        this.savedErr = new PrintStream(new WriterOutputStream(this.err, false), true);
        this.claimSystemOutErr = false;
        this.myIsolate = VmIsolate.currentIsolate();
    }

    /**
     * Clear the console
     *
     * @see org.jnode.driver.console.TextConsole#clear()
     */
    @Override
    public void clear() {
        final int size = screen.getWidth() * screen.getHeight();
        screen.set(0, ' ', size, 0x07);
        syncScreen(0, size);
    }

    /**
     * Clear a given row
     */
    @Override
    public void clearRow(int row) {
        final int size = screen.getWidth();
        final int offset = screen.getOffset(0, row);
        screen.set(offset, ' ', size, 0x07);
        syncScreen(offset, size);
    }

    /**
     * Append characters to the current line.
     *
     * @param v
     * @param offset
     * @param length
     * @param color
     */
    @Override
    public void putChar(char v[], int offset, int length, int color) {
        // This method tries to paint runs of characters on the current screen
        // line with one call to screen.set(int, char[], int, int, int).  The
        // 'mark' is the offset of the start of the current run.  The 'limit'
        // is the offset at which we must end (or have ended) the current run.
        int mark = 0;
        int limit = Math.min(length, scrWidth - curX) - 1;
        for (int i = 0; i < length; i++) {
            char c = v[i + offset];
            if (c == '\n' || c == '\b' || c == '\t' || i == limit) {
                // The current run ends now.  First, output all but 'c' directly to the
                // current screen line, adjusting curX and curY when we're done.
                final int ln = i - mark;
                if (ln > 0) {
                    screen.set(screen.getOffset(curX, curY), v, offset + mark, ln, color);
                    curX += ln;
                    if (curX >= scrWidth) {
                        curY++;
                        curX -= scrWidth;
                    }
                }
                // Then output 'c' using doPutChar.  This knows how to deal with 
                // '\n', '\b' and '\t', and adjusts curX and curY accordingly.
                doPutChar(c, color);
                // Finally update 'mark' and 'limit' for the next run of characters.
                mark = i + 1;
                limit = Math.min(length - 1, i + scrWidth - curX);
            }
        }
        ensureVisible(screen, curY);
    }

    /**
     * Append a character to the current line.
     *
     * @param v
     * @param color
     */
    @Override
    public void putChar(char v, int color) {
        doPutChar(v, color);
        ensureVisible(screen, curY);
    }

    private void doPutChar(char v, int color) {
        if (v == '\n') {
            // Goto next line
            // Clear till eol
            screen.set(screen.getOffset(curX, curY), ' ', scrWidth - curX, color);
            curX = 0;
            curY++;
        } else if (v == '\b') {
            if (curX > 0) {
                curX--;
            } else if (curY > 0) {
                curX = scrWidth - 1;
                curY--;
            }
            setChar(curX, curY, ' ', color);
        } else if (v == '\t') {
            putChar(' ', color);
            while ((curX % tabSize) != 0) {
                putChar(' ', color);
            }
        } else {
            setChar(curX, curY, v, color);
            curX++;
            if (curX >= scrWidth) {
                curY++;
                curX = 0;
            }
        }
        while (curY >= scrHeight) {
            screen.copyContent(scrWidth, 0, (scrHeight - 1) * scrWidth);
            curY--;
            clearRow(curY);
        }
    }

    /**
     * @return Returns the tabSize.
     */
    @Override
    public int getTabSize() {
        return tabSize;
    }

    @Override
    public void setTabSize(int tabSize) {
        this.tabSize = tabSize;
    }

    @Override
    public int getColor(int x, int y) {
        return screen.getColor(screen.getOffset(x, y));
    }

    @Override
    public char getChar(int x, int y) {
        return screen.getChar(screen.getOffset(x, y));
    }

    @Override
    public int getCursorX() {
        return curX;
    }

    @Override
    public int getCursorY() {
        return curY;
    }

    @Override
    public int getHeight() {
        return screen.getHeight();
    }

    @Override
    public int getWidth() {
        return screen.getWidth();
    }

    @Override
    public int getDeviceHeight() {
        return screen.getDeviceHeight();
    }

    @Override
    public int getDeviceWidth() {
        return screen.getDeviceWidth();
    }

    @Override
    public void setChar(int x, int y, char ch, int color) {
        int offset = screen.getOffset(x, y);
        screen.set(offset, ch, 1, color);
        syncScreen(offset, 1);
    }

    @Override
    public void setChar(int x, int y, char[] ch, int color) {
        int offset = screen.getOffset(x, y);
        screen.set(offset, ch, 0, ch.length, color);
        syncScreen(offset, ch.length);
    }

    @Override
    public void setChar(int x, int y, char[] ch, int cOfset, int cLength, int color) {
        int offset = screen.getOffset(x, y);
        screen.set(offset, ch, cOfset, cLength, color);
        syncScreen(offset, cLength);
    }

    @Override
    public void setCursor(int x, int y) {
        this.curX = x;
        this.curY = y;
        int offset = screen.setCursor(x, y);
        syncScreen(offset, 1);
    }

    private void syncScreen(int offset, int size) {
        if (isFocused()) {
            screen.sync(offset, size);
        }
    }

    @Override
    public InputCompleter getCompleter() {
        if (in instanceof KeyboardReader) {
            return ((KeyboardReader) in).getCompleter();
        } else {
            return null;
        }
    }

    @Override
    public void setCompleter(InputCompleter completer) {
        if (in instanceof KeyboardReader) {
            ((KeyboardReader) in).setCompleter(completer);
        }
    }
    
    @Override
    public ConsoleKeyEventBindings getKeyEventBindings() {
        if (in instanceof KeyboardReader) {
            return ((KeyboardReader) in).getKeyEventBindings();
        } else {
            throw new UnsupportedOperationException("key event bindings not available");
        }
    }

    @Override
    public void setKeyEventBindings(ConsoleKeyEventBindings bindings) {
        if (in instanceof KeyboardReader) {
            ((KeyboardReader) in).setKeyEventBindings(bindings);
        } else {
            throw new UnsupportedOperationException("key event bindings cannt be set");
        }
    }

    @Override
    public Reader getIn() {
        return in;
    }

    void setIn(Reader in) {
        this.in = in;
    }

    @Override
    public Writer getErr() {
        return err;
    }

    @Override
    public Writer getOut() {
        return out;
    }

    /**
     * Is the cursor visible.
     */
    @Override
    public boolean isCursorVisible() {
        return cursorVisible;
    }

    /**
     * Make the cursor visible or not visible.
     *
     * @param visible
     */
    @Override
    public void setCursorVisible(boolean visible) {
        this.cursorVisible = visible;
        int offset = screen.setCursorVisible(visible);
        syncScreen(offset, 1);
    }

    @Override
    public void focusGained(FocusEvent event) {
        super.focusGained(event);
        syncScreen(0, screen.getWidth() * screen.getHeight());
        if (in instanceof FocusListener) {
            ((FocusListener) in).focusGained(event);
        }
        if (claimSystemOutErr && VmSystem.hasVmIOContext()) {
            myIsolate.invokeAndWait(new Runnable() {
                public void run() {
                    AccessController.doPrivileged(new PrivilegedAction<Object>() {
                        public Object run() {
                            System.setOut(savedOut);
                            System.setErr(savedErr);
                            return null;
                        }
                    });
                }
            });
        }
    }

    @Override
    public void focusLost(FocusEvent event) {
        if (in instanceof FocusListener) {
            ((FocusListener) in).focusLost(event);
        }
        if (claimSystemOutErr && VmSystem.hasVmIOContext()) {
            myIsolate.invokeAndWait(new Runnable() {
                public void run() {
                    savedOut = System.out;
                    savedErr = System.err;
                }
            });
        }
        super.focusLost(event);
    }

    /**
     * @return Returns the screen.
     */
    protected final TextScreen getScreen() {
        return this.screen;
    }

    /**
     * Get the options used to create this {@link TextScreenConsole}
     * @return the options.
     */
    final int getOptions() {
        return options;
    }
    
    @Override
    public void systemScreenChanged(TextScreen systemScreen) {
        // ensure that old and new screens are compatible
        if ((systemScreen.getWidth() != screen.getWidth()) || (systemScreen.getHeight() != screen.getHeight())) {
            throw new IllegalArgumentException("old and new screen have different sizes");
        }
    
        TextScreen oldScreen = screen;
        screen = systemScreen;
        
        final int size = oldScreen.getWidth() * oldScreen.getHeight();
        ensureVisible(oldScreen, 0);
        oldScreen.copyTo(screen, 0, size);
        syncScreen(0, size);
    }
    
    private final void ensureVisible(TextScreen scr, int row) {
        if (scr instanceof ScrollableTextScreen) {
            ((ScrollableTextScreen) scr).ensureVisible(row, isFocused());
        }
    }
}
