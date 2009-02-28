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

import java.awt.event.KeyEvent;
import java.io.IOException;

import net.wimpi.telnetd.io.BasicTerminalIO;

import org.jnode.driver.console.textscreen.KeyboardHandler;
import org.jnode.driver.input.KeyboardEvent;

/**
 * 
 * @author Fabien DUMINY (fduminy at jnode.org)
 * 
 */
public class RemoteKeyboardHandler extends KeyboardHandler {
    private final RemoteKeyBoardEventReader kbReader;

    /**
     * Construct a remote keyboard handler for the given terminal.
     * 
     * @param terminalIO
     */
    public RemoteKeyboardHandler(BasicTerminalIO terminalIO) {
        System.err.println("RemoteKeyboardHandler");

        kbReader = new RemoteKeyBoardEventReader(terminalIO);
        kbReader.start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        kbReader.close();
    }

    private class RemoteKeyBoardEventReader extends Thread {
        private boolean closed = false;
        private final BasicTerminalIO terminalIO;

        public RemoteKeyBoardEventReader(BasicTerminalIO terminalIO) {
            this.terminalIO = terminalIO;
        }

        public void run() {
            while (!closed) {
                try {
                    int ch = terminalIO.read();
                    char chr;

                    switch (ch) {
                        case BasicTerminalIO.LEFT:
                            chr = KeyEvent.VK_LEFT;
                            break;
                        case BasicTerminalIO.RIGHT:
                            chr = KeyEvent.VK_RIGHT;
                            break;
                        case BasicTerminalIO.UP:
                            chr = KeyEvent.VK_UP;
                            break;
                        case BasicTerminalIO.DOWN:
                            chr = KeyEvent.VK_DOWN;
                            break;
                        // case java.awt.event.KeyEvent.VK_PAGE_DOWN:
                        // chr = KeyEvent.VK_PAGE_DOWN;
                        // break;
                        // case java.awt.event.KeyEvent.VK_PAGE_UP:
                        // key = KeyEvent.VK_PAGE_UP;
                        // break;
                        //
                        // case java.awt.event.KeyEvent.VK_HOME:
                        // key = KeyEvent.VK_HOME;
                        // break;
                        //
                        // case java.awt.event.KeyEvent.VK_END:
                        // key = KeyEvent.VK_END;
                        // break;

                        case BasicTerminalIO.BACKSPACE:
                            chr = KeyEvent.VK_BACK_SPACE;
                            break;

                        // case java.awt.event.KeyEvent.VK_INSERT:
                        // key = KeyEvent.VK_INSERT;
                        // break;

                        case BasicTerminalIO.DELETE:
                            chr = KeyEvent.VK_DELETE;
                            break;

                        case BasicTerminalIO.ENTER:
                            chr = KeyEvent.VK_ENTER;
                            break;

                        // case java.awt.event.KeyEvent.ESCAPE:
                        // key = KeyEvent.VK_ESCAPE;
                        // break;

                        default:
                            chr = (char) ch;
                    }

                    System.err.println("ch=" + ch);
                    long time = System.currentTimeMillis();
                    KeyboardEvent e = new KeyboardEvent(KeyEvent.KEY_PRESSED, time, 0, chr, chr);
                    System.err.println("event built");
                    postEvent(e);
                    System.err.println("event sent");
                } catch (IOException e) {
                    System.err.println("error : " + e.getMessage());
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        public void close() throws IOException {
            terminalIO.close();
            closed = true;
        }
    }
}
