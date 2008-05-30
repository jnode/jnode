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

/**
 * Device API implemented by Pointer devices.
 *
 * @author qades
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface PointerAPI extends DeviceAPI {

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
}
