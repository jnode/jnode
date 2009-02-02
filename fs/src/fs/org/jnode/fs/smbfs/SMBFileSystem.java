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
 
package org.jnode.fs.smbfs;

import java.io.IOException;
import jcifs.smb.NtlmAuthenticator;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;
import org.jnode.fs.FileSystem;

/**
 * @author Levente S\u00e1ntha
 */
public class SMBFileSystem extends NtlmAuthenticator implements FileSystem<SMBFSEntry> {
    private SMBFSDevice device;
    private SMBFSDirectory root;
    private boolean closed;
    private SMBFileSystemType type;

    public SMBFileSystem(SMBFSDevice device, SMBFileSystemType type) {
        this.type = type;
        this.device = device;
        try {
            root = new SMBFSDirectory(null, new SmbFile("smb://" + device.getUser() + ":" + device.getPassword() +
                "@" + device.getHost() + "/" + device.getPath() + "/"));
            root.smbFile.setDefaultUseCaches(false);
            root.smbFile.connect();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public final SMBFileSystemType getType() {
        return type;
    }

    protected NtlmPasswordAuthentication getNtlmPasswordAuthentication() {
        return new NtlmPasswordAuthentication("", device.getUser(), device.getPassword());
    }

    /**
     * Close this filesystem. After a close, all invocations of method of this
     * filesystem or objects created by this filesystem will throw an
     * IOException.
     *
     * @throws java.io.IOException
     */
    public synchronized void close() throws IOException {
        closed = true;
    }

    /**
     * Gets the device this FS driver operates on.
     */
    public SMBFSDevice getDevice() {
        return device;
    }

    /**
     * Gets the root entry of this filesystem. This is usually a directory, but
     * this is not required.
     */
    public SMBFSEntry getRootEntry() throws IOException {
        return root;
    }

    /**
     * Is this filesystem closed.
     */
    public synchronized boolean isClosed() {
        return closed;
    }

    /**
     * Is the filesystem mounted in readonly mode ?
     */
    public boolean isReadOnly() {
        return false;
    }

    public long getFreeSpace() throws IOException {
        return root.smbFile.getDiskFreeSpace();
    }

    public long getTotalSpace() throws IOException {
        //todo fix it
        return -1;
    }

    public long getUsableSpace() throws IOException {
        return root.smbFile.getDiskFreeSpace();
    }
}
