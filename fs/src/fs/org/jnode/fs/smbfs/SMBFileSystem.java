package org.jnode.fs.smbfs;

import org.jnode.fs.FileSystem;

import java.io.IOException;

import jcifs.smb.NtlmAuthenticator;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;

/**
 * @author Levente S\u00e1ntha
 */
public class SMBFileSystem extends NtlmAuthenticator implements FileSystem {
    private SMBFSDevice device;
    private SMBFSDirectory root;
    private boolean closed;

    public SMBFileSystem(SMBFSDevice device) {
        this.device = device;
        try {
            root = new SMBFSDirectory(null,
                    new SmbFile("smb://" + device.getUser() + ":" + device.getPassword() + "@" + device.getHost() + "/" + device.getPath() + "/"));
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    protected NtlmPasswordAuthentication getNtlmPasswordAuthentication() {
        return new NtlmPasswordAuthentication( "", device.getUser(), device.getPassword() );
    }

    /**
     * Close this filesystem. After a close, all invocations of method of this
     * filesystem or objects created by this filesystem will throw an
     * IOException.
     *
     * @throws java.io.IOException
     */
    public void close() throws IOException {
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
        System.out.println("get root");
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
        return true;
    }
}
