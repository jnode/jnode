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
 
package org.jnode.fs.service.def;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import org.jnode.driver.Device;
import org.jnode.fs.FileSystem;

/**
 * @author epr
 */
final class FileSystemManager {

	/** All registed filesystems (device, fs) */
	private final HashMap<String, FileSystem> filesystems = new HashMap<String, FileSystem>();

	protected String getMountPoint(Device device) {
		return device.getId();
	}
	
	/**
	 * Register a mounted filesystem
	 * 
	 * @param fs
	 */
	public synchronized void registerFileSystem(FileSystem fs) {
		//String idToMount = fs.getDevice().getId();
		final String idToMount = getMountPoint(fs.getDevice());
		filesystems.put(idToMount, fs);
	}

	/**
	 * Unregister a mounted filesystem
	 * 
	 * @param device
	 */
	public synchronized FileSystem unregisterFileSystem(Device device) {
		//return (FileSystem)filesystems.remove(device.getId());
		return (FileSystem)filesystems.remove(getMountPoint(device));

	}

	/**
	 * Gets the filesystem registered on the given device.
	 * 
	 * @param device
	 * @return null if no filesystem was found.
	 */
	public synchronized FileSystem getFileSystem(Device device) {
		//return (FileSystem)filesystems.get(device.getId());
		return (FileSystem)filesystems.get(getMountPoint(device));
	}

	/**
	 * Gets the filesystem registered on the given name.
	 * 
	 * @param rootName
	 * @return null if no filesystem was found.
	 */
	public synchronized FileSystem getFileSystem(String rootName) {
		return (FileSystem)filesystems.get(rootName);
	}

	/**
	 * Gets all registered filesystems. All instances of the returned collection
	 * are instanceof FileSystem.
	 */
	public synchronized Collection<FileSystem> fileSystems() {
		return new ArrayList<FileSystem>(filesystems.values());
	}

	public synchronized Set<String> fileSystemRoots() {
		return new TreeSet<String>(filesystems.keySet());
	}
}
