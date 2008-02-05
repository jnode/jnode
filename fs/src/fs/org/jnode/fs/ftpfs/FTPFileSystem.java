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

package org.jnode.fs.ftpfs;

import org.jnode.fs.FileSystem;
import org.jnode.driver.DeviceListener;
import org.jnode.driver.Device;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.security.PrivilegedAction;
import java.security.AccessController;

import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.FTPFile;


/**
 * @author Levente S\u00e1ntha
 */
public class FTPFileSystem implements FileSystem<FTPFSDirectory> {
    private FTPFSDevice device;
    private FTPFSDirectory root;
    private boolean closed;
    private Thread thread;
    final private FTPClient client;


    public FTPFileSystem(final FTPFSDevice device) {
    	this.client = new FTPClient();
        this.device = device;
        device.addListener(new DeviceListener() {
            public void deviceStarted(Device device) {
                //empty
            }

            public void deviceStop(Device device) {
                try {
                    close();
                } catch(IOException x){
                    x.printStackTrace();
                }
            }
        });
        try{

        	client.setRemoteHost(device.getHost());
        	client.setTimeout(300000);
            AccessController.doPrivileged(
            new PrivilegedAction(){
                public Object run() {
                    try{
                        client.connect();
                        return null;
                    }catch(Exception e){
                        throw new RuntimeException(e);
                    }
                }
            });

            client.login(device.getUser(),device.getPassword());
            thread = new Thread(new Runnable(){
                public void run() {
                    try{
                        while(!isClosed()){
                            try {
                                Thread.sleep(100000);
                                nop();
                            }catch(InterruptedException x){
                                //ignore
                            }
                        }
                    } catch(Exception x){
                        x.printStackTrace();
                    }
                }
            },"ftpfs_keepalive");
            thread.start();
            FTPFile f = new FTPFile("/", "/", 0, true, new Date(0));
//            FTPFile f = new FTPFile();
//            f.setName(printWorkingDirectory());
            root = new FTPFSDirectory(this, f);
            closed = false;
        }catch(Exception e){
            throw new RuntimeException(e);
        }

    }

	final public FTPFileSystemType getType() {
		return FTPFileSystemType.getInstance();
	}

    private synchronized void nop() throws Exception{
    	client.dir(root.path());
    }


    /**
     * Close this filesystem. After a close, all invocations of method of this
     * filesystem or objects created by this filesystem will throw an
     * IOException.
     *
     * @throws java.io.IOException
     */
    public synchronized void close() throws IOException {
        try {
            closed = true;
            thread = null;
            client.quit();
        } catch(Exception e){
            throw new IOException("Close error");
        }
    }

    /**
     * Gets the device this FS driver operates on.
     */
    public FTPFSDevice getDevice() {
        return device;
    }

    /**
     * Gets the root entry of this filesystem. This is usually a directory, but
     * this is not required.
     */
    public FTPFSDirectory getRootEntry() throws IOException {
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
        return true;
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

	FTPFile[] dirDetails(String path) throws IOException, FTPException, ParseException {
		return client.dirDetails(path);
	}

	void chdir(String path) throws IOException, FTPException {
		client.chdir(path);
	}

	byte[] get(String name) throws IOException, FTPException {
		return client.get(name);
	}
}
