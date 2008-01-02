package org.jnode.net.nfs.nfs2;

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
