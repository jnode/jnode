/*
 * $Id: header.txt 5714 2010-01-03 13:33:07Z lsantha $
 *
 * Copyright (C) 2003-2012 JNode.org
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

import java.io.RandomAccessFile;
import org.jnode.apps.vmware.disk.descriptor.Descriptor;

/**
 * Wrote from the 'Virtual Disk Format 1.0' specifications (from VMWare).
 * 
 * @author Fabien DUMINY (fduminy at jnode dot org)
 * 
 */
public class FileDescriptor {
    private final Descriptor descriptor;
    private final RandomAccessFile raf;
    private final ExtentFactory factory;

    /**
     * 
     * @param descriptor
     * @param raf
     * @param factory
     */
    public FileDescriptor(Descriptor descriptor, RandomAccessFile raf, ExtentFactory factory) {
        this.descriptor = descriptor;
        this.raf = raf;
        this.factory = factory;
    }

    /**
     * 
     * @return
     */
    public Descriptor getDescriptor() {
        return descriptor;
    }

    /**
     * 
     * @return
     */
    public RandomAccessFile getRandomAccessFile() {
        return raf;
    }

    /**
     * 
     * @return
     */
    public ExtentFactory getExtentFactory() {
        return factory;
    }
}
