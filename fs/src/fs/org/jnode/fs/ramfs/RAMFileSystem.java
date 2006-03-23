package org.jnode.fs.ramfs;

import java.io.IOException;

import org.jnode.driver.Device;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FileSystem;
import org.jnode.fs.FileSystemException;


public class RAMFileSystem implements FileSystem {

	private Device device;
	private boolean readOnly;
	
	private RAMDirectory root;

	private final long maxSize;
	private long summedBufferSize;
	private long summedFileSize;
	
	/**
	 * Constructor for RAMFileSystem
	 */
	public RAMFileSystem(Device device, boolean readOnly, int maxSize) throws FileSystemException {
		this.device = device;
		this.readOnly = readOnly;
		
		this.maxSize = maxSize;
		summedBufferSize = 0;
		summedFileSize = 0;
		
		root = new RAMDirectory(this, null, "");
	}

	public Device getDevice() {
		return device;
	}

	public FSEntry getRootEntry() throws IOException {
		if (isClosed())
			throw new IOException("Filesystem closed");
		return root;
	}
	
	public boolean isReadOnly() {
		return readOnly;
	}

	public void close() throws IOException {
		root = null;
	}

	public boolean isClosed() {
		return (root == null);
	}

	/**
     * (non-Javadoc)
     * @see org.jnode.fs.FileSystem#getTotalSpace()
     */
    public long getTotalSpace() {
    	return maxSize;
    }
    
    /**
     * (non-Javadoc)
     * @see org.jnode.fs.FileSystem#getFreeSpace()
     */
    public long getFreeSpace() {
    	return maxSize - summedBufferSize;
    }
    
    /**
     * (non-Javadoc)
     * @see org.jnode.fs.FileSystem#getUsableSpace()
     */
    public long getUsableSpace() {
    	return maxSize - summedFileSize;
    }
    
	synchronized void addSummmedBufferSize(long toAdd) {
		summedBufferSize += toAdd;
	}

	synchronized void addSummedFileSize(long toAdd) {
			summedFileSize += toAdd;
	}
}
