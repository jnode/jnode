package org.jnode.fs.nfs.nfs2.rpc.nfs;

public class CreateDirectoryResult {

    private byte[] fileHandle;

    private FileAttribute fileAttribute;

    public byte[] getFileHandle() {
        return fileHandle;
    }

    public void setFileHandle(byte[] fileHandle) {
        this.fileHandle = fileHandle;
    }

    public FileAttribute getFileAttribute() {
        return fileAttribute;
    }

    public void setFileAttribute(FileAttribute fileAttribute) {
        this.fileAttribute = fileAttribute;
    }

}
