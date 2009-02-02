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
 
package org.jnode.fs.jifs;

import org.apache.log4j.Logger;
import org.jnode.driver.Device;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.FileSystemType;

/**
 * @author Andreas H\u00e4nel
 */
public class JIFileSystemType implements FileSystemType<JIFileSystem> {
    public static final Class<JIFileSystemType> ID = JIFileSystemType.class;

    public static final String VIRTUAL_DEVICE_NAME = "jifs";

    /** Logger */
    private static final Logger log = Logger.getLogger(JIFileSystemType.class);

    /**
     * Gets the unique name of this file system type.
     */
    public String getName() {
        return "JIFS";
    }

    /**
     * Create a filesystem for a given device.
     * 
     * @param device
     * @param readOnly
     * @return JIFileSystem(device,readOnly)
     * @throws FileSystemException
     */
    public JIFileSystem create(Device device, boolean readOnly) throws FileSystemException {
        JIFileSystem ret = new JIFileSystem(device, readOnly, this);
        if (ret == null) {
            log.error("got NULL from the JIFileSystem...");
        }
        return ret;
    }
}
