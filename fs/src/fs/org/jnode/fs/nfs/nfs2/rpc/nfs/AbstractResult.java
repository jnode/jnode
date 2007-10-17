package org.jnode.fs.nfs.nfs2.rpc.nfs;

import java.io.IOException;

import org.acplt.oncrpc.OncRpcException;
import org.acplt.oncrpc.XdrAble;
import org.acplt.oncrpc.XdrDecodingStream;
import org.acplt.oncrpc.XdrEncodingStream;

public abstract class AbstractResult implements XdrAble {

    private int status;

    public void xdrEncode(XdrEncodingStream xdr) throws OncRpcException, IOException {
        // TODO Auto-generated method stub

    }

    public void xdrDecode(XdrDecodingStream xdr) throws OncRpcException, IOException {
        status = xdr.xdrDecodeInt();
        switch (status) {
            case Status.NFS_OK:
                decode(xdr);
                break;
            default:
                break;
        }

    }

    public int getStatus() {
        return status;
    }

    public abstract void decode(XdrDecodingStream xdr) throws OncRpcException, IOException;
}