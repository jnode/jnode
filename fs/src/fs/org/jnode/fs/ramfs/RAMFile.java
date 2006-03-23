package org.jnode.fs.ramfs;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.log4j.Logger;
import org.jnode.fs.FSAccessRights;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FSFile;
import org.jnode.fs.FileSystem;
import org.jnode.fs.FileSystemFullException;

public class RAMFile implements FSEntry, FSFile {

	/**	Logger*/	
	private static final Logger log = Logger.getLogger(RAMFile.class);

	private RAMFileSystem fileSystem;
	private RAMDirectory parent;

	private String filename;
	private ByteBuffer buffer;

	private long lastModified;
	private FSAccessRights accessRights;

	private boolean isValid = true;
	
	public RAMFile(RAMDirectory parent, String filename) {
		this.parent = parent;
		this.filename = filename;
		this.lastModified = System.currentTimeMillis();
		
		// TODO accessRights
		
		buffer = ByteBuffer.allocate(128);
		buffer.limit(0);
		
		fileSystem = (RAMFileSystem) parent.getFileSystem();
			
		fileSystem.addSummmedBufferSize(128);
	}

	
	private void enlargeBuffer() throws FileSystemFullException {

		//log.debug("Enlarge Buffer, oldCapacity = " + buffer.capacity());
		
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

	public String getName() {
		return filename;
	}

	public FSDirectory getParent() {
		return parent;
	}

	public long getLastModified() throws IOException {
		return lastModified;
	}

	public boolean isFile() {
		return true;
	}

	public boolean isDirectory() {
		return false;
	}

	public void setName(String newName) throws IOException {
		// TODO check for special chars
		filename = newName;
	}

	public void setLastModified(long lastModified) throws IOException {
		this.lastModified = lastModified;
	}

	public FSFile getFile() throws IOException {
		return this;
	}

	public FSDirectory getDirectory() throws IOException {
		throw new IOException("Not a directory");
	}

	public FSAccessRights getAccessRights() throws IOException {
		return accessRights;
	}

	public boolean isDirty() throws IOException {
		return false;
	}

	public boolean isValid() {
		return isValid;
	}

	public FileSystem getFileSystem() {
		return fileSystem;
	}

	public long getLength() {
		return buffer.limit();
	}

	public void setLength(long length) throws IOException {
		
		//log.debug("NewLength for file " + filename + " is " + length);

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
	}

	public void read(long fileOffset, ByteBuffer dest) throws IOException {

		//log.debug("Read file " + filename + " from " + fileOffset + " and read " + dest.limit() + " bytes");
		
		long currentSize = buffer.limit();
		long toRead = dest.limit();
		
		if (fileOffset + toRead > currentSize)
			throw new IOException ("FileOffest outside file");

		buffer.position((int) fileOffset);
		buffer.get(dest.array(), 0, dest.limit());

		//log.debug("Text toRead::" + new String(dest.array()));
	}

	public void write(long fileOffset, ByteBuffer src) throws IOException {
		
		//log.debug("Write file " + filename + " from " + fileOffset + " and write " + src.limit() + " bytes");
		//log.debug("Text was::" + new String(src.array()));
		
		long currentSize = buffer.limit();
		long toWrite = src.limit();
		
		if (fileOffset + toWrite >= currentSize)
			setLength(fileOffset + toWrite);
		
		buffer.position((int) fileOffset);
		buffer.put(src);
	}

	public void flush() throws IOException {
		// nothing todo here
	}
	
	public void remove() throws IOException {
		
		//log.debug("Remove file " + filename);
		
		long capacity = buffer.capacity();
		long filesize = getLength();
		
		this.parent = null;
		this.buffer = null;
		
		fileSystem.addSummedFileSize(-filesize);
		fileSystem.addSummmedBufferSize(-capacity);
	}
}
