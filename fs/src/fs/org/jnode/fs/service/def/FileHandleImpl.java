/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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
 
package org.jnode.fs.service.def;

import java.io.IOException;
import java.io.VMOpenMode;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;

import org.jnode.fs.FSFile;
import org.jnode.java.io.VMFileHandle;

/**
 * @author epr
 */
final class FileHandleImpl implements VMFileHandle {

    /** The open mode of this filehandle */
    private final VMOpenMode mode;
    /** The actual file on the filesystem */
    private final FSFile file;
    /** Is this a readonly connection? */
    private final boolean readOnly;
    /** The manager i'll use to close me */
    private final FileHandleManager fhm;
    /** Am i closed? */
    private boolean closed;
    /** Position within this file */
    private long fileOffset;

    /**
     * Create a new instance
     * 
     * @param file
     * @param mode
     * @param fhm
     */
    public FileHandleImpl(FSFile file, VMOpenMode mode, FileHandleManager fhm) {
        this.mode = mode;
        this.file = file;
        this.readOnly = (mode == VMOpenMode.READ);
        this.fhm = fhm;
        this.closed = false;

        // WRITE only mode, i.e. NOT APPEND mode. Thus we have to set the
        // filesize to 0
        if (!mode.canRead() && mode.canWrite()) {
            try {
                file.setLength(0);
            } catch (IOException e) {
                // todo improve it - RuntimeException is not the best choice
                // here
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Gets the length (in bytes) of this file
     * 
     * @return long
     */
    public synchronized long getLength() {
        if (closed) {
            return 0;
        }
        return file.getLength();
    }

    /**
     * Sets the length of this file.
     * 
     * @param length
     * @throws IOException
     */
    public synchronized void setLength(long length) throws IOException {
        if (closed) {
            throw new IOException("File closed");
        }
        if (readOnly) {
            throw new IOException("Cannot write");
        }
        file.setLength(length);
        // todo check this
        // if (length > fileOffset) {
        fileOffset = length;
        // }
    }

    /**
     * Gets the current position in the file
     * 
     * @return long
     */
    public long getPosition() {
        return fileOffset;
    }

    /**
     * Sets the position in the file.
     * 
     * @param position
     * @throws IOException
     */
    public void setPosition(long position) throws IOException {
        if (position < 0) {
            throw new IOException("Position < 0");
        }
        if (position > getLength()) {
            // allow seeking beyond the end of file by extending the file
            // TODO Investigate this decision - the classpath implementations of
            // TODO RandomAccessFile (which is currently used) requires this.
            // TODO Review it when the RandomAccessFile of OpenJDK is
            // integrated.
            setLength(position);
            // throw new IOException("Position > file size");
        }
        this.fileOffset = position;
    }

    /**
     * Read <code>len</code> bytes from the given position. The read data is
     * read fom this file starting at offset <code>fileOffset</code> and
     * stored in <code>dest</code> starting at offset <code>ofs</code>.
     * 
     * @param dest
     * @param off
     * @param len
     * @throws IOException
     */
    public synchronized int read(ByteBuffer dest) throws IOException {
        if (closed) {
            throw new IOException("File closed");
        }
        int avail = available();
        if (avail < 1) {
            return -1; // eof
        }

        int nbRead = Math.min(dest.remaining(), avail);
        dest.limit(dest.position() + nbRead);

        // TODO file.read should return the number of read bytes
        // file.read(fileOffset, dest, off, nbRead);
        file.read(fileOffset, dest);
        fileOffset += nbRead;
        return nbRead;
    }

    /**
     * Write <code>len</code> bytes to the given position. The data is read
     * from <code>src</code> starting at offset <code>ofs</code> and written
     * to this file starting at offset <code>fileOffset</code>.
     * 
     * @param src
     * @param off
     * @param len
     * @throws IOException
     */
    // public synchronized void write(byte[] src, int off, int len) throws
    // IOException {
    public synchronized void write(ByteBuffer src) throws IOException {
        if (closed) {
            throw new IOException("File closed");
        }
        if (readOnly) {
            throw new IOException("Cannot write");
        }

        // TODO file.write should return the number of written bytes
        final int len = src.remaining();
        file.write(fileOffset, src);
        fileOffset += len;
    }

    /**
     * Close this file.
     */
    public synchronized void close() throws IOException {
        file.flush();
        closed = true;
        fhm.close(this);
    }

    /**
     * Has this handle been closed?
     */
    public boolean isClosed() {
        return closed;
    }

    /**
     * Duplicate this handle
     * 
     * @throws IOException
     */
    public VMFileHandle dup(VMOpenMode newMode) throws IOException {
        return fhm.dup(this, newMode);
    }

    /**
     * Gets the file of this handle
     */
    public FSFile getFile() {
        return file;
    }

    /**
     * Gets the mode of this handle
     */
    public VMOpenMode getMode() {
        return mode;
    }

    /**
     * Is this handle readonly
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    public int available() {
        long avail = Math.max(0L, file.getLength() - fileOffset);
        return (avail > Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int) avail;
    }

    public void unlock(long pos, long len) {
        // TODO Auto-generated method stub
    }

    public int read() throws IOException {
        // TODO very inefficient, optimize it
        ByteBuffer dest = ByteBuffer.allocate(1);
        int nbRead = -1;
        nbRead = read(dest);
        if (nbRead < 1) {
            return -1; // eof
        }
        return dest.get(0);
    }

    public void write(int b) throws IOException {
        // TODO very inefficient, optimize it
        ByteBuffer src = ByteBuffer.wrap(new byte[] {(byte) b});
        write(src);
    }

    public boolean lock() {
        // TODO Auto-generated method stub
        return true;
    }

    public MappedByteBuffer mapImpl(char mode, long position, int size) {
        // TODO Auto-generated method stub
        return null;
    }
}
