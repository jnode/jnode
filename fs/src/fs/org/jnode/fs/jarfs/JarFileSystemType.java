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
 
package org.jnode.fs.jarfs;

import org.jnode.driver.Device;
import org.jnode.driver.block.JarFileDevice;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.FileSystemType;

/**
 * 
 * @author Fabien DUMINY (fduminy at users.sourceforge.net)
 * 
 */
public class JarFileSystemType implements FileSystemType<JarFileSystem> {
    public static final Class<JarFileSystemType> ID = JarFileSystemType.class;

    public final String getName() {
        return "jar";
    }

    /**
     * @see org.jnode.fs.FileSystemType#create(Device, boolean)
     */
    public JarFileSystem create(Device device, boolean readOnly) throws FileSystemException {
        // jar file systems are always readOnly
        return new JarFileSystem((JarFileDevice) device, this);
    }
}
