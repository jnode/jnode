/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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
 * You should have received a copy of the GNU General Public License
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
 * <description>
 * 
 * @author epr
 */
public class JarFileDevice extends FileDevice implements FSBlockDeviceAPI {
    private JarFile jarFile;
    
	public JarFileDevice(File file, String mode) throws FileNotFoundException, IOException {
		super(file, mode);
        jarFile = new JarFile(file);
    }
    
    public JarFile getJarFile()
    {
        return jarFile; 
    }
    
	/**
	 * @param devOffset
	 * @param src
	 * @param srcOffset
	 * @param length
	 * @see org.jnode.driver.block.BlockDeviceAPI#write(long, byte[], int, int)
	 * @throws IOException
	 */
	public void write(long devOffset, ByteBuffer srcBuf) throws IOException {
        throw new ReadOnlyFileSystemException("jar file systems are not writeable");
	}

	public void setLength(long length) throws IOException {
        throw new ReadOnlyFileSystemException("jar file systems are not writeable");
	}
}
