package org.jnode.fs.nfs.nfs2;

import java.io.IOException;

import org.jnode.fs.FSEntry;
import org.jnode.fs.FSFile;
import org.jnode.fs.nfs.nfs2.rpc.nfs.FileAttribute;

public class NFS2RootEntry extends NFS2Entry implements FSEntry {

    NFS2RootEntry(NFS2FileSystem fileSystem, byte[] fileHandle,
            FileAttribute fileAttribute) {
        super(fileSystem, null, "/", fileHandle, fileAttribute);

    }

    public FSFile getFile() throws IOException {
        throw new IOException(
                "It is not  a file. It is the root of the file system.");
    }

    public void setLastModified(long lastModified) throws IOException {
        throw new IOException("Cannot change last modified of root directory");

    }

    public void setName(String newName) throws IOException {
        throw new IOException("Cannot change name of root directory");

    }

}