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
 
package org.jnode.apps.vmware.disk.test;

import java.io.File;
import java.io.IOException;
import org.jnode.apps.vmware.disk.tools.DiskCopier;
import org.junit.After;
import org.junit.Before;

/**
 * Wrote from the 'Virtual Disk Format 1.0' specifications (from VMWare)
 * 
 * @author Fabien DUMINY (fduminy at jnode dot org)
 * 
 */
public abstract class BaseTest {

    protected final File originalDiskFile;
    protected final boolean copyDisk;
    protected File diskFile;

    public BaseTest(File diskFile, boolean copyDisk) {
        this.originalDiskFile = diskFile;
        this.copyDisk = copyDisk;
    }

    @Before
    public void setUp() throws IOException {
        this.diskFile =
                copyDisk ? DiskCopier.copyDisk(originalDiskFile, Utils.createTempDir())
                        : originalDiskFile;
    }

    @After
    public void tearDown() throws IOException {
        Utils.clearTempDir(true);
        Utils.DO_CLEAR = true;
    }

    @Override
    public String toString() {
        return diskFile.getName();
    }

}
