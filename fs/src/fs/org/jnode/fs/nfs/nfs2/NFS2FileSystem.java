/*
 * $Id: FTPFileSystem.java 3337 2007-06-30 11:58:44Z fduminy $
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
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.acplt.oncrpc.OncRpcClientAuthUnix;
import org.acplt.oncrpc.OncRpcException;
import org.acplt.oncrpc.OncRpcProtocols;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceListener;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FileSystem;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.nfs.nfs2.rpc.mount.DirPath;
import org.jnode.fs.nfs.nfs2.rpc.mount.Mount1Client;
import org.jnode.fs.nfs.nfs2.rpc.mount.MountResult;
import org.jnode.fs.nfs.nfs2.rpc.nfs.NFS2Client;

/**
 * @author Andrei Dore
 */
public class NFS2FileSystem implements FileSystem {

    private NFS2Device device;

    private NFS2RootEntry root;

    private Mount1Client mountClient;
    private NFS2Client nfsClient;

    private boolean closed;

    private boolean readOnly;

    public NFS2FileSystem(final NFS2Device device, boolean readOnly) throws FileSystemException {
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

        // Mount the file system

        try {


            mountClient = new Mount1Client(InetAddress.getByName(device.getHost()), OncRpcProtocols.ONCRPC_UDP);

            nfsClient = new NFS2Client(InetAddress.getByName(device.getHost()), OncRpcProtocols.ONCRPC_UDP);

            if (!readOnly) {

                mountClient.getClient().setAuth(new OncRpcClientAuthUnix("test", device.getUid(), device.getGid()));
                nfsClient.getClient().setAuth(new OncRpcClientAuthUnix("test", device.getUid(), device.getGid()));

            }


            MountResult result = mountClient.mount(new DirPath(device.getRemoteDirectory()));

            //
            if (result.getStatus() == Mount1Client.MOUNT_OK) {

                root = new NFS2RootEntry(this, result.getDirectory().getValue());

            } else {
                throw new IOException("The status of the call it is not ok ");
            }

        } catch (Exception e) {

            // Not good . Improve exception handling . Give more detail .
            throw new FileSystemException(e.getMessage(), e);
        }

    }

    /**
     * Close this filesystem. After a close, all invocations of method of this
     * filesystem or objects created by this filesystem will throw an
     * IOException.
     *
     * @throws java.io.IOException
     */
    public void close() throws IOException {

        // Improve the exception !!!!!!!!!!!!!!!
        Exception unmountException = null;
        try {
            mountClient.unmount(new DirPath(device.getRemoteDirectory()));
        } catch (OncRpcException ex) {
            unmountException = ex;
        }

        Exception nfsException = null;
        try {
            nfsClient.close();
        } catch (OncRpcException ex) {
            nfsException = ex;
        }

        Exception mountException = null;
        try {
            mountClient.close();
        } catch (OncRpcException ex) {
            mountException = ex;
        }

        if (unmountException != null || nfsException != null || mountException != null) {
            throw new IOException("Can not close the nfs file system ");
        }

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
    public FSEntry getRootEntry() throws IOException {
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
        return readOnly;
    }

    public long getFreeSpace() {
        // TODO implement me
        return 0;
    }

    public long getTotalSpace() {
        // TODO implement me
        return 0;
    }

    public long getUsableSpace() {
        // TODO implement me
        return 0;
    }

    NFS2Client getNFSClient() {
        return nfsClient;
    }

}
