/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.net.nfs.nfs2;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.acplt.oncrpc.OncRpcClient;
import org.acplt.oncrpc.OncRpcClientAuthUnix;
import org.acplt.oncrpc.OncRpcException;
import org.acplt.oncrpc.OncRpcPortmapClient;
import org.acplt.oncrpc.OncRpcProtocols;
import org.acplt.oncrpc.OncRpcTcpClient;
import org.acplt.oncrpc.OncRpcUdpClient;
import org.acplt.oncrpc.XdrAble;
import org.acplt.oncrpc.XdrDecodingStream;
import org.acplt.oncrpc.XdrEncodingStream;
import org.acplt.oncrpc.XdrVoid;
import org.apache.log4j.Logger;
import org.jnode.net.nfs.Protocol;

/**
 * This class access a NFS2 server . It implements all the method from NFS2 specification
 *  
 *  http://tools.ietf.org/html/rfc1094
 * 
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

    /**
     * This constant it is use when we create a rpc client . The creation of the
     * rpc client need a buffer to store parameters or results . To create this
     * buffer we must know the max length of the buffer. The NFS2 specification
     * specify that for read write operation the maximun number of bytes it is
     * 8192.At this number we must add the length of the header(24), the lenght
     * of the auth part (400) and the length of the parameters in write
     * operation (16 +FILE_HANDLE_SIZE) . We chose to add the length of the
     * parameter in write operation because only in this case we risk a buffer
     * overflow.
     */
    private static final int HEADER_DATA = 440 + FILE_HANDLE_SIZE;

    private static final int NFS_VERSION = 2;

    private static final int NFS_PROGRAM = 100003;

    private static final int PROCEDURE_TEST = 0;

    private static final int PROCEDURE_GET_ATTRIBUTE = 1;

    private static final int PROCEDURE_SET_ATTRIBUTE = 2;

    private static final int PROCEDURE_LOOKUP = 4;

    private static final int PROCEDURE_READ_FILE = 6;

    private static final int PROCEDURE_WRITE_FILE = 8;

    private static final int PROCEDURE_CREATE_FILE = 9;

    private static final int PROCEDURE_REMOVE_FILE = 10;

    private static final int PROCEDURE_RENAME_FILE = 11;

    private static final int PROCEDURE_CREATE_DIRECTORY = 14;

    private static final int PROCEDURE_REMOVE_DIRECTORY = 15;

    private static final int PROCEDURE_LIST_DIRECTORY = 16;

    private static final int PROCEDURE_GET_FILE_SYSTEM_ATTRIBUTE = 17;

    private static final Logger LOGGER = Logger.getLogger(NFS2Client.class);

    private List<OncRpcClient> rpcClientPool;

    private InetAddress host;

    private Protocol protocol;

    private int uid;

    private int gid;

    private boolean closed;

    /**
     * Constructs a <code>NFS2Client</code> client stub proxy object from
     * which the NFS_PROGRAM remote program can be accessed.
     */
    public NFS2Client(InetAddress host, Protocol protocol, int uid, int gid) {
        this.host = host;
        this.protocol = protocol;
        this.uid = uid;
        this.gid = gid;
        rpcClientPool = new LinkedList<OncRpcClient>();
    }

    private OncRpcClient createRpcClient() throws OncRpcException, IOException {
        // invoke portmap
        OncRpcPortmapClient portmap = new OncRpcPortmapClient(host);
        int port;
        try {
            port = portmap.getPort(NFS_PROGRAM, NFS_VERSION,
                            protocol == Protocol.UDP ? OncRpcProtocols.ONCRPC_UDP
                                    : OncRpcProtocols.ONCRPC_UDP);
        } finally {
            portmap.close();
        }

        // create the client
        // We create the client with a buffer with lenght equals witn MAX_DATA +
        // 424 ( max header length)
        OncRpcClient client = null;
        if (protocol == Protocol.UDP) {
            client = new OncRpcUdpClient(host, NFS_PROGRAM, NFS_VERSION, port, MAX_DATA + HEADER_DATA);
        } else if (protocol == Protocol.TCP) {
            client = new OncRpcTcpClient(host, NFS_PROGRAM, NFS_VERSION, port, MAX_DATA + HEADER_DATA);
        } else {
            // TODO Do something
        }
        client.setTimeout(10000);
        if (uid != -1 && gid != -1) {
            client.setAuth(new OncRpcClientAuthUnix("test", uid, gid));
        }
        return client;
    }

    // TODO This lock it is not good because we wait an IO operation before we
    // free the lock . So the creation of the nfsclient must be outside of the
    // lock
    private synchronized OncRpcClient getRpcClient() throws OncRpcException, IOException {
        if (closed) {
            throw new IOException("The nfs client it is closed");
        }
        if (rpcClientPool.size() == 0) {
            // TODO Improve this lock
            return createRpcClient();
        } else {
            return rpcClientPool.remove(0);
        }
    }

    private synchronized void releaseRpcClient(OncRpcClient client) throws IOException {
        if (closed) {
            throw new IOException("The nfs client it is closed");
        }
        if (client != null) {
            rpcClientPool.add(client);
        }
    }

    private void call(final int functionId, final XdrAble parameter, final XdrAble result)
        throws NFS2Exception, IOException {
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
                if (e instanceof OncRpcException) {
                    if (countCall > 5) {
                        throw new NFS2Exception(e.getMessage(), e);
                    } else {
                        LOGGER.warn("An error occurs when nfs file system try to call the rpc method. Reason: " +
                                        e.getMessage() + " . It will try again");
                        continue;
                    }
                } else {
                    throw new NFS2Exception(e.getMessage(), e);
                }
            } finally {

                if (client != null) {
                    try {
                        releaseRpcClient(client);
                    } catch (IOException e) {
                        // ignore
                    }
                }
            }
        }
    }

    public synchronized void close() throws IOException {
        closed = true;
        List<OncRpcException> exceptionList = new ArrayList<OncRpcException>();
        for (int i = 0; i < rpcClientPool.size(); i++) {
            OncRpcClient client = rpcClientPool.get(i);
            try {
                client.close();
            } catch (OncRpcException e) {
                exceptionList.add(e);
            }
        }
        if (exceptionList.size() != 0) {
            StringBuilder builder = new StringBuilder();
            builder.append("An error occurs when the mount client close connections. Reason:");
            for (int i = 0; i < exceptionList.size(); i++) {
                builder.append(exceptionList.get(i).getMessage());
                builder.append(".");
            }
            throw new IOException(builder.toString());
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

    public LookupResult lookup(final byte[] fileHandle, final String entryName)
        throws NFS2Exception, IOException {
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

    public ListDirectoryResult listDirectory(final byte[] fileHandle, final byte[] cookie,
            final int count) throws NFS2Exception, IOException {
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

    public ReadFileResult readFile(final byte[] fileHandle, final int offset, final int count)
        throws NFS2Exception, IOException {
        if (count > MAX_DATA) {
            throw new IllegalArgumentException(
                    "The number of bytes read by the nfs client can not be greater than " + MAX_DATA);
        }
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
                // TODO Optimize this
                result.setData(xdr.xdrDecodeDynamicOpaque());
            }
        };
        call(PROCEDURE_READ_FILE, nfsParameter, nfsResult);
        return result;
    }

    public void removeDirectory(final byte[] fileHandle, final String name)
        throws NFS2Exception, IOException {
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

    public void removeFile(final byte[] parentFileHandle, final String name)
        throws NFS2Exception, IOException {
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

    public void renameFile(final byte[] fromParentFileHandle, final String fromName,
            final byte[] toParentFileHandle, final String toName) throws NFS2Exception, IOException {
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
            final boolean[] permission, final int uid, final int gid, final int size,
            final Time lastAccessed, final Time lastModified) throws NFS2Exception, IOException {
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

    public CreateFileResult createFile(final byte[] parentFileHandle, final String name,
            final boolean[] permission, final int uid, final int gid, final int size,
            final Time lastAccessed, final Time lastModified) throws NFS2Exception, IOException {
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

                // encode an array of bytes
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

    /**
     * Set the attributes for file.
     * 
     * 
     * @param fileHandle
     *                file handle.
     * @param mode
     *                mode.
     * @param uid
     * @param gid
     * @param size
     * @param lastAccessed
     * @param lastModified
     * @return
     * @throws NFS2Exception
     * @throws IOException
     */
    public FileAttribute setAttribute(final byte[] fileHandle, final int mode, final int uid,
            final int gid, final int size, final Time lastAccessed, final Time lastModified)
        throws NFS2Exception, IOException {
        XdrAble nfsParameter = new NFSParameter() {
            public void xdrEncode(XdrEncodingStream xdr) throws OncRpcException, IOException {
                xdr.xdrEncodeOpaque(fileHandle, FILE_HANDLE_SIZE);
                xdr.xdrEncodeInt(mode);
                xdr.xdrEncodeInt(uid);
                xdr.xdrEncodeInt(gid);
                xdr.xdrEncodeInt(size);
                xdrEncodeTime(xdr, lastAccessed);
                xdrEncodeTime(xdr, lastModified);
            }
        };

        final FileAttribute fileAttribute = new FileAttribute();
        XdrAble nfsResult = new NFSResult() {
            public void xdrDecode(XdrDecodingStream xdr) throws OncRpcException, IOException {
                xdrFileAttributeDecode(xdr, fileAttribute);
            }
        };
        call(PROCEDURE_SET_ATTRIBUTE, nfsParameter, nfsResult);
        return fileAttribute;
    }

    public FileSystemAttribute getFileSystemAttribute(final byte[] fileHandle)
        throws NFS2Exception, IOException {

        XdrAble nfsParameter = new NFSParameter() {
            public void xdrEncode(XdrEncodingStream xdr) throws OncRpcException, IOException {
                xdr.xdrEncodeOpaque(fileHandle, FILE_HANDLE_SIZE);
            }
        };

        final FileSystemAttribute fileSystemAttribute = new FileSystemAttribute();
        XdrAble nfsResult = new NFSResult() {
            public void xdrDecode(XdrDecodingStream xdr) throws OncRpcException, IOException {
                fileSystemAttribute.setTransferSize(xdrDecodeUnsignedInt(xdr));
                fileSystemAttribute.setBlockSize(xdrDecodeUnsignedInt(xdr));
                fileSystemAttribute.setBlockCount(xdrDecodeUnsignedInt(xdr));
                fileSystemAttribute.setFreeBlockCount(xdrDecodeUnsignedInt(xdr));
                fileSystemAttribute.setAvailableBlockCount(xdrDecodeUnsignedInt(xdr));
            }
        };
        call(PROCEDURE_GET_FILE_SYSTEM_ATTRIBUTE, nfsParameter, nfsResult);
        return fileSystemAttribute;
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

    private void xdrEncodeTime(XdrEncodingStream xdrEncodingStream, Time time)
        throws OncRpcException, IOException {
        xdrEncodingStream.xdrEncodeInt(time.getSeconds());
        xdrEncodingStream.xdrEncodeInt(time.getMicroSeconds());
    }

    private void xdrDecodeTime(XdrDecodingStream xdrDecodingStream, Time time)
        throws OncRpcException, IOException {
        time.setSeconds(xdrDecodingStream.xdrDecodeInt());
        time.setMicroSeconds(xdrDecodingStream.xdrDecodeInt());
    }

    private void xdrFileAttributeDecode(XdrDecodingStream xdr, FileAttribute fileAttribute)
        throws OncRpcException, IOException {
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
        fileAttribute.setFileId(xdr.xdrDecodeInt());
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

    private long xdrDecodeUnsignedInt(XdrDecodingStream xdr) throws OncRpcException, IOException {
        byte[] buffer = new byte[4];
        xdr.xdrDecodeOpaque(buffer);
        return ((buffer[0] & 0xFF) << 24 | (buffer[1] & 0xFF) << 16 | 
                (buffer[2] & 0xFF) << 8 | (buffer[3] & 0xFF));
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
