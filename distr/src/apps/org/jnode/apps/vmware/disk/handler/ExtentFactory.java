package org.jnode.apps.vmware.disk.handler;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import org.apache.log4j.Logger;
import org.jnode.apps.vmware.disk.descriptor.DescriptorRW;
import org.jnode.apps.vmware.disk.handler.sparse.SparseExtentFactory;

/**
 * Wrote from the 'Virtual Disk Format 1.0' specifications (from VMWare)
 * 
 * @author Fabien DUMINY (fduminy at jnode dot org)
 * 
 */
public abstract class ExtentFactory {
    private static final Logger LOG = Logger.getLogger(SparseExtentFactory.class);

    public FileDescriptor createFileDescriptor(File file)
        throws IOException, UnsupportedFormatException {
        RandomAccessFile raf = null;
        FileDescriptor fileDescriptor = null;

        try {
            raf = new RandomAccessFile(file, "rw");

            fileDescriptor = createFileDescriptor(file, raf);
        } finally {
            if ((fileDescriptor == null) && (raf != null)) {
                // if there was an error but RandomAccessFile has
                // been created, then close it
                raf.close();
            }
        }

        return fileDescriptor;

    }

    protected abstract DescriptorRW getDescriptorRW();

    protected abstract FileDescriptor createFileDescriptor(File file, RandomAccessFile raf)
        throws IOException, UnsupportedFormatException;

    public abstract IOHandler createIOHandler(FileDescriptor fileDescriptor)
        throws IOException, UnsupportedFormatException;
}
