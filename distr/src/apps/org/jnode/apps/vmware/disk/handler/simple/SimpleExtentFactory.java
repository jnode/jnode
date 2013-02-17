/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.apps.vmware.disk.handler.simple;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import org.jnode.apps.vmware.disk.descriptor.Descriptor;
import org.jnode.apps.vmware.disk.handler.ExtentFactory;
import org.jnode.apps.vmware.disk.handler.FileDescriptor;
import org.jnode.apps.vmware.disk.handler.UnsupportedFormatException;

/**
 * Wrote from the 'Virtual Disk Format 1.0' specifications (from VMWare).
 * 
 * @author Fabien DUMINY (fduminy at jnode dot org)
 * 
 */
public class SimpleExtentFactory extends ExtentFactory {
    protected FileDescriptor createFileDescriptor(File file, RandomAccessFile raf)
        throws IOException, UnsupportedFormatException {
        // read 2 sectors, starting from sector number 0
        Descriptor descriptor = getDescriptorRW().read(file, 0, 2);

        return new FileDescriptor(descriptor, raf, this);
    }

    /**
     * 
     */
    public SimpleIOHandler createIOHandler(FileDescriptor fileDescriptor)
        throws IOException, UnsupportedFormatException {
        return new SimpleIOHandler(fileDescriptor);
    }

    /**
     * 
     */
    @Override
    protected SimpleDescriptorRW getDescriptorRW() {
        return new SimpleDescriptorRW();
    }
}
