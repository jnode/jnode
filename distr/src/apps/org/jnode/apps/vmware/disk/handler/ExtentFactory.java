/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
 *
 * JNode.org
 * Copyright (C) 2003-2009 JNode.org
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
