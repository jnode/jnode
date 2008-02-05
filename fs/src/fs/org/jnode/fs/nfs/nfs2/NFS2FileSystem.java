/*
 * $Id $
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

package org.jnode.fs.nfs.nfs2;

import java.io.IOException;

import org.jnode.driver.Device;
import org.jnode.driver.DeviceListener;
import org.jnode.fs.FileSystem;
import org.jnode.fs.FileSystemException;
import org.jnode.net.nfs.nfs2.FileAttribute;
import org.jnode.net.nfs.nfs2.FileSystemAttribute;
import org.jnode.net.nfs.nfs2.NFS2Client;
import org.jnode.net.nfs.nfs2.NFS2Exception;
import org.jnode.net.nfs.nfs2.mount.Mount1Client;
import org.jnode.net.nfs.nfs2.mount.MountException;
import org.jnode.net.nfs.nfs2.mount.MountResult;

/**
 * @author Andrei Dore
 */
public class NFS2FileSystem implements FileSystem<NFS2RootEntry> {

    private NFS2Device device;

    private NFS2RootEntry root;

    private Mount1Client mountClient;

    private NFS2Client nfsClient;

    private boolean closed;

    private boolean readOnly;

    public NFS2FileSystem(final NFS2Device device, boolean readOnly)
            throws FileSystemException {
        this.device = device;
        this.readOnly = readOnly;

        device.addListener(new DeviceListener() {
            public void deviceStarted(Device device) {
            }

            public void deviceStop(Device device) {
                try {
                    close();
                } catch (IOException x) {
                    // Ignore this
                }
            }
        });

        mountClient = new Mount1Client(device.getHost(), device.getProtocol(),
                device.getUid(), device.getGid());

        nfsClient = new NFS2Client(device.getHost(), device.getProtocol(),
                device.getUid(), device.getGid());

        MountResult result;

        FileAttribute fileAttribute;

        // Mount the file system
        try {
            result = mountClient.mount(device.getRemoteDirectory());

            fileAttribute = nfsClient.getAttribute(result.getFileHandle());

        } catch (IOException e) {

            try {
                close();
            } catch (IOException e1) {
            }

            throw new FileSystemException(e.getMessage(), e);
        } catch (MountException e) {

            try {
                close();
            } catch (IOException e1) {
            }

            throw new FileSystemException(e.getMessage(), e);
        } catch (NFS2Exception e) {

            try {
                close();
            } catch (IOException e1) {
            }

            throw new FileSystemException(e.getMessage(), e);
        }

        root = new NFS2RootEntry(this, result.getFileHandle(), fileAttribute);

    }

	final public NFS2FileSystemType getType() {
		return NFS2FileSystemType.getInstance();
	}

    /**
     * Close this filesystem. After a close, all invocations of method of this
     * filesystem or objects created by this filesystem will throw an
     * IOException.
     *
     * @throws java.io.IOException
     */
    public void close() throws IOException {

        if (mountClient != null) {
            try {
                mountClient.unmount(device.getRemoteDirectory());
            } catch (MountException e) {

            }

            try {
                mountClient.close();
            } catch (IOException e) {

            }
        }

        if (nfsClient != null) {

            try {
                nfsClient.close();
            } catch (IOException e) {

            }

        }

        closed = true;

    }

    /**
     * Gets the device this FS driver operates on.
     */
    public NFS2Device getDevice() {
        return device;
    }

    /**
     * Gets the root entry of this filesystem. This is usually a directory, but
     * this is not required.
     */
    public NFS2RootEntry getRootEntry() throws IOException {
        return root;
    }

    /**
     * Is this filesystem closed.
     */
    public boolean isClosed() {
        return closed;
    }

    /**
     * Is the filesystem mounted in readonly mode ?
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    public long getFreeSpace() {

        FileSystemAttribute fileSystemAttribute = getFileSystemAttribute();
        return fileSystemAttribute.getBlockSize()
                * fileSystemAttribute.getFreeBlockCount();
    }

    public long getTotalSpace() {
        FileSystemAttribute fileSystemAttribute = getFileSystemAttribute();

        return fileSystemAttribute.getBlockSize()
                * fileSystemAttribute.getBlockCount();
    }

    public long getUsableSpace() {
        FileSystemAttribute fileSystemAttribute = getFileSystemAttribute();
        return fileSystemAttribute.getBlockSize()
                * fileSystemAttribute.getFreeBlockCount();
    }

    private FileSystemAttribute getFileSystemAttribute() {
        try {
            return nfsClient.getFileSystemAttribute(root.getFileHandle());
        } catch (NFS2Exception e) {
            // throw new IOException(e);
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    NFS2Client getNFSClient() {
        return nfsClient;
    }

}
