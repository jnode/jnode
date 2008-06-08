package org.jnode.protocol.nfs.nfs2;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.jnode.net.nfs.Protocol;
import org.jnode.net.nfs.nfs2.CreateFileResult;
import org.jnode.net.nfs.nfs2.FileAttribute;
import org.jnode.net.nfs.nfs2.LookupResult;
import org.jnode.net.nfs.nfs2.NFS2Client;
import org.jnode.net.nfs.nfs2.NFS2Exception;
import org.jnode.net.nfs.nfs2.Time;
import org.jnode.net.nfs.nfs2.mount.ExportEntry;
import org.jnode.net.nfs.nfs2.mount.Mount1Client;
import org.jnode.net.nfs.nfs2.mount.MountException;
import org.jnode.net.nfs.nfs2.mount.MountResult;

public class NFS2OutputStream extends OutputStream {
    private static final boolean DEFAULT_PERMISSION[] =
            new boolean[] {true, true, false, true, false, false, true, false, false};
    private static final int DEFAULT_BUFFER_SIZE = NFS2Client.MAX_DATA;
    private Mount1Client mountClient;
    private NFS2Client nfsClient;
    private String mountDirectory;
    private byte[] fileHandle;
    private FileAttribute fileAttribute;
    private long fileOffset;
    private byte[] buffer;
    private int count;

    public NFS2OutputStream(URL url) throws IOException {
        int uid;
        int gid;
        String userinfo = url.getUserInfo();
        if (userinfo != null) {
            final int pos = userinfo.indexOf(':');
            if (pos != -1) {
                uid = Integer.parseInt(userinfo.substring(0, pos));
                gid = Integer.parseInt(userinfo.substring(pos + 1));
            } else {
                throw new IOException("The url doesn't contains the uid and guid.");
            }
        } else {
            throw new IOException("The url doesn't contains the uid and guid.");
        }

        mountClient = new Mount1Client(InetAddress.getByName(url.getHost()), Protocol.TCP, uid, gid);
        nfsClient = new NFS2Client(InetAddress.getByName(url.getHost()), Protocol.TCP, uid, gid);
        String path = url.getPath();
        List<ExportEntry> exportList;
        try {
            exportList = mountClient.export();
        } catch (MountException e1) {
            mountClient.close();
            throw new IOException(e1.getMessage());
        }

        ExportEntry exportEntry = null;
        for (int i = 0; i < exportList.size(); i++) {
            ExportEntry e = exportList.get(i);
            if (path.startsWith(e.getDirectory())) {
                if (exportEntry == null) {
                    exportEntry = e;
                } else {
                    if (exportEntry.getDirectory().length() < e.getDirectory().length()) {
                        exportEntry = e;
                    }
                }
            }
        }
        if (exportEntry == null) {
            throw new IOException("The path " + path + " it is not exported");
        }
        mountDirectory = exportEntry.getDirectory();
        MountResult mountResult;
        try {
            mountResult = mountClient.mount(mountDirectory);
        } catch (MountException e) {
            mountClient.close();
            throw new IOException(e.getMessage());
        }

        byte[] tempFileHandle = mountResult.getFileHandle();
        try {
            String filePath = path.substring(exportEntry.getDirectory().length());
            StringTokenizer tokenizer = new StringTokenizer(filePath, "/");
            List<String> tokenList = new ArrayList<String>();
            while (tokenizer.hasMoreElements()) {
                String t = tokenizer.nextToken();
                tokenList.add(t);
            }
            for (int i = 0; i < tokenList.size() - 1; i++) {
                String t = tokenList.get(i);
                LookupResult lookup = nfsClient.lookup(tempFileHandle, t);
                if (lookup.getFileAttribute().getType() == FileAttribute.FILE) {
                    throw new IOException("The path contains a file : " + t + ".");
                } else if (lookup.getFileAttribute().getType() == FileAttribute.DIRECTORY) {
                    tempFileHandle = lookup.getFileHandle();
                } else {
                    throw new IOException("The path contains an unknow resource: " + t +
                            ". It is not directory or file");
                }
            }
            CreateFileResult result =
                    nfsClient.createFile(tempFileHandle, tokenList.get(tokenList.size() - 1),
                            DEFAULT_PERMISSION, uid, gid, 0, new Time(-1, -1), new Time(-1, -1));
            fileHandle = result.getFileHandle();
            fileAttribute = result.getFileAttribute();
        } catch (NFS2Exception e) {
            try {
                mountClient.unmount(mountDirectory);
            } catch (MountException e1) {
                // ignore
            }
            mountClient.close();
            nfsClient.close();
            throw new IOException(e.getMessage());
        }
        if (fileHandle == null) {
            throw new IOException("The target of the " + url.toString() + " it is not a file.");
        }
        buffer = new byte[DEFAULT_BUFFER_SIZE];
    }

    private void flushBuffer() throws IOException {
        if (count == 0) {
            return;
        }
        try {
            fileAttribute = nfsClient.writeFile(fileHandle, (int) fileOffset, buffer, 0, count);
        } catch (NFS2Exception e) {
            throw new IOException(e);
        }
        fileOffset += count;
        count = 0;
    }

    @Override
    public synchronized void write(int b) throws IOException {
        if (count >= buffer.length) {
            flushBuffer();
        }
        buffer[count] = (byte) b;
        count++;
    }

    @Override
    public synchronized void flush() throws IOException {
        flushBuffer();
    }

    @Override
    public synchronized void write(byte[] b, int off, int len) throws IOException {
        int writeBytes = 0;
        while (writeBytes != len) {
            if (count >= buffer.length) {
                flushBuffer();
            }
            int c = Math.min(buffer.length - count, len - writeBytes);
            System.arraycopy(b, off + writeBytes, buffer, count, c);
            writeBytes += c;
            count += c;
        }
    }

    // TODO Remove the synch in the future
    @Override
    public synchronized void close() throws IOException {
        if (mountClient != null) {
            try {
                mountClient.unmount(mountDirectory);
            } catch (MountException e) {
                // ignore
            }
            try {
                mountClient.close();
            } catch (IOException e) {
                // ignore
            }
        }
        if (nfsClient != null) {
            try {
                nfsClient.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }
}
