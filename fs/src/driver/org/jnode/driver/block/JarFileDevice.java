/*
 * $Id$
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
 
package org.jnode.driver.block;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.jar.JarFile;
import org.jnode.fs.ReadOnlyFileSystemException;

/**
 * This class is a device that wraps a JarFile
 *
 * @author epr
 */
public class JarFileDevice extends FileDevice implements FSBlockDeviceAPI {

    private JarFile jarFile;


    /**
     * Create a new JarFileDevice
     *
     * @param file
     * @param mode
     * @throws FileNotFoundException
     * @throws IOException
     */
    public JarFileDevice(File file, String mode) throws FileNotFoundException, IOException {
        super(file, mode);
        jarFile = new JarFile(file);
    }

    /**
     * Returns the JarFile wraped by this device
     *
     * @return the wraped JarFile
     */
    public JarFile getJarFile() {
        return jarFile;
    }

    /**
     * This method allways throws an Exception, since the JarFileSystem is not writable.
     *
     * @see org.jnode.driver.block.FileDevice#write(long, java.nio.ByteBuffer)
     */
    public void write(long devOffset, ByteBuffer srcBuf) throws IOException {
        throw new ReadOnlyFileSystemException("jar file systems are not writeable");
    }

    /**
     * This method allways throws an Exception, since the JarFileSystem is not writable.
     *
     * @see org.jnode.driver.block.FileDevice#setLength(long)
     */
    public void setLength(long length) throws IOException {
        throw new ReadOnlyFileSystemException("jar file systems are not writeable");
    }
}
