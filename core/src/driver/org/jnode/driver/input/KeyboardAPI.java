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

package org.jnode.driver.input;

import org.jnode.driver.DeviceAPI;
import org.jnode.driver.DriverPermission;

/**
 * @author epr
 */
public interface KeyboardAPI extends DeviceAPI {

    /**
     * Permission
     */
    public static final DriverPermission SET_PREFERRED_LISTENER_PERMISSION =
        new DriverPermission("setPreferredListener");

    /**
     * Add a keyboard listener
     *
     * @param l
     */
    public abstract void addKeyboardListener(KeyboardListener l);

    /**
     * Remove a keyboard listener
     *
     * @param l
     */
    public abstract void removeKeyboardListener(KeyboardListener l);

    /**
     * Claim to be the preferred listener.
     * The given listener must have been added by addKeyboardListener.
     * If there is a security manager, this method will call
     * <code>checkPermission(new DriverPermission("setPreferredListener"))</code>.
     *
     * @param l
     */
    public abstract void setPreferredListener(KeyboardListener l);

    /**
     * @return KeyboardInterpreter
     */
    public abstract KeyboardInterpreter getKbInterpreter();

    /**
     * Sets the kbInterpreter.
     *
     * @param kbInterpreter The kbInterpreter to set
     */
    public abstract void setKbInterpreter(KeyboardInterpreter kbInterpreter);
}
