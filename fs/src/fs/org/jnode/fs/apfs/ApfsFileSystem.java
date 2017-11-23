package org.jnode.fs.apfs;

import java.io.IOException;
import org.jnode.driver.Device;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FSFile;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.spi.AbstractFileSystem;

/**
 * @author Luke Quinane
 */
public final class ApfsFileSystem extends AbstractFileSystem<FSEntry> {

    public ApfsFileSystem(Device device, boolean readOnly, ApfsFileSystemType type) throws FileSystemException {
        super(device, readOnly, type);
    }

    @Override
    public FSEntry createRootEntry() throws IOException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public long getTotalSpace() throws IOException {
        return -1;
    }

    @Override
    public long getFreeSpace() throws IOException {
        return -1;
    }

    @Override
    public long getUsableSpace() throws IOException {
        return -1;
    }

    @Override
    public String getVolumeName() throws IOException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void flush() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected FSFile createFile(FSEntry entry) throws IOException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    protected FSDirectory createDirectory(FSEntry entry) throws IOException {
        throw new UnsupportedOperationException("Not implemented");
    }
}
