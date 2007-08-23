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
 
package org.jnode.driver.console.textscreen;

import java.io.InputStream;
import java.io.PrintStream;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.jnode.driver.console.ConsoleManager;
import org.jnode.driver.console.InputCompleter;
import org.jnode.driver.console.TextConsole;
import org.jnode.driver.console.spi.AbstractConsole;
import org.jnode.driver.console.spi.ConsoleOutputStream;
import org.jnode.driver.console.spi.ConsolePrintStream;
import org.jnode.driver.textscreen.TextScreen;
import org.jnode.system.event.FocusEvent;
import org.jnode.system.event.FocusListener;
import org.jnode.vm.VmSystem;
import org.jnode.vm.isolate.VmIsolate;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author Levente S\u00e1ntha (lsantha@users.sourceforge.net)
 */
public class TextScreenConsole extends AbstractConsole implements TextConsole {

	/** The screen I'm writing on */
	private final TextScreen screen;

	/** Width of the screen */
	private final int scrWidth;

	/** Height of the screen */
	private final int scrHeight;

	/** Current tab size */
	private int tabSize = 4;

	/** Current X position */
	private int curX;

	/** Current Y position */
	private int curY;
	
	private InputStream in;

	private final ConsolePrintStream out;

	private final ConsolePrintStream err;

	private PrintStream savedOut;

	private PrintStream savedErr;

	private boolean cursorVisible = true;

	private final boolean claimSystemOutErr;

    private VmIsolate myIsolate;

    /**
	 * @param mgr
	 * @param name
	 * @param screen
	 */
	public TextScreenConsole(ConsoleManager mgr, String name,
			TextScreen screen, int options) {
		super(mgr, name);
		this.screen = screen;
		this.scrWidth = screen.getWidth();
		this.scrHeight = screen.getHeight();
		this.savedOut = this.out = new ConsolePrintStream(new ConsoleOutputStream(
				this, 0x07));
		this.savedErr = this.err = new ConsolePrintStream(new ConsoleOutputStream(
				this, 0x04));
		this.claimSystemOutErr = ((options & ConsoleManager.CreateOptions.NO_SYSTEM_OUT_ERR) == 0);
        this.myIsolate = VmIsolate.currentIsolate();
    }

	/**
	 * Clear the console
	 * 
	 * @see org.jnode.driver.console.TextConsole#clear()
	 */
	public void clear() {
		final int size = screen.getWidth() * screen.getHeight();
		screen.set(0, ' ', size, 0x07);
		syncScreen();
	}

	/**
	 * Clear a given row
	 */
	public void clearRow(int row) {
		final int size = screen.getWidth();
		screen.set(screen.getOffset(0, row), ' ', size, 0x07);
		syncScreen();
	}

    /**
	 * Append characters to the current line.
	 *
	 * @param v
     * @param offset
     * @param lenght
	 * @param color
	 */
    public void putChar(char v[], int offset, int lenght, int color) {
        int mark = 0;
        for(int i = 0; i < lenght; i++){
            char c = v[i + offset];
            if(c == '\n' || c =='\b' || c == '\t' ||
                    curX + i == scrWidth - 1 || i == lenght - 1){
                final int ln = i - mark;
                if(ln > 0){
                    screen.set(screen.getOffset(curX, curY), v, offset + mark, ln, color);
                    curX += ln;
                    if (curX >= scrWidth) {
                        curY++;
                        curX = curX - scrWidth;
			        }
                }
                mark = i + 1;
                doPutChar(c, color);
            }
        }
		screen.ensureVisible(curY);
        syncScreen();
    }

	/**
	 * Append a character to the current line.
	 * 
	 * @param v
	 * @param color
	 */
	public void putChar(char v, int color) {
		doPutChar(v, color);
		screen.ensureVisible(curY);
		syncScreen();
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
	public int getTabSize() {
		return tabSize;
	}

	/**
	 * @param tabSize
	 *            The tabSize to set.
	 */
	public void setTabSize(int tabSize) {
		this.tabSize = tabSize;
	}

	/**
	 * @see org.jnode.driver.console.TextConsole#getColor(int, int)
	 */
	public int getColor(int x, int y) {
		return screen.getColor(screen.getOffset(x, y));
	}

	/**
	 * @see org.jnode.driver.console.TextConsole#getChar(int, int)
	 */
	public char getChar(int x, int y) {
		return screen.getChar(screen.getOffset(x, y));
	}

	/**
	 * @see org.jnode.driver.console.TextConsole#getCursorX()
	 */
	public int getCursorX() {
		return curX;
	}

	/**
	 * @see org.jnode.driver.console.TextConsole#getCursorY()
	 */
	public int getCursorY() {
		return curY;
	}

	/**
	 * @see org.jnode.driver.console.TextConsole#getHeight()
	 */
	public int getHeight() {
		return screen.getHeight();
	}

	/**
	 * @see org.jnode.driver.console.TextConsole#getWidth()
	 */
	public int getWidth() {
		return screen.getWidth();
	}

	/**
	 * @see org.jnode.driver.console.TextConsole#setChar(int, int, char, int)
	 */
	public void setChar(int x, int y, char ch, int color) {
		screen.set(screen.getOffset(x, y), ch, 1, color);
		syncScreen();
	}

	public void setChar(int x, int y, char[] ch, int color) {
		screen.set(screen.getOffset(x, y), ch, 0, ch.length, color);
		syncScreen();
	}

	public void setChar(int x, int y, char[] ch, int cOfset, int cLength,
			int color) {
		screen.set(screen.getOffset(x, y), ch, cOfset, cLength, color);
		syncScreen();
	}

	/**
	 * @see org.jnode.driver.console.TextConsole#setCursor(int, int)
	 */
	public void setCursor(int x, int y) {
		this.curX = x;
		this.curY = y;
        screen.setCursor(x,y);
		syncScreen();
	}

	protected final void syncScreen() {
		if (isFocused()) {
			screen.sync();
		}
	}

	/**
	 * Ensure that the given row is visible.
	 * 
	 * @param row
	 */
	public void ensureVisible(int row) {
		screen.ensureVisible(row);
		syncScreen();
	}

	public InputCompleter getCompleter() {
		if (in instanceof KeyboardInputStream) {
			return ((KeyboardInputStream) in).getCompleter();
		}
		else {
			return null;
		}
	}

	public void setCompleter(InputCompleter completer) {
		if (in instanceof KeyboardInputStream) {
			((KeyboardInputStream) in).setCompleter(completer);
		}
	}

	/**
	 * @see org.jnode.driver.console.TextConsole#getIn()
	 */
	public InputStream getIn() {
		return in;
	}

	void setIn(InputStream in) {
		this.in = in;
	}

	/**
	 * @see org.jnode.driver.console.TextConsole#getErr()
	 */
	public PrintStream getErr() {
		return err;
	}

	/**
	 * @see org.jnode.driver.console.TextConsole#getOut()
	 */
	public PrintStream getOut() {
		return out;
	}

	/**
	 * Is the cursor visible.
	 */
	public boolean isCursorVisible() {
		return cursorVisible;
	}

	/**
	 * Make the cursor visible or not visible.
	 * 
	 * @param visible
	 */
	public void setCursorVisible(boolean visible) {
		this.cursorVisible = visible;
        screen.setCursorVisible(visible);
		syncScreen();
	}

	/**
	 * @see org.jnode.system.event.FocusListener#focusGained(org.jnode.system.event.FocusEvent)
	 */
	public void focusGained(FocusEvent event) {
		super.focusGained(event);
		syncScreen();
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

	/**
	 * @see org.jnode.system.event.FocusListener#focusLost(org.jnode.system.event.FocusEvent)
	 */
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
}
