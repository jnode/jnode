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

package org.jnode.driver.console;

import org.jnode.driver.input.KeyboardListener;
import org.jnode.driver.input.PointerListener;
import org.jnode.system.event.FocusListener;

/**
 * @author epr
 */
public interface Console extends FocusListener, KeyboardListener,
    PointerListener {

    /**
     * Has this control the focus.
     *
     * @return
     */
    public boolean isFocused();

    /**
     * Close this console
     */
    public void close();

    /**
     * returns the registered name for this console
     *
     * @return registered name for this console
     */
    public String getConsoleName();

    /**
     * Sets the keycode of the accelerator that will focus this console.
     *
     * @param keyCode
     */
    public void setAcceleratorKeyCode(int keyCode);

    /**
     * Gets the keycode of the accelerator that will focus this console.
     */
    public int getAcceleratorKeyCode();

    /**
     * Add a keyboard listener
     *
     * @param l
     */
    public void addKeyboardListener(KeyboardListener l);

    /**
     * Remove a keyboard listener
     *
     * @param l
     */
    public void removeKeyboardListener(KeyboardListener l);

    /**
     * Add a pointer listener
     *
     * @param l
     */
    public void addPointerListener(PointerListener l);

    /**
     * Remove a pointer listener
     *
     * @param l
     */
    public void removePointerListener(PointerListener l);

    /**
     * Add a console listener
     *
     * @param l
     */
    public void addConsoleListener(ConsoleListener l);

    /**
     * Remove a console listener
     *
     * @param l
     */
    public void removeConsoleListener(ConsoleListener l);


    /**
     * Gets the manager this console is registered with.
     *
     * @return
     */
    public ConsoleManager getManager();
}
