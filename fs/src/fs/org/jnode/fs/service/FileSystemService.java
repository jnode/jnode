/*
 * $Id$
 */
package org.jnode.fs.service;

import java.io.*;
import java.util.Collection;

import org.jnode.driver.Device;
import org.jnode.fs.FileSystem;

/**
 * @author epr
 */
public interface FileSystemService {

	/**
	 * Name used to lookup a FileSystemTypeManager in the initial namespace.
	 */
	public static final String NAME = "system/FileSystemService";
	
	/**
	 * Gets all registered file system types.
	 * All instances of the returned collection are instanceof FileSystemType.
	 */
	public Collection fileSystemTypes();
	
	/**
	 * Register a mounted filesystem
	 * @param fs
	 */
	public void registerFileSystem(FileSystem fs);

	/**
	 * Unregister a mounted filesystem
	 * @param fs
	 */
	public void unregisterFileSystem(FileSystem fs);
	
	/**
	 * Gets the filesystem registered on the given device.
	 * @param device
	 * @return null if no filesystem was found.
	 */
	public FileSystem getFileSystem(Device device);

	/**
	 * Gets all registered filesystems.
	 * All instances of the returned collection are instanceof FileSystem.
	 */
	public Collection fileSystems();
	
	/**
	 * Gets the filesystem API.
	 */
	public VMFileSystemAPI getApi();
}
