/*
 * $Id$
 */
package org.jnode.fs.service;

import java.io.*;
import java.util.Collection;

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
	public static final Class NAME = FileSystemService.class; //"system/FileSystemService";

	/**
	 * Gets all registered file system types. All instances of the returned
	 * collection are instanceof FileSystemType.
	 */
	public Collection fileSystemTypes();

	/**
	 * Gets registered file system types with the gicen name.
	 * 
	 * @param name the name of the FSType you want
	 * @return the fileSystemType
	 */
	public FileSystemType getFileSystemTypeForNameSystemTypes(String name) throws FileSystemException;

	/**
	 * Register a mounted filesystem
	 * 
	 * @param fs
	 */
	public void registerFileSystem(FileSystem fs) throws FileSystemException;

	/**
	 * Unregister a mounted filesystem
	 * 
	 * @param device
	 * @return The filesystem that was registered for the device, or null if not found.
	 */
	public FileSystem unregisterFileSystem(Device device);

	/**
	 * Gets the filesystem registered on the given device.
	 * 
	 * @param device
	 * @return null if no filesystem was found.
	 */
	public FileSystem getFileSystem(Device device);

	/**
	 * Gets all registered filesystems. All instances of the returned collection
	 * are instanceof FileSystem.
	 */
	public Collection fileSystems();

	/**
	 * Gets the filesystem API.
	 */
	public VMFileSystemAPI getApi();
}
