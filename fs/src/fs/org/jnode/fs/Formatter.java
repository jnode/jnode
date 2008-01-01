package org.jnode.fs;

import org.jnode.driver.Device;
import org.jnode.fs.ext2.Ext2FileSystemType;

/**
 *
 * @author Fabien DUMINY (fduminy at jnode.org)
 *
 * @param <T>
 */
abstract public class Formatter<T extends FileSystem> {
	private final FileSystemType<T> type;

	protected Formatter(FileSystemType<T> type)
	{
		this.type = type;
	}

    /**
     * Format the given device
     *
     * @param device The device we want to format
     * @return the newly created FileSystem
     * @throws FileSystemException
     */
	abstract public T format(Device device) throws FileSystemException;

	final public FileSystemType<T> getFileSystemType()
	{
		return type;
	}
}
