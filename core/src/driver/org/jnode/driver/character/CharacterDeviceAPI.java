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

package org.jnode.driver.character;

import java.nio.channels.ByteChannel;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceAPI;
import org.jnode.driver.DeviceException;

/**
 * @author qades
 */
public interface CharacterDeviceAPI extends DeviceAPI {

    /**
     * Aquire the channel associated with this device.
     *
     * @param owner the subsequent owner of this channel
     * @return The channel
     * @throws ChannelAlreadyOwnedException if the channel to this device is already owned
     * @throws DeviceException
     */
    public ByteChannel getChannel(Device owner) throws ChannelAlreadyOwnedException, DeviceException;
}
