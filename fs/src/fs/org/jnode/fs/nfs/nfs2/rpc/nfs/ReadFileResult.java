package org.jnode.fs.nfs.nfs2.rpc.nfs;


public class ReadFileResult  {


    private FileAttribute fileAttribute;
    private byte[] data;

    
    public void setFileAttribute(FileAttribute fileAttribute) {
        this.fileAttribute = fileAttribute;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public FileAttribute getFileAttribute() {
        return fileAttribute;
    }

    public byte[] getData() {
        return data;
    }


}
