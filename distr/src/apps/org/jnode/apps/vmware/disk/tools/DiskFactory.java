/*
 * $Id$
 *
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
 
package org.jnode.apps.vmware.disk.tools;

import java.io.File;
import java.io.IOException;
import org.jnode.apps.vmware.disk.handler.sparse.SparseDiskFactory;

/**
 * Wrote from the 'Virtual Disk Format 1.0' specifications (from VMWare).
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
