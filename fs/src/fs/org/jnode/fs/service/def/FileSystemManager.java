/*
 * $Id$
 */
package org.jnode.fs.service.def;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import org.jnode.driver.Device;
import org.jnode.fs.FileSystem;

/**
 * @author epr
 */
public class FileSystemManager {

	/** All registed filesystems (device, fs) */
	private final HashMap filesystems = new HashMap();

	/**
	 * Create a new instance
	 */
	protected FileSystemManager() {
	}

	/**
	 * Register a mounted filesystem
	 * @param fs
	 */
	public void registerFileSystem(FileSystem fs) {
		filesystems.put(fs.getDevice().getId(), fs);
	}

	/**
	 * Unregister a mounted filesystem
	 * @param fs
	 */
	public void unregisterFileSystem(FileSystem fs) {
		filesystems.remove(fs.getDevice().getId());
	}
	
	/**
	 * Gets the filesystem registered on the given device.
	 * @param device
	 * @return null if no filesystem was found.
	 */
	public FileSystem getFileSystem(Device device) {
		return (FileSystem)filesystems.get(device.getId());
	}

	/**
	 * Gets the filesystem registered on the given name.
	 * @param rootName
	 * @return null if no filesystem was found.
	 */
	public FileSystem getFileSystem(String rootName) {
		return (FileSystem)filesystems.get(rootName);
	}

	/**
	 * Gets all registered filesystems.
	 * All instances of the returned collection are instanceof FileSystem.
	 */
	public Collection fileSystems() {
		return Collections.unmodifiableCollection(filesystems.values());
	}
	
	public Set fileSystemRoots() {
		return filesystems.keySet();
	}

	/**
	 * Initialize this manager
	 */
	protected void initialize() {
	}
	
}
