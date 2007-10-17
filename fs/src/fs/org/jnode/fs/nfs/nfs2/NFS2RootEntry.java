package org.jnode.fs.nfs.nfs2;

import java.io.IOException;

import org.jnode.fs.FSAccessRights;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FSFile;

public class NFS2RootEntry extends NFS2Object implements FSEntry {

    private byte[] fileHandle;

    private NFS2Directory directory;

    NFS2RootEntry(NFS2FileSystem fileSystem, byte[] fileHandle) {

        super(fileSystem);

        this.fileHandle = fileHandle;

        directory = new NFS2Directory(fileSystem, fileHandle);

    }

    public FSAccessRights getAccessRights() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    public FSDirectory getDirectory() throws IOException {
        return directory;
    }

    public FSFile getFile() throws IOException {
        throw new IOException("It is not  a file. It is the root of the file system.");
    }

    public long getLastModified() throws IOException {
        return 0;
    }

    public String getName() {
        return "/";
    }

    public FSDirectory getParent() {
        return null;
    }

    public boolean isDirectory() {
        return true;
    }

    public boolean isDirty() throws IOException {
        return false;
    }

    public boolean isFile() {
        return false;
    }

    public void setLastModified(long lastModified) throws IOException {
        throw new IOException("Cannot change last modified of root directory");

    }

    public void setName(String newName) throws IOException {
        throw new IOException("Cannot change name of root directory");

    }


}
