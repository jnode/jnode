package org.jnode.fs.nfs.nfs2.rpc.nfs;

import java.io.IOException;

import org.acplt.oncrpc.OncRpcException;
import org.acplt.oncrpc.XdrDecodingStream;

public class ReadFileResult extends AbstractResult {


    private FileAttribute fileAttribute;
    private byte[] data;

    public ReadFileResult() {
    }

    public void decode(XdrDecodingStream xdr) throws OncRpcException, IOException {
        fileAttribute = new FileAttribute(xdr);
        // Optimize this
        data = xdr.xdrDecodeDynamicOpaque();
    }

    public FileAttribute getFileAttribute() {
        return fileAttribute;
    }

    public byte[] getData() {
        return data;
    }


}
