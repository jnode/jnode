/*
 * $Id$
 */
package org.jnode.fs.spi;

import java.io.IOException;

import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.driver.block.FSBlockDeviceAPI;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FileSystem;
import org.jnode.fs.FileSystemException;

/**
 * @author Fabien DUMINY
 * 
 * Abstract class with common things in different FileSystem implementations
 */
public abstract class AbstractFileSystem implements FileSystem {

    private final boolean readOnly;

    private final Device device;

    private final BlockDeviceAPI api;

    private final FSBlockDeviceAPI fsApi;

    private boolean closed;

    /**
     * Construct an AbstractFileSystem in specified readOnly mode
     */
    public AbstractFileSystem(Device device, boolean readOnly)
            throws FileSystemException {
        if (device == null) throw new IllegalArgumentException("null device!");

        this.device = device;

        try {
            api = (BlockDeviceAPI) device.getAPI(BlockDeviceAPI.class);
        } catch (ApiNotFoundException e) {
            throw new FileSystemException("Device is not a partition!", e);
        }
        FSBlockDeviceAPI fsApi = null;
        try {
            fsApi = (FSBlockDeviceAPI) device.getAPI(FSBlockDeviceAPI.class);
        } catch (ApiNotFoundException e) {
            // Ignore
        }
        this.fsApi = fsApi;
        this.closed = false;
        this.readOnly = readOnly;
    }

    /**
     * @see org.jnode.fs.FileSystem#getDevice()
     */
    final public Device getDevice() {
        return device;
    }

    /**
     * @see org.jnode.fs.FileSystem#getRootEntry()
     */
    public abstract FSEntry getRootEntry() throws IOException;

    /**
     * @see org.jnode.fs.FileSystem#close()
     */
    final public void close() throws IOException {
        closed = true;

        // if readOnly, nothing to do
        if (!isReadOnly()) {
            flush();
        }
    }

    public abstract void flush() throws IOException;

    /**
     * @return Returns the api.
     */
    public final BlockDeviceAPI getApi() {
        return api;
    }

    /**
     * @return Returns the api.
     */
    public final FSBlockDeviceAPI getFSApi() throws ApiNotFoundException {
        if (fsApi == null) {
            throw new ApiNotFoundException(FSBlockDeviceAPI.class.getName());
        } else {
            return fsApi;
        }
    }

    /**
     * @return Returns the closed.
     */
    final public boolean isClosed() {
        return closed;
    }

    /**
     * @return Returns the readOnly.
     */
    final public boolean isReadOnly() {
        return readOnly;
    }

}