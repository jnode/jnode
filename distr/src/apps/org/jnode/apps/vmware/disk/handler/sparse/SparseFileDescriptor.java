package org.jnode.apps.vmware.disk.handler.sparse;

import java.io.RandomAccessFile;
import org.jnode.apps.vmware.disk.descriptor.Descriptor;
import org.jnode.apps.vmware.disk.handler.ExtentFactory;
import org.jnode.apps.vmware.disk.handler.FileDescriptor;

/**
 * Wrote from the 'Virtual Disk Format 1.0' specifications (from VMWare)
 * 
 * @author Fabien DUMINY (fduminy at jnode dot org)
 * 
 */
public class SparseFileDescriptor extends FileDescriptor {
    private final SparseExtentHeader header;

    public SparseFileDescriptor(Descriptor descriptor, RandomAccessFile raf, ExtentFactory factory,
            SparseExtentHeader header) {
        super(descriptor, raf, factory);

        this.header = header;
    }

    public SparseExtentHeader getHeader() {
        return header;
    }

}
