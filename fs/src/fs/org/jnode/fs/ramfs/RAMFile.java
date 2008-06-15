package org.jnode.fs.ramfs;

import java.io.IOException;
import java.nio.ByteBuffer;

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
 */
public class RAMFile implements FSEntry, FSFile {

    private RAMFileSystem fileSystem;
    private RAMDirectory parent;

    private String filename;
    private ByteBuffer buffer;

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

        buffer = ByteBuffer.allocate(128);
        buffer.limit(0);

        fileSystem = (RAMFileSystem) parent.getFileSystem();

        fileSystem.addSummmedBufferSize(128);
    }

    private void enlargeBuffer() throws FileSystemFullException {

        int oldCapacity = buffer.capacity();

        if (oldCapacity > fileSystem.getFreeSpace())
            throw new FileSystemFullException("RAMFileSystem reached maxSize");

        ByteBuffer temp = ByteBuffer.allocate(oldCapacity * 2);
        buffer.position(0);
        temp.put(buffer);
        buffer = temp;
        buffer.position(0);

        // update fileSystem values
        fileSystem.addSummmedBufferSize(oldCapacity);
    }

    private void shrinkBuffer() {

        int toShrink = buffer.capacity() / 2;

        ByteBuffer temp = ByteBuffer.allocate(toShrink);
        temp.put(buffer.array(), 0, toShrink);
        buffer = temp;
        buffer.position(0);

        // update fileSystem counter
        fileSystem.addSummmedBufferSize(-toShrink);
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
    public FileSystem getFileSystem() {
        return fileSystem;
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.jnode.fs.FSFile#getLength()
     */
    public long getLength() {
        return buffer.limit();
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.jnode.fs.FSFile#setLength(long)
     */
    public void setLength(long length) throws IOException {

        if (length > Integer.MAX_VALUE)
            throw new IOException("Filesize too large");

        while (buffer.capacity() < length)
            enlargeBuffer();

        long toEnlarge = length - buffer.limit();

        while (length < buffer.capacity() / 2)
            shrinkBuffer();

        buffer.limit((int) length);

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

        long currentSize = buffer.limit();
        long toRead = dest.limit();

        if (fileOffset + toRead > currentSize)
            throw new IOException("FileOffest outside file");

        buffer.position((int) fileOffset);
        buffer.get(dest.array(), 0, dest.limit());
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.jnode.fs.FSFile#write(long, java.nio.ByteBuffer)
     */
    public void write(long fileOffset, ByteBuffer src) throws IOException {

        long currentSize = buffer.limit();
        long toWrite = src.limit();

        if (fileOffset + toWrite >= currentSize)
            setLength(fileOffset + toWrite);

        buffer.position((int) fileOffset);
        buffer.put(src);
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

        long capacity = buffer.capacity();
        long filesize = getLength();

        this.parent = null;
        this.buffer = null;

        fileSystem.addSummedFileSize(-filesize);
        fileSystem.addSummmedBufferSize(-capacity);
    }
}
