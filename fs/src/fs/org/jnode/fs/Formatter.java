package org.jnode.fs;

import org.jnode.driver.Device;

/**
 *
 * @author Fabien DUMINY (fduminy at jnode.org)
 *
 * @param <T>
 */
public interface Formatter<T extends FileSystem> {
    /**
     * Format the given device
     *
     * @param device The device we want to format
     * @return the newly created FileSystem
     * @throws FileSystemException
     */
	public T format(Device device) throws FileSystemException;
}
