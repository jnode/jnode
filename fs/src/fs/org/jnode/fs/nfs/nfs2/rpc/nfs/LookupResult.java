package org.jnode.fs.nfs.nfs2.rpc.nfs;

import java.io.IOException;

import org.acplt.oncrpc.OncRpcException;
import org.acplt.oncrpc.XdrDecodingStream;


public class LookupResult extends AbstractResult {

    private byte[] fileHandle;
    private FileAttribute fileAttribute;

    public void decode(XdrDecodingStream xdr) throws OncRpcException, IOException {
        fileHandle = xdr.xdrDecodeOpaque(NFS2Client.FILE_HANDLE_SIZE);
        fileAttribute = new FileAttribute(xdr);

    }

    public byte[] getFileHandle() {
        return fileHandle;
    }

    public FileAttribute getFileAttribute() {
        return fileAttribute;
    }


}