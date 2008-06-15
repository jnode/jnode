package org.jnode.apps.vmware.disk.tools;

import java.io.File;
import java.io.IOException;
import org.jnode.apps.vmware.disk.handler.sparse.SparseDiskFactory;

/**
 * Wrote from the 'Virtual Disk Format 1.0' specifications (from VMWare)
 * 
 * @author Fabien DUMINY (fduminy at jnode dot org)
 * 
 */
public abstract class DiskFactory {
    private static final SparseDiskFactory SPARSE_FACTORY = new SparseDiskFactory();

    public static File createSparseDisk(File directory, String name, long size) throws IOException {
        return SPARSE_FACTORY.createDisk(directory, name, size);
    }

    public File createDisk(File directory, String name, long size) throws IOException {
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException(directory.getAbsolutePath() + " is not a directory");
        }
        if (!directory.canWrite()) {
            throw new IllegalArgumentException(directory.getAbsolutePath() + " must be writable");
        }

        return createDiskImpl(directory, name, size);
    }

    protected abstract File createDiskImpl(File directory, String name, long size)
        throws IOException;
}
