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
 
package org.jnode.fs.service.def;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jnode.driver.Device;
import org.jnode.fs.FileSystem;

/**
 * @author epr
 */
final class FileSystemManager {
    /** All registed filesystems (device, fs) */
    private final Map<Device, FileSystem<?>> filesystems = new HashMap<Device, FileSystem<?>>();

    /**
     * Register a mounted filesystem
     * 
     * @param fs
     */
    public void registerFileSystem(FileSystem<?> fs) {
        final Device device = fs.getDevice();
        synchronized (this) {
            filesystems.put(device, fs);
        }
    }

    /**
     * Unregister a mounted filesystem
     * 
     * @param device
     */
    public synchronized FileSystem<?> unregisterFileSystem(Device device) {
        return filesystems.remove(device);
    }

    /**
     * Gets the filesystem registered on the given device.
     * 
     * @param device
     * @return null if no filesystem was found.
     */
    public synchronized FileSystem<?> getFileSystem(Device device) {
        return filesystems.get(device);
    }

    /**
     * Gets all registered filesystems. All instances of the returned collection
     * are instanceof FileSystem.
     */
    public synchronized Collection<FileSystem<?>> fileSystems() {
        return new ArrayList<FileSystem<?>>(filesystems.values());
    }
}
