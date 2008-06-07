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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.fs.jarfs;

import gnu.java.nio.InputStreamChannel;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.jar.JarFile;

import org.jnode.fs.FSFile;
import org.jnode.fs.FileSystem;
import org.jnode.fs.ReadOnlyFileSystemException;

/** 
 * @author Fabien DUMINY (fduminy at users.sourceforge.net)
 */
public class JarFSFile implements FSFile {

    private final JarFSEntry entry;

    /**
     * @param entry
     */
    public JarFSFile(JarFSEntry entry) {
        this.entry = entry;
    }

    /**
     * @see org.jnode.fs.FSFile#getLength()
     */
    public long getLength() {
        return entry.getJarEntry().getSize();
    }

    /**
     * @see org.jnode.fs.FSFile#setLength(long)
     */
    public void setLength(long length) throws IOException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * @see org.jnode.fs.FSFile#read(long, byte[], int, int)
     */
    public void read(long fileOffset, ByteBuffer destBuf) throws IOException {
        final JarFileSystem fs = (JarFileSystem) getFileSystem();
        final JarFile jarFile = fs.getJarFile();
        final InputStream is = jarFile.getInputStream(entry.getJarEntry());
        is.skip(fileOffset);
        InputStreamChannel isc = new InputStreamChannel(is);
        isc.read(destBuf);
        isc.close();
        is.close();
    }

    /**
     * @see org.jnode.fs.FSFile#write(long, byte[], int, int)
     */ 
    public void write(long fileOffset, ByteBuffer src) throws IOException {
        throw new ReadOnlyFileSystemException("Not yet implemented");
    }

    /**
     * @see org.jnode.fs.FSFile#flush()
     */
    public void flush() throws IOException {
        // Readonly
        //throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * @see org.jnode.fs.FSObject#isValid()
     */
    public boolean isValid() {
        return true;
    }

    /**
     * @see org.jnode.fs.FSObject#getFileSystem()
     */
    public final FileSystem getFileSystem() {
        return entry.getFileSystem();
    }
}
