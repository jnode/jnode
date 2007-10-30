package org.jnode.fs.nfs.nfs2.rpc.nfs;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.acplt.oncrpc.OncRpcClient;
import org.acplt.oncrpc.OncRpcClientAuthUnix;
import org.acplt.oncrpc.OncRpcException;
import org.acplt.oncrpc.XdrAble;
import org.acplt.oncrpc.XdrDecodingStream;
import org.acplt.oncrpc.XdrEncodingStream;
import org.acplt.oncrpc.XdrVoid;

/**
 * @author Andrei Dore
 */
public class NFS2Client {

    public static final int FILE_HANDLE_SIZE = 32;
    public static final int MAX_NAME_LENGTH = 255;
    public static final int MAX_PATH_LENGTH = 1024;
    public static final int COOKIE_SIZE = 4;
    public static final int MAX_DATA = 8192;

    private static final int NFS_VERSION = 2;
    private static final int NFS_PROGRAM = 100003;

    private static final int PROCEDURE_TEST = 0;
    private static final int PROCEDURE_LOOKUP = 4;
    private static final int PROCEDURE_READ_FILE = 6;
    private static final int PROCEDURE_WRITE_FILE = 8;
    private static final int PROCEDURE_CREATE_FILE = 9;
    private static final int PROCEDURE_REMOVE_FILE = 10;
    private static final int PROCEDURE_RENAME_FILE = 11;
    private static final int PROCEDURE_CREATE_DIRECTORY = 14;
    private static final int PROCEDURE_REMOVE_DIRECTORY = 15;
    private static final int PROCEDURE_LIST_DIRECTORY = 16;

    private List<OncRpcClient> rpcClientPool;
    private InetAddress host;
    private int protocol;

    private final Object rpcCLientLock = new Object();
    private int uid;
    private int gid;

    /**
     * Constructs a <code>NFS2Client</code> client stub proxy object from
     * which the NFS_PROGRAM remote program can be accessed.
     *
     * @param host     Internet address of host where to contact the remote
     *                 program.
     * @param protocol {@link org.acplt.oncrpc.OncRpcProtocols Protocol} to be
     *                 used for ONC/RPC calls.
     * @throws NFS2Exception
     */
    public NFS2Client(InetAddress host, int protocol, int uid, int gid) throws NFS2Exception {
        this.host = host;
        this.protocol = protocol;

        this.uid = uid;
        this.gid = gid;

        rpcClientPool = new LinkedList<OncRpcClient>();
        try {
            rpcClientPool.add(createRpcClient());
        } catch (OncRpcException e) {
            throw new NFS2Exception(e.getMessage(), e);
        } catch (IOException e) {
            throw new NFS2Exception(e.getMessage(), e);
        }

    }

    private OncRpcClient createRpcClient() throws OncRpcException, IOException {

        OncRpcClient client = OncRpcClient.newOncRpcClient(host, NFS_PROGRAM, NFS_VERSION, protocol);
        if (uid != -1 && gid != -1) {
            client.setAuth(new OncRpcClientAuthUnix("test", uid, gid));
        }

        return client;
    }

    private OncRpcClient getRpcClient() throws OncRpcException, IOException {

        synchronized (rpcCLientLock) {

            if (rpcClientPool.size() == 0) {
                return createRpcClient();
            } else {
                return rpcClientPool.remove(0);

            }
        }

    }

    private void releaseRpcClient(OncRpcClient client) {

        synchronized (rpcCLientLock) {

            if (client != null) {
                rpcClientPool.add(client);
            }

        }

    }

    private void call(final int functionId, final XdrAble parameter, final XdrAble result) throws NFS2Exception,
            IOException {

        OncRpcClient client = null;

        int countCall = 0;

        while (countCall < 10) {
            try {
                client = getRpcClient();

                if (result == XdrVoid.XDR_VOID) {

                    client.call(functionId, parameter, result);

                } else {

                    ResultWithCode nfsResult = new ResultWithCode(result);

                    client.call(functionId, parameter, nfsResult);

                    if (nfsResult.getResultCode() != ResultCode.NFS_OK) {
                        throw new NFS2Exception(nfsResult.getResultCode());
                    }

                }

                break;

            } catch (OncRpcException e) {

                if (e.getReason() == OncRpcException.RPC_TIMEDOUT) {
                    countCall++;
                    continue;
                }

                throw new NFS2Exception(e.getMessage(), e);
            } catch (IOException e) {

                throw e;

            } finally {
                releaseRpcClient(client);
            }
        }

    }

    public void close() {

        for (int i = 0; i < rpcClientPool.size(); i++) {

            OncRpcClient client = rpcClientPool.get(i);
            try {
                client.close();
            } catch (OncRpcException e) {

            }

        }
    }

    /**
     * Call remote procedure test.
     *
     * @throws NFS2Exception
     * @throws IOException
     */
    public void test() throws NFS2Exception, IOException {

        call(PROCEDURE_TEST, XdrVoid.XDR_VOID, XdrVoid.XDR_VOID);
    }

    public LookupResult lookup(final byte[] fileHandle, final String entryName) throws NFS2Exception, IOException {

        XdrAble nfsParameter = new NFSParameter() {

            public void xdrEncode(XdrEncodingStream xdr) throws OncRpcException, IOException {
                xdr.xdrEncodeOpaque(fileHandle, NFS2Client.FILE_HANDLE_SIZE);
                xdr.xdrEncodeString(entryName);

            }

        };

        final LookupResult result = new LookupResult();

        XdrAble nfsResult = new NFSResult() {

            public void xdrDecode(XdrDecodingStream xdr) throws OncRpcException, IOException {
                result.setFileHandle(xdr.xdrDecodeOpaque(NFS2Client.FILE_HANDLE_SIZE));
                result.setFileAttribute(new FileAttribute(xdr));

            }

        };

        call(PROCEDURE_LOOKUP, nfsParameter, nfsResult);
        return result;
    }

    public ListDirectoryResult listDirectory(final byte[] fileHandle, final byte[] cookie, final int count)
            throws NFS2Exception, IOException {

        XdrAble nfsParameter = new NFSParameter() {

            public void xdrEncode(XdrEncodingStream xdr) throws OncRpcException, IOException {
                xdr.xdrEncodeOpaque(fileHandle, FILE_HANDLE_SIZE);
                xdr.xdrEncodeOpaque(cookie, COOKIE_SIZE);
                xdr.xdrEncodeInt(count);

            }

        };

        final ListDirectoryResult result = new ListDirectoryResult();

        XdrAble nfsResult = new NFSResult() {

            public void xdrDecode(XdrDecodingStream xdr) throws OncRpcException, IOException {

                List<Entry> entryList = new ArrayList<Entry>();

                while (xdr.xdrDecodeBoolean()) {

                    int fileId = xdr.xdrDecodeInt();
                    String name = xdr.xdrDecodeString();
                    byte[] cookie = xdr.xdrDecodeOpaque(COOKIE_SIZE);

                    Entry entry = new Entry(fileId, name, cookie);

                    entryList.add(entry);

                }

                result.setEntryList(entryList);
                result.setEof(xdr.xdrDecodeBoolean());

            }

        };

        call(PROCEDURE_LIST_DIRECTORY, nfsParameter, nfsResult);
        return result;
    }

    public ReadFileResult readFile(final byte[] fileHandle, final int offset, final int count) throws NFS2Exception,
            IOException {

        XdrAble nfsParameter = new NFSParameter() {

            public void xdrEncode(XdrEncodingStream xdr) throws OncRpcException, IOException {
                xdr.xdrEncodeOpaque(fileHandle, FILE_HANDLE_SIZE);
                xdr.xdrEncodeInt(offset);
                xdr.xdrEncodeInt(count);
                xdr.xdrEncodeInt(0);

            }

        };

        final ReadFileResult result = new ReadFileResult();

        XdrAble nfsResult = new NFSResult() {

            public void xdrDecode(XdrDecodingStream xdr) throws OncRpcException, IOException {
                result.setFileAttribute(new FileAttribute(xdr));
                // Optimize this
                result.setData(xdr.xdrDecodeDynamicOpaque());

            }

        };

        call(PROCEDURE_READ_FILE, nfsParameter, nfsResult);
        return result;
    }

    public void removeDirectory(final byte[] fileHandle, final String name) throws NFS2Exception, IOException {

        NFSParameter nfsParameter = new NFSParameter() {

            public void xdrEncode(XdrEncodingStream xdr) throws OncRpcException, IOException {
                xdr.xdrEncodeOpaque(fileHandle, FILE_HANDLE_SIZE);
                xdr.xdrEncodeString(name);
            }

        };

        XdrAble nfsResult = new NFSResult() {

            public void xdrDecode(XdrDecodingStream xdr) throws OncRpcException, IOException {
            }

        };

        call(PROCEDURE_REMOVE_DIRECTORY, nfsParameter, nfsResult);

    }

    public void removeFile(final byte[] parentFileHandle, final String name) throws NFS2Exception, IOException {

        NFSParameter nfsParameter = new NFSParameter() {

            public void xdrEncode(XdrEncodingStream xdr) throws OncRpcException, IOException {
                xdr.xdrEncodeOpaque(parentFileHandle, FILE_HANDLE_SIZE);
                xdr.xdrEncodeString(name);
            }

        };

        XdrAble nfsResult = new NFSResult() {

            public void xdrDecode(XdrDecodingStream xdr) throws OncRpcException, IOException {
            }

        };

        call(PROCEDURE_REMOVE_FILE, nfsParameter, nfsResult);

    }

    public void renameFile(final byte[] fromParentFileHandle, final String fromName, final byte[] toParentFileHandle,
                           final String toName) throws NFS2Exception, IOException {

        NFSParameter nfsParameter = new NFSParameter() {

            public void xdrEncode(XdrEncodingStream xdr) throws OncRpcException, IOException {
                xdr.xdrEncodeOpaque(fromParentFileHandle, FILE_HANDLE_SIZE);
                xdr.xdrEncodeString(fromName);
                xdr.xdrEncodeOpaque(toParentFileHandle, FILE_HANDLE_SIZE);
                xdr.xdrEncodeString(toName);
            }

        };

        XdrAble nfsResult = new NFSResult() {

            public void xdrDecode(XdrDecodingStream xdr) throws OncRpcException, IOException {
            }

        };

        call(PROCEDURE_RENAME_FILE, nfsParameter, nfsResult);

    }

    public CreateDirectoryResult createDirectory(final byte[] parentFileHandle, final String name,
                                                 final boolean[] permission, final int uid, final int gid, final int size, final Time lastAccessed,
                                                 final Time lastModified) throws NFS2Exception, IOException {

        final int mode = createMode(permission) | 0x4000;

        NFSParameter nfsParameter = new NFSParameter() {

            public void xdrEncode(XdrEncodingStream xdr) throws OncRpcException, IOException {
                xdr.xdrEncodeOpaque(parentFileHandle, FILE_HANDLE_SIZE);
                xdr.xdrEncodeString(name);

                xdr.xdrEncodeInt(mode);
                xdr.xdrEncodeInt(uid);
                xdr.xdrEncodeInt(gid);
                xdr.xdrEncodeInt(size);

                xdrEncodeTime(xdr, lastAccessed);
                xdrEncodeTime(xdr, lastModified);

            }

        };

        final CreateDirectoryResult result = new CreateDirectoryResult();

        XdrAble nfsResult = new NFSResult() {

            public void xdrDecode(XdrDecodingStream xdr) throws OncRpcException, IOException {
                result.setFileHandle(xdr.xdrDecodeOpaque(NFS2Client.FILE_HANDLE_SIZE));
                result.setFileAttribute(new FileAttribute(xdr));
            }

        };

        call(PROCEDURE_CREATE_DIRECTORY, nfsParameter, nfsResult);

        return result;

    }

    public CreateFileResult createFile(final byte[] parentFileHandle, final String name, final boolean[] permission,
                                       final int uid, final int gid, final int size, final Time lastAccessed, final Time lastModified)
            throws NFS2Exception, IOException {

        final int mode = createMode(permission) | 0x8000;

        NFSParameter nfsParameter = new NFSParameter() {

            public void xdrEncode(XdrEncodingStream xdr) throws OncRpcException, IOException {
                xdr.xdrEncodeOpaque(parentFileHandle, FILE_HANDLE_SIZE);
                xdr.xdrEncodeString(name);

                xdr.xdrEncodeInt(mode);
                xdr.xdrEncodeInt(uid);
                xdr.xdrEncodeInt(gid);
                xdr.xdrEncodeInt(size);

                xdrEncodeTime(xdr, lastAccessed);
                xdrEncodeTime(xdr, lastModified);

            }

        };

        final CreateFileResult result = new CreateFileResult();

        XdrAble nfsResult = new NFSResult() {

            public void xdrDecode(XdrDecodingStream xdr) throws OncRpcException, IOException {
                result.setFileHandle(xdr.xdrDecodeOpaque(NFS2Client.FILE_HANDLE_SIZE));
                result.setFileAttribute(new FileAttribute(xdr));
            }

        };

        call(PROCEDURE_CREATE_FILE, nfsParameter, nfsResult);

        return result;

    }

    public FileAttribute writeFile(final byte[] fileHandle, final int offset, final int count, final byte[] data)
            throws NFS2Exception, IOException {

        NFSParameter nfsParameter = new NFSParameter() {

            public void xdrEncode(XdrEncodingStream xdr) throws OncRpcException, IOException {
                xdr.xdrEncodeOpaque(fileHandle, FILE_HANDLE_SIZE);
                xdr.xdrEncodeInt(0);
                xdr.xdrEncodeInt(offset);
                xdr.xdrEncodeInt(0);
                xdr.xdrEncodeDynamicOpaque(data);

            }

        };

        final FileAttribute fileAttribute = new FileAttribute();

        XdrAble nfsResult = new NFSResult() {

            public void xdrDecode(XdrDecodingStream xdr) throws OncRpcException, IOException {
                fileAttribute.xdrDecode(xdr);
            }

        };

        call(PROCEDURE_WRITE_FILE, nfsParameter, nfsResult);

        return fileAttribute;

    }

    private int createMode(boolean[] data) {

        int mode = 0;

        // owner
        if (data[0]) {
            mode |= 0x100;
        }
        if (data[1]) {
            mode |= 0x80;
        }
        if (data[2]) {
            mode |= 0x40;
        }

        // group
        if (data[3]) {
            mode |= 0x20;
        }
        if (data[4]) {
            mode |= 0x10;
        }
        if (data[5]) {
            mode |= 0x8;
        }

        // other
        if (data[6]) {
            mode |= 0x4;
        }
        if (data[7]) {
            mode |= 0x2;
        }
        if (data[8]) {
            mode |= 0x1;
        }

        return mode;

    }

    private void xdrEncodeTime(XdrEncodingStream xdrEncodingStream, Time time) throws OncRpcException, IOException {
        xdrEncodingStream.xdrEncodeInt(time.getSeconds());
        xdrEncodingStream.xdrEncodeInt(time.getMicroSeconds());
    }

    private abstract class NFSParameter implements XdrAble {

        public void xdrDecode(XdrDecodingStream arg0) throws OncRpcException, IOException {

        }

    }

    private abstract class NFSResult implements XdrAble {

        public void xdrEncode(XdrEncodingStream arg0) throws OncRpcException, IOException {

        }

    }

    private class ResultWithCode implements XdrAble {

        private ResultCode resultCode;
        private XdrAble xdrAble;

        public ResultWithCode(XdrAble xdrAble) {
            this.xdrAble = xdrAble;
        }

        public void xdrEncode(XdrEncodingStream xdr) throws OncRpcException, IOException {
        }

        public void xdrDecode(XdrDecodingStream xdr) throws OncRpcException, IOException {
            resultCode = ResultCode.getResultCode(xdr.xdrDecodeInt());
            if (resultCode == ResultCode.NFS_OK) {
                xdrAble.xdrDecode(xdr);
            }

        }

        public ResultCode getResultCode() {
            return resultCode;
        }

    }

}
