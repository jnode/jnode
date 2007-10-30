package org.jnode.fs.nfs.nfs2.rpc.mount;

public class MountResult {

    private byte[] fileHandle;

    public byte[] getFileHandle() {
        return fileHandle;
    }

    public void setFileHandle(byte[] fileHandle) {
        this.fileHandle = fileHandle;
    }

}
