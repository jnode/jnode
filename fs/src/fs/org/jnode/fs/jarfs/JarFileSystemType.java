/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.fs.jarfs;

import org.jnode.driver.Device;
import org.jnode.driver.block.FSBlockDeviceAPI;
import org.jnode.driver.block.JarFileDevice;
import org.jnode.fs.FileSystem;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.FileSystemType;
import org.jnode.partitions.PartitionTableEntry;

/**
 * 
 * @author Fabien DUMINY (fduminy at users.sourceforge.net)
 *
 */
public class JarFileSystemType implements FileSystemType {

    public static final String NAME = "jar";

    public final String getName() {
        return NAME;
    }

    /**
     * @see org.jnode.fs.FileSystemType#supports(PartitionTableEntry, byte[],
     *      FSBlockDeviceAPI)
     */
    public boolean supports(PartitionTableEntry pte, byte[] firstSector,
            FSBlockDeviceAPI devApi) {
        return false;
    }

    /**
     * @see org.jnode.fs.FileSystemType#create(Device, boolean)
     */
    public FileSystem create(Device device, boolean readOnly) throws FileSystemException {
        // jar file systems are always readOnly        
        return new JarFileSystem((JarFileDevice) device);
    }

    /**
     * @see org.jnode.fs.FileSystemType#format(org.jnode.driver.Device,
     *      java.lang.Object)
     */
    public FileSystem format(Device device, Object specificOptions)
            throws FileSystemException {
        throw new FileSystemException("Not yet implemented");
    }
}
