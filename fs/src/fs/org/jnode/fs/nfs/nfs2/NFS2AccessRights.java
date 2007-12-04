package org.jnode.fs.nfs.nfs2;

import java.io.IOException;
import java.security.Principal;

import org.jnode.fs.FSAccessRights;

public class NFS2AccessRights extends NFS2Object implements FSAccessRights {

    private NFS2Entry entry;

    public NFS2AccessRights(NFS2FileSystem fileSystem, NFS2Entry entry) {
        super(fileSystem);
        this.entry = entry;
    }

    public boolean canExecute() {

        return false;

    }

    public boolean canRead() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean canWrite() {
        // TODO Auto-generated method stub
        return false;
    }

    public Principal getOwner() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean setExecutable(boolean enable, boolean owneronly) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean setReadable(boolean enable, boolean owneronly) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean setWritable(boolean enable, boolean owneronly) {
        // TODO Auto-generated method stub
        return false;
    }

}
