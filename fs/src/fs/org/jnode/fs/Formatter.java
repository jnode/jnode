package org.jnode.fs;

import org.jnode.driver.Device;

/**
 * 
 * @author Fabien DUMINY (fduminy at jnode.org)
 * 
 * @param <T>
 */
public abstract class Formatter<T extends FileSystem<?>> implements Cloneable {
    private final FileSystemType<T> type;

    protected Formatter(FileSystemType<T> type) {
        this.type = type;
    }

    /**
     * Format the given device
     * 
     * @param device The device we want to format
     * @return the newly created FileSystem
     * @throws FileSystemException
     */
    public abstract T format(Device device) throws FileSystemException;

    public final FileSystemType<T> getFileSystemType() {
        return type;
    }

    @SuppressWarnings("unchecked")
    public Formatter<T> clone() throws CloneNotSupportedException {
        return (Formatter<T>) super.clone();
    }
}
