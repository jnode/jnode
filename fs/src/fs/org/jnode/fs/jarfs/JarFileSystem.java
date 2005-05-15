/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.fs.jarfs;

import java.util.Map;
import java.util.jar.JarFile;

import org.jnode.driver.block.JarFileDevice;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FSFile;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.spi.AbstractFileSystem;

/**
 * 
 * @author Fabien DUMINY (fduminy at users.sourceforge.net)
 *
 */
public class JarFileSystem extends AbstractFileSystem {

    private JarFile jarFile;
    private JarFSCache cache;
    private JarFSEntry rootEntry;

    /**
     * @see org.jnode.fs.FileSystem#getDevice()
     */
    public JarFileSystem(JarFileDevice device)
            throws FileSystemException {
        super(device, true); // jar file systems are always readOnly
        
        jarFile = device.getJarFile();
        cache = new JarFSCache();
        rootEntry = FSTreeBuilder.build(this, jarFile, cache);
    }

    public JarFile getJarFile()
    {
        return jarFile;
    }
    
    /**
     * 
     */
	protected FSFile createFile(FSEntry entry) {
		return new JarFSFile((JarFSEntry) entry);
	}

	/**
	 * 
	 */
	protected FSDirectory createDirectory(FSEntry entry) {
        Map<String, JarFSEntry> entries = cache.getChildEntries((JarFSEntry) entry);
        return new JarFSDirectory((JarFSEntry) entry, entries);
	}
    
	/**
	 * 
	 */
	protected FSEntry createRootEntry() {        
		return rootEntry;
	}
}
