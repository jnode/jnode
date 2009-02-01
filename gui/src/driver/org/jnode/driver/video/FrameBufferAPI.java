/*
 * $Id$
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
 
package org.jnode.driver.video;

import org.jnode.driver.DeviceAPI;
import org.jnode.driver.DeviceException;

/**
 * This API must be implemented by all FrameBuffer devices. Is is used to
 * retrieve all configurations of the device and open a graphics object for a
 * specific configuration.
 * 
 * @author epr
 */
public interface FrameBufferAPI extends DeviceAPI {

    /**
     * Gets all configurations supported by this framebuffer device.
     */
    public FrameBufferConfiguration[] getConfigurations();

    /**
     * Gets the current configuration of this framebuffer.
     */
    public FrameBufferConfiguration getCurrentConfiguration();

    /**
     * Open a specific framebuffer configuration
     * 
     * @param config
     */
    public Surface open(FrameBufferConfiguration config)
        throws UnknownConfigurationException, AlreadyOpenException, DeviceException;

    /**
     * Is there an open framebuffer configuration.
     * 
     * @return
     */
    public boolean isOpen();
    
    /**
     * Request to be the owner of the underlying FrameBuffer device.
     * The old owner (if any) will receive a request to stop using the underlying FrameBuffer device.
     * 
     * @param owner
     * @return true if owner can now start using exclusively the underlying FrameBuffer device.
     */
    public void requestOwnership(FrameBufferAPIOwner owner);

    /**
     * Request the ownership on the underlying FrameBuffer device.
     * 
     * @param owner
     * @return true if owner can now start using exclusively the underlying FrameBuffer device.
     */
    public void releaseOwnership(FrameBufferAPIOwner owner);
    
    /**
     * Gets the currently opened framebuffer configuration.
     * 
     * @return
     */
    public Surface getCurrentSurface() throws NotOpenException;
}
