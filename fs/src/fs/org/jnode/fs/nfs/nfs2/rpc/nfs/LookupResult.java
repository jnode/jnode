package org.jnode.fs.nfs.nfs2.rpc.nfs;

public class LookupResult {

    private byte[] fileHandle;
    private FileAttribute fileAttribute;

    public byte[] getFileHandle() {
        return fileHandle;
    }

    public FileAttribute getFileAttribute() {
        return fileAttribute;
    }

    public void setFileHandle(byte[] fileHandle) {
        this.fileHandle = fileHandle;
    }

    public void setFileAttribute(FileAttribute fileAttribute) {
        this.fileAttribute = fileAttribute;
    }

}