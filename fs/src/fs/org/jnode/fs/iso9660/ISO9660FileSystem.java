/*
 * $Id$
 */
package org.jnode.fs.iso9660;

import java.io.IOException;

import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.spi.AbstractFileSystem;

/**
 * @author Chira
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class ISO9660FileSystem extends AbstractFileSystem {

    private final ISO9660Volume volume;

    /**
     * @see org.jnode.fs.FileSystem#getDevice()
     */
    public ISO9660FileSystem(Device device, boolean readOnly)
            throws FileSystemException {
        super(device, readOnly);

        try {
            volume = new ISO9660Volume(getFSApi());
        } catch (IOException e) {
            throw new FileSystemException(e);
        } catch (ApiNotFoundException ex) {
            throw new FileSystemException("Need FSBlockDeviceAPI for ISO9660 filesystem");
        }
    }

    /**
     * @see org.jnode.fs.FileSystem#getRootEntry()
     */
    public FSEntry getRootEntry() throws IOException {
        return new ISO9660Entry(this, volume.getPrimaryVolumeDescriptor()
                .getRootDirectoryEntry());
    }

    /**
     * @return Returns the volume.
     */
    public ISO9660Volume getVolume() {
        return this.volume;
    }

    /**
     * @see org.jnode.fs.spi.AbstractFileSystem#flush()
     */
    public void flush() throws IOException {
        if (isReadOnly()) {
            // Do nothing, since readonly
        } else {
            // TODO not implemented yet
        }
    }
}