/*
 * $Id$
 *
 * Copyright (C) 2003-2012 JNode.org
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
 
package org.jnode.fs.ramfs;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import java.util.ArrayList;
import org.jnode.fs.FSAccessRights;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FSFile;
import org.jnode.fs.FileSystem;
import org.jnode.fs.FileSystemFullException;

/**
 * A File implementation in the system RAM
 * 
 * @author peda
 * @author Levente S\u00e1ntha
 */
public class RAMFile implements FSEntry, FSFile {

    private RAMFileSystem fileSystem;
    private RAMDirectory parent;

    private String filename;
    private BufferList bufferList;

    private long created;
    private long lastModified;
    private long lastAccessed;
    private FSAccessRights accessRights;

    private boolean isValid = true;

    /**
     * Constructor for a new RAMFile
     * 
     * @param parent
     * @param filename
     */
    public RAMFile(RAMDirectory parent, String filename) {
        this.parent = parent;
        this.filename = filename;
        this.created = this.lastModified = this.lastAccessed = System.currentTimeMillis();

        // TODO accessRights

        bufferList = new BufferList();

        fileSystem = (RAMFileSystem) parent.getFileSystem();

        fileSystem.addSummmedBufferSize(bufferList.capacity());
    }

    private void enlargeBuffer() throws FileSystemFullException {

        int oldCapacity = bufferList.capacity();

        if (oldCapacity > fileSystem.getFreeSpace())
            throw new FileSystemFullException("RAMFileSystem reached maxSize");

        bufferList.enlarge();
        int newCapacity = bufferList.capacity();

        // update fileSystem values
        fileSystem.addSummmedBufferSize(newCapacity - oldCapacity);
    }

    private void shrinkBuffer() {

        int oldCapacity = bufferList.capacity();

        bufferList.shrink();
        int newCapacity = bufferList.capacity();

        // update fileSystem counter
        fileSystem.addSummmedBufferSize(newCapacity - oldCapacity);
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.jnode.fs.FSEntry#getName()
     */
    public String getName() {
        return filename;
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.jnode.fs.FSEntry#getParent()
     */
    public FSDirectory getParent() {
        return parent;
    }

    public long getCreated() throws IOException {
        return created;
    }

    public long getLastModified() throws IOException {
        return lastModified;
    }

    public long getLastAccessed() throws IOException {
        return lastAccessed;
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.jnode.fs.FSEntry#isFile()
     */
    public boolean isFile() {
        return true;
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.jnode.fs.FSEntry#isDirectory()
     */
    public boolean isDirectory() {
        return false;
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.jnode.fs.FSEntry#setName(java.lang.String)
     */
    public void setName(String newName) throws IOException {
        // TODO check for special chars / normalize name
        filename = newName;
        setLastModified(System.currentTimeMillis());
    }

    public void setCreated(long created) throws IOException {
        this.created = created;
    }

    public void setLastModified(long lastModified) throws IOException {
        this.lastModified = lastModified;
    }

    public void setLastAccessed(long lastAccessed) throws IOException {
        this.lastAccessed = lastAccessed;
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.jnode.fs.FSEntry#getFile()
     */
    public FSFile getFile() throws IOException {
        return this;
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.jnode.fs.FSEntry#getDirectory()
     */
    public FSDirectory getDirectory() throws IOException {
        throw new IOException("Not a directory");
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.jnode.fs.FSEntry#getAccessRights()
     */
    public FSAccessRights getAccessRights() throws IOException {
        return accessRights;
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.jnode.fs.FSEntry#isDirty()
     */
    public boolean isDirty() throws IOException {
        return false;
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.jnode.fs.FSObject#isValid()
     */
    public boolean isValid() {
        return isValid;
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.jnode.fs.FSObject#getFileSystem()
     */
    public FileSystem<?> getFileSystem() {
        return fileSystem;
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.jnode.fs.FSFile#getLength()
     */
    public long getLength() {
        return bufferList.limit();
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.jnode.fs.FSFile#setLength(long)
     */
    public void setLength(long length) throws IOException {

        if (length > Integer.MAX_VALUE)
            throw new IOException("Filesize too large");

        while (bufferList.capacity() < length)
            enlargeBuffer();

        long toEnlarge = length - bufferList.limit();

        while (length < bufferList.capacity() / 2)
            shrinkBuffer();

        bufferList.limit((int) length);

        // update fileSystem counters
        fileSystem.addSummedFileSize(toEnlarge);
        setLastModified(System.currentTimeMillis());
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.jnode.fs.FSFile#read(long, java.nio.ByteBuffer)
     */
    public void read(long fileOffset, ByteBuffer dest) throws IOException {

        long currentSize = bufferList.limit();
        long toRead = dest.limit();

        if (fileOffset + toRead > currentSize)
            throw new IOException("FileOffest outside file");

        bufferList.position((int) fileOffset);
        bufferList.get(dest.array(), 0, dest.limit());
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.jnode.fs.FSFile#write(long, java.nio.ByteBuffer)
     */
    public void write(long fileOffset, ByteBuffer src) throws IOException {

        long currentSize = bufferList.limit();
        long toWrite = src.limit();

        if (fileOffset + toWrite >= currentSize)
            setLength(fileOffset + toWrite);

        bufferList.position((int) fileOffset);
        bufferList.put(src);
        setLastModified(System.currentTimeMillis());
    }

    /**
     * (non-Javadoc)
     * @see org.jnode.fs.FSFile#flush()
     */
    public void flush() throws IOException {
        // nothing todo here
    }

    void remove() throws IOException {

        long capacity = bufferList.capacity();
        long filesize = getLength();

        this.parent = null;
        this.bufferList = null;

        fileSystem.addSummedFileSize(-filesize);
        fileSystem.addSummmedBufferSize(-capacity);
    }

    /**
     * A resizing Buffer-like structure combining a set of NIO Buffers into one entity.
     *
     * @author Levente S\u00e1ntha
     */
    private static class BufferList {
        private static final int MAX_BUFFER_SIZE = 12 * 1024 * 1024;
        private final ArrayList<ByteBuffer> bufferList;

        BufferList() {
            bufferList = new ArrayList<ByteBuffer>();
            ByteBuffer buffer = ByteBuffer.allocate(128);
            buffer.limit(0);
            bufferList.add(buffer);
        }

        private void enlarge() {
            int oldCapacity = capacity();
            if (bufferList.size() == 1) {
                final ByteBuffer oldBuffer = bufferList.get(0);
                final int newCapacity = oldCapacity * 2;
                bufferList.clear();
                if (newCapacity > MAX_BUFFER_SIZE) {
                    final ByteBuffer newBuffer = ByteBuffer.allocate(MAX_BUFFER_SIZE);
                    oldBuffer.position(0);
                    newBuffer.put(oldBuffer);
                    newBuffer.position(0);

                    bufferList.add(newBuffer);
                    bufferList.add(ByteBuffer.allocate(newCapacity - MAX_BUFFER_SIZE));
                } else {
                    ByteBuffer buffer2 = ByteBuffer.allocate(newCapacity);
                    oldBuffer.position(0);
                    buffer2.put(oldBuffer);
                    buffer2.position(0);

                    bufferList.add(buffer2);
                }
            } else {
                bufferList.add(ByteBuffer.allocate(MAX_BUFFER_SIZE));
            }
        }

        private void shrink() {
            final int oldCapacity = capacity();
            if (bufferList.size() == 1) {
                final ByteBuffer oldBuffer = bufferList.get(0);
                final int newCapacity = oldCapacity / 2;
                bufferList.clear();

                final ByteBuffer newBuffer = ByteBuffer.allocate(newCapacity);
                oldBuffer.position(0);
                oldBuffer.limit(newCapacity);
                newBuffer.put(oldBuffer);
                newBuffer.position(0);

                bufferList.add(newBuffer);
            } else {
                bufferList.remove(bufferList.size() - 1);
            }
        }

        private int capacity() {
            int capacity = 0;
            for (ByteBuffer buffer : bufferList) {
                capacity += buffer.capacity();
            }
            return capacity;
        }

        private int limit() {
            int limit = 0;
            for (ByteBuffer buffer : bufferList) {
                limit += buffer.limit();
            }
            return limit;
        }

        private void limit(int limit) {
            for (ByteBuffer buffer : bufferList) {
                if (limit < 0) {
                    buffer.limit(0);
                } else {
                    int capacity = buffer.capacity();
                    if (limit <= capacity) {
                        buffer.limit(limit);
                    } else {
                        buffer.limit(capacity);
                    }
                    limit -= capacity;
                }
            }
        }

        private int position() {
            int position = 0;
            for (ByteBuffer buffer : bufferList) {
                position += buffer.position();
            }
            return position;
        }

        public void position(int position) {
            for (ByteBuffer buffer : bufferList) {
                if (position < 0) {
                    buffer.position(0);
                } else {
                    int limit = buffer.limit();
                    if (position <= limit) {
                        buffer.position(position);
                    } else {
                        buffer.position(limit);
                    }
                    position -= limit;
                }
            }
        }

        public int remaining() {
            int remaining = 0;
            for (ByteBuffer buffer : bufferList) {
                remaining += buffer.remaining();
            }
            return remaining;
        }

        public void get(byte[] array, int offset, int length) {

            if ((offset | length | (offset + length) | (array.length - (offset + length))) < 0)
                throw new IndexOutOfBoundsException();

            if (length > remaining())
                throw new BufferUnderflowException();

            for (ByteBuffer buffer : bufferList) {
                int remaining = buffer.remaining();
                if (remaining > 0) {
                    if (length > remaining) {
                        buffer.get(array, offset, remaining);
                        offset += remaining;
                        length -= remaining;
                    } else {
                        buffer.get(array, offset, length);
                        return;
                    }
                }
            }
        }

        public void put(ByteBuffer src) {
            int length = src.remaining();
            if (length > remaining())
                throw new BufferOverflowException();

            for (ByteBuffer buffer : bufferList) {
                int remaining = buffer.remaining();
                if (remaining > 0) {
                    if (length > remaining) {
                        src.limit(src.position() + remaining);
                        buffer.put(src);
                        length -= remaining;
                    } else {
                        src.limit(src.position() + length);
                        buffer.put(src);
                        return;
                    }
                }
            }
        }
    }
}
