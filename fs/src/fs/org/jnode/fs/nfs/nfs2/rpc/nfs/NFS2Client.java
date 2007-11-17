package org.jnode.fs.nfs.nfs2.rpc.nfs;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.acplt.oncrpc.OncRpcClient;
import org.acplt.oncrpc.OncRpcClientAuthUnix;
import org.acplt.oncrpc.OncRpcException;
import org.acplt.oncrpc.OncRpcProtocols;
import org.acplt.oncrpc.XdrAble;
import org.acplt.oncrpc.XdrDecodingStream;
import org.acplt.oncrpc.XdrEncodingStream;
import org.acplt.oncrpc.XdrVoid;
import org.apache.log4j.Logger;
import org.jnode.fs.nfs.nfs2.rpc.mount.Mount1Client;
import org.jnode.fs.nfs.nfs2.rpc.mount.MountException;

/**
 * @author Andrei Dore
 */
public class NFS2Client {

    /**
     * The maximum number of bytes of data in a READ or WRITE request.
     */
    public static final int MAX_DATA = 8192;

    public static final int FILE_HANDLE_SIZE = 32;

    public static final int MAX_NAME_LENGTH = 255;

    public static final int MAX_PATH_LENGTH = 1024;

    public static final int COOKIE_SIZE = 4;

    private static final int NFS_VERSION = 2;

    private static final int NFS_PROGRAM = 100003;

    private static final int PROCEDURE_TEST = 0;

    private static final int PROCEDURE_GET_ATTRIBUTE = 1;

    private static final int PROCEDURE_LOOKUP = 4;

    private static final int PROCEDURE_READ_FILE = 6;

    private static final int PROCEDURE_WRITE_FILE = 8;

    private static final int PROCEDURE_CREATE_FILE = 9;

    private static final int PROCEDURE_REMOVE_FILE = 10;

    private static final int PROCEDURE_RENAME_FILE = 11;

    private static final int PROCEDURE_CREATE_DIRECTORY = 14;

    private static final int PROCEDURE_REMOVE_DIRECTORY = 15;

    private static final int PROCEDURE_LIST_DIRECTORY = 16;

    private static final Logger LOGGER = Logger.getLogger(NFS2Client.class);

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
     * 
     */
    public NFS2Client(InetAddress host, int protocol, int uid, int gid) {
        this.host = host;
        this.protocol = protocol;

        this.uid = uid;
        this.gid = gid;

        rpcClientPool = new LinkedList<OncRpcClient>();

    }

    private OncRpcClient createRpcClient() throws OncRpcException, IOException {

        OncRpcClient client = OncRpcClient.newOncRpcClient(host, NFS_PROGRAM, NFS_VERSION, protocol);
        client.setTimeout(10000);

        if (uid != -1 && gid != -1) {
            client.setAuth(new OncRpcClientAuthUnix("test", uid, gid));
        }

        return client;
    }

    private synchronized OncRpcClient getRpcClient() throws OncRpcException, IOException {

        if (rpcClientPool.size() == 0) {
            // TODO Improve this lock
            return createRpcClient();
        } else {
            return rpcClientPool.remove(0);

        }

    }

    private synchronized void releaseRpcClient(OncRpcClient client) {

        if (client != null) {
            rpcClientPool.add(client);
        }

    }

    private void call(final int functionId, final XdrAble parameter, final XdrAble result) throws NFS2Exception,
            IOException {

        OncRpcClient client = null;

        int countCall = 0;

        while (true) {
            try {

                countCall++;

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

            } catch (Exception e) {

                // if we receive a timeout we will close the client and next
                // time we will use another rpc client
                if (client != null) {
                    try {
                        client.close();
                    } catch (OncRpcException e1) {
                        // Ignore this
                    }
                    client = null;
                }

                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                }

                if (e instanceof IOException || e instanceof OncRpcException) {

                    if (countCall > 10) {
                        throw new NFS2Exception(e.getMessage(), e);
                    } else {
                        LOGGER
                                .warn("An error occurs when nfs file system try to call the rpc method. It will try again");
                        continue;
                    }

                } else {
                    throw new NFS2Exception(e.getMessage(), e);
                }
            } finally {

                if (client != null) {
                    releaseRpcClient(client);
                }

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
                FileAttribute fileAttribute = new FileAttribute();
                xdrFileAttributeDecode(xdr, fileAttribute);
                result.setFileAttribute(fileAttribute);

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
                FileAttribute fileAttribute = new FileAttribute();
                xdrFileAttributeDecode(xdr, fileAttribute);
                result.setFileAttribute(fileAttribute);
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

        if (name.length() > MAX_NAME_LENGTH) {
            throw new NFS2Exception("The name is too long.The maximun length is " + MAX_NAME_LENGTH);
        }

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
                FileAttribute fileAttribute = new FileAttribute();
                xdrFileAttributeDecode(xdr, fileAttribute);
                result.setFileAttribute(fileAttribute);
            }

        };

        call(PROCEDURE_CREATE_DIRECTORY, nfsParameter, nfsResult);

        return result;

    }

    public CreateFileResult createFile(final byte[] parentFileHandle, final String name, final boolean[] permission,
            final int uid, final int gid, final int size, final Time lastAccessed, final Time lastModified)
            throws NFS2Exception, IOException {

        if (name.length() > MAX_NAME_LENGTH) {
            throw new NFS2Exception("The name is too long.The maximun length is " + MAX_NAME_LENGTH);
        }

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
                FileAttribute fileAttribute = new FileAttribute();
                xdrFileAttributeDecode(xdr, fileAttribute);
                result.setFileAttribute(fileAttribute);
            }

        };

        call(PROCEDURE_CREATE_FILE, nfsParameter, nfsResult);

        return result;

    }

    public FileAttribute writeFile(final byte[] fileHandle, final int offset, final byte[] buffer)
            throws NFS2Exception, IOException {

        return writeFile(fileHandle, offset, buffer, 0, buffer.length);
    }

    public FileAttribute writeFile(final byte[] fileHandle, final int offset, final byte[] buffer,
            final int bufferIndex, final int bufferCount) throws NFS2Exception, IOException {

        NFSParameter nfsParameter = new NFSParameter() {

            public void xdrEncode(XdrEncodingStream xdr) throws OncRpcException, IOException {
                xdr.xdrEncodeOpaque(fileHandle, FILE_HANDLE_SIZE);
                xdr.xdrEncodeInt(0);
                xdr.xdrEncodeInt(offset);
                xdr.xdrEncodeInt(0);

                // encode an array of byte
                xdr.xdrEncodeInt(bufferCount);
                xdr.xdrEncodeOpaque(buffer, bufferIndex, bufferCount);

            }

        };

        final FileAttribute fileAttribute = new FileAttribute();

        XdrAble nfsResult = new NFSResult() {

            public void xdrDecode(XdrDecodingStream xdr) throws OncRpcException, IOException {
                xdrFileAttributeDecode(xdr, fileAttribute);

            }

        };

        call(PROCEDURE_WRITE_FILE, nfsParameter, nfsResult);

        return fileAttribute;

    }

    public FileAttribute getAttribute(final byte[] fileHandle) throws NFS2Exception, IOException {

        XdrAble nfsParameter = new NFSParameter() {

            public void xdrEncode(XdrEncodingStream xdr) throws OncRpcException, IOException {
                xdr.xdrEncodeOpaque(fileHandle, FILE_HANDLE_SIZE);

            }

        };

        final FileAttribute fileAttribute = new FileAttribute();

        XdrAble nfsResult = new NFSResult() {

            public void xdrDecode(XdrDecodingStream xdr) throws OncRpcException, IOException {

                xdrFileAttributeDecode(xdr, fileAttribute);

            }

        };

        call(PROCEDURE_GET_ATTRIBUTE, nfsParameter, nfsResult);

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

    private void xdrDecodeTime(XdrDecodingStream xdrDecodingStream, Time time) throws OncRpcException, IOException {

        time.setSeconds(xdrDecodingStream.xdrDecodeInt());
        time.setMicroSeconds(xdrDecodingStream.xdrDecodeInt());

    }

    public void xdrFileAttributeEncode(XdrEncodingStream xdr, FileAttribute fileAttribute) throws OncRpcException,
            IOException {
        xdr.xdrEncodeInt(fileAttribute.getType());
        xdr.xdrEncodeInt(fileAttribute.getMode());
        xdr.xdrEncodeInt(fileAttribute.getNlink());
        xdr.xdrEncodeInt(fileAttribute.getUid());
        xdr.xdrEncodeInt(fileAttribute.getGid());
        xdr.xdrEncodeInt(fileAttribute.getSize());
        xdr.xdrEncodeInt(fileAttribute.getBlocksize());
        xdr.xdrEncodeInt(fileAttribute.getRdev());
        xdr.xdrEncodeInt(fileAttribute.getBlocks());
        xdr.xdrEncodeInt(fileAttribute.getFsid());
        xdr.xdrEncodeInt(fileAttribute.getFileid());
        xdrEncodeTime(xdr, fileAttribute.getLastAccessed());
        xdrEncodeTime(xdr, fileAttribute.getLastModified());
        xdrEncodeTime(xdr, fileAttribute.getLastStatusChanged());

    }

    public void xdrFileAttributeDecode(XdrDecodingStream xdr, FileAttribute fileAttribute) throws OncRpcException,
            IOException {
        fileAttribute.setType(xdr.xdrDecodeInt());
        fileAttribute.setMode(xdr.xdrDecodeInt());
        fileAttribute.setNlink(xdr.xdrDecodeInt());
        fileAttribute.setUid(xdr.xdrDecodeInt());
        fileAttribute.setGid(xdr.xdrDecodeInt());
        fileAttribute.setSize(xdr.xdrDecodeInt());
        fileAttribute.setBlocksize(xdr.xdrDecodeInt());
        fileAttribute.setRdev(xdr.xdrDecodeInt());
        fileAttribute.setBlocks(xdr.xdrDecodeInt());
        fileAttribute.setFsid(xdr.xdrDecodeInt());
        fileAttribute.setFileid(xdr.xdrDecodeInt());
        Time lastAccessedTime = new Time();
        xdrDecodeTime(xdr, lastAccessedTime);
        fileAttribute.setLastAccessed(lastAccessedTime);
        Time lastModifiedTime = new Time();
        xdrDecodeTime(xdr, lastModifiedTime);
        fileAttribute.setLastModified(lastModifiedTime);
        Time lastStatusChangedTime = new Time();
        xdrDecodeTime(xdr, lastStatusChangedTime);
        fileAttribute.setLastStatusChanged(lastStatusChangedTime);

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
