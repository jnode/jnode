/*
 * $Id$
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
	private final HashMap filesystems = new HashMap();

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
		String idToMount = getMountPoint(fs.getDevice());
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
	public synchronized Collection fileSystems() {
		return new ArrayList(filesystems.values());
	}

	public synchronized Set fileSystemRoots() {
		return new TreeSet(filesystems.keySet());
	}
}
