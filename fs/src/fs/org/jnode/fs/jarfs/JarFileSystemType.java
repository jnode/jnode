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
public class JarFileSystemType implements FileSystemType<JarFileSystem> {

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
    public JarFileSystem create(Device device, boolean readOnly) throws FileSystemException {
        // jar file systems are always readOnly
        return new JarFileSystem((JarFileDevice) device);
    }
}
