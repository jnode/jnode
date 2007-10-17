package org.jnode.fs.nfs.nfs2;

import org.jnode.fs.FSObject;
import org.jnode.fs.FileSystem;
import org.jnode.fs.nfs.nfs2.rpc.nfs.NFS2Client;

public class NFS2Object implements FSObject {

    private NFS2FileSystem fileSystem;

    NFS2Object(NFS2FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    public FileSystem getFileSystem() {
        return fileSystem;
    }

    public boolean isValid() {
        // TODO Auto-generated method stub
        return false;
    }

    public NFS2Client getNFSClient() {
        return fileSystem.getNFSClient();
    }

}
