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

package org.jnode.driver.video.ddc;

import org.jnode.driver.DeviceAPI;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface DisplayDataChannelAPI extends DeviceAPI {

    /**
     * Start a DDC1 readout
     */
    public void setupDDC1();

    /**
     * Terminate a DDC1 readout
     */
    public void closeDDC1();

    /**
     * Wait for the vsync signal and return the current ddc1 bit.
     * 
     * @return True for '1', false for '0'
     */
    public boolean getDDC1Bit();

}
