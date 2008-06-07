package org.jnode.fs.ramfs;

import org.jnode.driver.Device;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.FileSystemType;

/**
 * FileSystemType for RAMFS, a filesystem implementation in system RAM
 * 
 * @author peda
 */
public class RAMFileSystemType implements FileSystemType<RAMFileSystem> {
    public static final Class<RAMFileSystemType> ID = RAMFileSystemType.class;
    private static final int DEFAULT_SIZE = 104857600;

    /** Virtual Device name for this filesystem */
    public static final String VIRTUAL_DEVICE_NAME = "ramfsdevice";

    /**
     * (non-Javadoc)
     * 
     * @see org.jnode.fs.FileSystemType#getName()
     */
    public String getName() {
        return "RAMFS";
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.jnode.fs.FileSystemType#create(org.jnode.driver.Device, boolean)
     */
    public RAMFileSystem create(Device device, boolean readOnly) throws FileSystemException {
        return new RAMFileSystem(device, readOnly, DEFAULT_SIZE, this);
    }
}
