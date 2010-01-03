/*
 * $Id$
 *
 * Copyright (C) 2003-2010 JNode.org
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
 
package org.jnode.fs.service;

import java.io.IOException;
import org.jnode.java.io.VMFileSystemAPI;
import java.util.Collection;
import java.util.Map;

import org.jnode.driver.Device;
import org.jnode.fs.FileSystem;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.FileSystemType;

/**
 * @author epr
 */
public interface FileSystemService {

    /**
     * Name used to lookup a FileSystemTypeManager in the initial namespace.
     */
    public static final Class<FileSystemService> NAME = FileSystemService.class; // "system/FileSystemService";

    /**
     * Gets all registered file system types. 
     */
    public Collection<FileSystemType<?>> fileSystemTypes();

    /**
     * Gets registered file system types with the given name.
     * 
     * @param name the name of the FSType you want
     * @return the fileSystemType
     */
    public <T extends FileSystemType<?>> T getFileSystemType(Class<T> name)
        throws FileSystemException;

    /**
     * Register a mounted filesystem
     * 
     * @param fs
     */
    public void registerFileSystem(FileSystem<?> fs) throws FileSystemException;

    /**
     * Unregister a mounted filesystem
     * 
     * @param device
     * @return The filesystem that was registered for the device, or null if not
     *         found.
     */
    public FileSystem<?> unregisterFileSystem(Device device);

    /**
     * Gets the filesystem registered on the given device.
     * 
     * @param device
     * @return {@code null} if no filesystem was found.
     */
    public FileSystem<?> getFileSystem(Device device);

    /**
     * Gets all registered filesystems. 
     */
    public Collection<FileSystem<?>> fileSystems();

    /**
     * Mount the given filesystem at the fullPath, using the fsPath as root of
     * the to be mounted filesystem.
     * 
     * @param fullPath
     * @param fs
     * @param fsPath Null or empty to use the root of the filesystem.
     */
    public void mount(String fullPath, FileSystem<?> fs, String fsPath) throws IOException;

    /**
     * Return a map (fullPath -> FileSystem) of mount points
     * 
     * @return a copy of the internal map, sorted by fullPath
     */
    public Map<String, FileSystem<?>> getMountPoints();

    /**
     * Return a map of devices and their mount points.
     * 
     * @return the map
     */
    public Map<String, String> getDeviceMountPoints();

    /**
     * Is the given directory a mount.
     * 
     * @param fullPath
     * @return {@code true} if the directory is a mount, otherwise {@code false}
     */
    public boolean isMount(String fullPath);

    /**
     * Gets the filesystem API.
     */
    public VMFileSystemAPI getApi();
}
