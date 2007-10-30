package org.jnode.fs.nfs.nfs2.rpc.mount;

import java.io.IOException;
import java.net.InetAddress;

import org.acplt.oncrpc.OncRpcClient;
import org.acplt.oncrpc.OncRpcClientAuthUnix;
import org.acplt.oncrpc.OncRpcException;
import org.acplt.oncrpc.XdrAble;
import org.acplt.oncrpc.XdrDecodingStream;
import org.acplt.oncrpc.XdrEncodingStream;
import org.acplt.oncrpc.XdrVoid;

/**
 *
 */
public class Mount1Client {

    private static final int MOUNT_CODE = 100005;
    private static final int MOUNT_VERSION = 1;

    private static final int PROCEDURE_TEST = 0;
    private static final int PROCEDURE_MOUNT = 1;
    private static final int PROCEDURE_UNMOUNT = 3;

    public static final int FILE_HANDLE_SIZE = 32;
    public static final int MAX_PATH_LENGHT = 1024;
    public static final int MAX_NAME_LENGHT = 255;

    public static final int MOUNT_OK = 0;
    private InetAddress host;
    private int protocol;
    private OncRpcClient client;

    /**
     * Constructs a <code>Mount1Client</code> client stub proxy object from
     * which the MOUNTPROG remote program can be accessed.
     *
     * @param host     Internet address of host where to contact the remote
     *                 program.
     * @param protocol {@link org.acplt.oncrpc.OncRpcProtocols Protocol} to be
     *                 used for ONC/RPC calls.
     */
    public Mount1Client(InetAddress host, int protocol, int uid, int gid) throws OncRpcException, IOException {
        this.host = host;
        this.protocol = protocol;

        client = OncRpcClient.newOncRpcClient(host, MOUNT_CODE, MOUNT_VERSION, protocol);
        if (uid != -1 && gid != -1) {
            client.setAuth(new OncRpcClientAuthUnix("test", uid, gid));
        }

    }

    /**
     * Call remote procedure test.
     *
     * @throws OncRpcException if an ONC/RPC error occurs.
     * @throws IOException     if an I/O error occurs.
     * @throws MountException
     */
    public void test() throws IOException, MountException {
        call(PROCEDURE_TEST, XdrVoid.XDR_VOID, XdrVoid.XDR_VOID);
    }

    /**
     * Call remote procedure mount.
     *
     * @param dirPath parameter (of type DirPath) to the remote procedure call.
     * @return Result from remote procedure call (of type MountResult).
     * @throws OncRpcException if an ONC/RPC error occurs.
     * @throws IOException     if an I/O error occurs.
     * @throws MountException
     */
    public MountResult mount(final String path) throws IOException, MountException {

        XdrAble mountParameter = new Parameter() {

            public void xdrEncode(XdrEncodingStream xdrEncodingStream) throws OncRpcException, IOException {
                xdrEncodingStream.xdrEncodeString(path);
            }

        };

        final MountResult result = new MountResult();

        XdrAble mountResult = new Result() {

            public void xdrDecode(XdrDecodingStream xdrDecodingStream) throws OncRpcException, IOException {
                result.setFileHandle(xdrDecodingStream.xdrDecodeOpaque(Mount1Client.FILE_HANDLE_SIZE));

            }
        };

        call(PROCEDURE_MOUNT, mountParameter, mountResult);

        return result;
    }

    public void unmount(final String dirPath) throws IOException, MountException {

        XdrAble mountParameter = new Parameter() {

            public void xdrEncode(XdrEncodingStream xdrEncodingStream) throws OncRpcException, IOException {
                xdrEncodingStream.xdrEncodeString(dirPath);
            }

        };

        call(PROCEDURE_UNMOUNT, mountParameter, XdrVoid.XDR_VOID);

    }

    private void call(final int functionId, final XdrAble parameter, final XdrAble result) throws MountException {

        int countCall = 0;

        while (countCall < 10) {
            try {

                if (result == XdrVoid.XDR_VOID) {

                    client.call(functionId, parameter, result);

                } else {

                    ResultWithCode mountResult = new ResultWithCode(result);

                    client.call(functionId, parameter, mountResult);

                    if (mountResult.getResultCode() != 0) {
                        throw new MountException("Test");
                    }

                }

                break;

            } catch (OncRpcException e) {

                if (e.getReason() == OncRpcException.RPC_TIMEDOUT) {
                    countCall++;
                    continue;
                }

                throw new MountException("Test");
            } finally {

            }
        }

    }

    private abstract class Parameter implements XdrAble {

        public void xdrDecode(XdrDecodingStream arg0) throws OncRpcException, IOException {

        }

    }

    private abstract class Result implements XdrAble {

        public void xdrEncode(XdrEncodingStream arg0) throws OncRpcException, IOException {

        }

    }

    private class ResultWithCode implements XdrAble {

        private int resultCode;
        private XdrAble xdrAble;

        public ResultWithCode(XdrAble xdrAble) {
            this.xdrAble = xdrAble;
        }

        public void xdrEncode(XdrEncodingStream xdr) throws OncRpcException, IOException {
        }

        public void xdrDecode(XdrDecodingStream xdr) throws OncRpcException, IOException {
            resultCode = xdr.xdrDecodeInt();
            if (resultCode == 0) {
                xdrAble.xdrDecode(xdr);
            }

        }

        public int getResultCode() {
            return resultCode;
        }

    }

    public void close() throws MountException {
        try {
            client.close();
        } catch (OncRpcException e) {
            throw new MountException(e.getMessage(), e);
        }

    }

}
