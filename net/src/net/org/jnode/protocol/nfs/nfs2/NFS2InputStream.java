package org.jnode.protocol.nfs.nfs2;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.util.List;
import java.util.StringTokenizer;

import org.jnode.net.nfs.Protocol;
import org.jnode.net.nfs.nfs2.FileAttribute;
import org.jnode.net.nfs.nfs2.LookupResult;
import org.jnode.net.nfs.nfs2.NFS2Client;
import org.jnode.net.nfs.nfs2.NFS2Exception;
import org.jnode.net.nfs.nfs2.ReadFileResult;
import org.jnode.net.nfs.nfs2.mount.ExportEntry;
import org.jnode.net.nfs.nfs2.mount.Mount1Client;
import org.jnode.net.nfs.nfs2.mount.MountException;
import org.jnode.net.nfs.nfs2.mount.MountResult;

/**
 * A NFS2InputStream obtains the bytes from a nfs2 connection. 
 * The URL is nfs://host/remotePath
 * The remotePath contains also the export path.
 * 
 * 
 * @author Andrei Dore
 *
 */
public class NFS2InputStream extends InputStream {

    private static int DEFAULT_BUFFER_SIZE = NFS2Client.MAX_DATA;

    private byte[] buffer;

    private int bufferCount;

    private int bufferPosition;

    private long markFileOffset = -1;

    private int markLimit = -1;

    private Mount1Client mountClient;

    private NFS2Client nfsClient;

    private String mountDirectory;

    private long fileOffset;

    private byte[] fileHandle;

    private FileAttribute fileAttribute;

    public NFS2InputStream(URL url) throws IOException {

        mountClient = new Mount1Client(InetAddress.getByName(url.getHost()),
                Protocol.TCP, -1, -1);

        nfsClient = new NFS2Client(InetAddress.getByName(url.getHost()),
                Protocol.TCP, -1, -1);

        String path = url.getPath();

        List<ExportEntry> exportList;
        try {
            exportList = mountClient.export();
        } catch (MountException e) {
            try {
                mountClient.close();
            } catch (IOException e1) {
            }
            throw new IOException(e.getMessage());
        } catch (IOException e) {
            try {
                mountClient.close();
            } catch (IOException e1) {
            }
            throw e;
        }

        ExportEntry exportEntry = null;

        for (int i = 0; i < exportList.size(); i++) {

            ExportEntry e = exportList.get(i);
            if (path.startsWith(e.getDirectory())) {

                if (exportEntry == null) {
                    exportEntry = e;
                } else {

                    if (exportEntry.getDirectory().length() < e.getDirectory()
                            .length()) {
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
            try {
                mountClient.close();
            } catch (IOException e1) {
            }
            throw new IOException(e.getMessage());
        } catch (IOException e) {
            try {
                mountClient.close();
            } catch (IOException e1) {
            }
            throw e;
        }

        byte[] tempFileHandle = mountResult.getFileHandle();

        try {
            String filePath = path.substring(exportEntry.getDirectory()
                    .length());

            StringTokenizer tokenizer = new StringTokenizer(filePath, "/");
            while (tokenizer.hasMoreElements()) {
                String t = tokenizer.nextToken();

                LookupResult lookup = nfsClient.lookup(tempFileHandle, t);

                if (lookup.getFileAttribute().getType() == FileAttribute.FILE) {
                    fileHandle = lookup.getFileHandle();
                    fileAttribute = lookup.getFileAttribute();
                    break;
                } else if (lookup.getFileAttribute().getType() == FileAttribute.DIRECTORY) {
                    tempFileHandle = lookup.getFileHandle();
                } else {
                    throw new IOException(
                            "The path contains an unknow resource: " + t
                                    + ". It is not directory or file");
                }

            }
        } catch (NFS2Exception e) {

            try {
                close();
            } catch (IOException e1) {
            }

            throw new IOException(e.getMessage());

        } catch (IOException e) {
            try {
                close();
            } catch (IOException e1) {
            }

            throw e;
        }

        if (fileHandle == null) {
            throw new IOException("The target of the " + url.toString()
                    + " it is not a file.");
        }

        buffer = new byte[DEFAULT_BUFFER_SIZE];

    }

    @Override
    public synchronized int read() throws IOException {

        if (bufferPosition >= bufferCount) {

            if (fillBuffer() == 0) {
                return -1;
            }

        }

        int data = buffer[bufferPosition] & 0xFF;

        bufferPosition++;

        return data;

    }

    @Override
    public synchronized int read(byte[] b, int off, int len) throws IOException {

        if (len == 0) {
            return 0;
        }

        int readBytes = 0;

        while (true) {

            if (readBytes == len) {
                return readBytes;
            }

            int avail = bufferCount - bufferPosition;
            if (avail == 0) {
                if (fillBuffer() == 0) {

                    if (readBytes == 0) {
                        return -1;
                    }

                    return readBytes;
                }
            }

            int count = Math.min(len - readBytes, bufferCount - bufferPosition);

            System.arraycopy(buffer, bufferPosition, b, off + readBytes, count);

            readBytes += count;

            bufferPosition += count;

        }

    }

    @Override
    public synchronized int available() throws IOException {
        return bufferCount - bufferPosition;
    }

    private int fillBuffer() throws IOException {

        if (fileOffset >= fileAttribute.getSize()) {
            return 0;
        }

        ReadFileResult readFileResult;

        try {
            readFileResult = nfsClient.readFile(fileHandle, (int) fileOffset,
                    DEFAULT_BUFFER_SIZE);
        } catch (NFS2Exception e) {
            throw new IOException(e.getMessage());
        }

        fileAttribute = readFileResult.getFileAttribute();

        fileOffset += readFileResult.getData().length;

        System.arraycopy(readFileResult.getData(), 0, buffer, 0, readFileResult
                .getData().length);

        bufferPosition = 0;
        bufferCount = readFileResult.getData().length;
        return bufferCount;

    }

    @Override
    public synchronized void mark(int readlimit) {
        this.markLimit = readlimit;
        this.markFileOffset = fileOffset;
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public synchronized void reset() throws IOException {

        if (markFileOffset == -1 && markLimit == -1) {
            throw new IOException(
                    "The mark was not set. Use mark method to set the mark");
        }

        if (fileOffset - markFileOffset > markLimit) {
            throw new IOException("The mark limit exced.");
        }

        fileOffset = markFileOffset;

        // reset the buffer
        bufferPosition = 0;
        bufferCount = 0;

        // TODO Optimize this . If the mark it is buffer don't reset the buffer.
        // Unfortunatlly it is not a simple modification in this method.
    }

    public synchronized long skip(long n) throws IOException {

        if (n <= 0) {
            return 0;
        }

        if (n < bufferCount - bufferPosition) {

            // It is inside of the buffer
            bufferPosition += n;

            return n;
        } else {
            // It is outside of the buffer

            // reset the buffer
            bufferCount = 0;
            bufferPosition = 0;

            if (fileOffset + n - (bufferCount - bufferPosition) < fileAttribute
                    .getSize()) {

                fileOffset += n - (bufferCount - bufferPosition);

                return n;

            } else {

                long skipBytes = fileAttribute.getSize() - fileOffset;

                fileOffset = fileAttribute.getSize();

                return skipBytes;

            }

        }

    }

    // TODO Remove the synch in the future
    @Override
    public synchronized void close() throws IOException {

        if (mountClient != null) {

            try {
                mountClient.unmount(mountDirectory);
            } catch (MountException e) {

            }

            try {
                mountClient.close();
            } catch (IOException e) {
            }

        }

        if (nfsClient != null) {
            try {
                nfsClient.close();
            } catch (IOException e) {
            }
        }

    }

}
