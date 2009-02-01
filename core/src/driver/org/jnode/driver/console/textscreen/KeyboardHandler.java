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
 
package org.jnode.driver.console.textscreen;

import java.io.IOException;

import org.jnode.driver.input.KeyboardEvent;
import org.jnode.util.Queue;


/**
 * KeyInputStream maps keyboard events into a stream of characters.  Current functionality includes:
 * <ul>
 * <li>line buffering and line editing, using a text console,
 * <li>integrated input history and completion,
 * <li>CTRL-D is interpretted as a 'soft' EOF mark,KeyboardInputStream
 * <li>listens to keyboard focus events.
 * </ul>
 * 
 * Future enhancements include:
 * <ul>
 * <li>a "raw" mode in which characters and other keyboard events are delivered without line editing,
 * <li>a "no echo" mode in which line editting occurs without echoing of input characters,
 * <li>making the active characters and keycodes "soft",
 * <li>making completion and history context sensitive; e.g. when switching between a shell and 
 * an application, and
 * <li>code refactoring to support classical terminal devices and remote consoles.
 * </ul>
 * 
 * Bugs:
 * <ul>
 * <li>The current method of echoing the input is suboptimal, and is broken in the case where an 
 * application outputs a prompt string to stdout or stderr.
 * </ul>
 * 
 * @author crawley@jnode.org
 *
 */
public abstract class KeyboardHandler {

    /** The queue of keyboard events */
    private final Queue<KeyboardEvent> queue = new Queue<KeyboardEvent>();

    protected void postEvent(KeyboardEvent event) {
        queue.add(event);
    }

    /**
     * Get the next KeyboardEvent from the internal queue (and wait if none is
     * available).
     * 
     * @return
     */
    public final KeyboardEvent getEvent() {
        return queue.get();
    }

    public abstract void close() throws IOException;
}
