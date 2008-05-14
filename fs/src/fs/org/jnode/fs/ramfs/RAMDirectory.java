package org.jnode.fs.ramfs;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.jnode.fs.FSAccessRights;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FSFile;
import org.jnode.fs.FileSystem;

/**
 * A Directory implementation in the system RAM
 * 
 * @author peda
 */
public class RAMDirectory implements FSEntry, FSDirectory {

	/**	Logger */	
	private static final Logger log = Logger.getLogger(RAMDirectory.class);

	private RAMFileSystem filesystem;
	
	private RAMDirectory parent;
	private String directoryName;
	
	private long lastModified;
	private FSAccessRights accessRights;

	/* if file is deleted, it is no longer valid */
	private boolean isValid = true;
	
	private HashMap<String, FSEntry> entries;
	
	/**
	 * Constructor for a new RAMDirectory
	 * 
	 * @param fs
	 * @param parent
	 * @param name
	 */
	public RAMDirectory(final RAMFileSystem fs, final RAMDirectory parent, final String name) {
		
		this.filesystem = fs;
		this.parent = parent;
		if (this.parent == null){
			this.parent = this;
		}
		this.directoryName = name;
		this.lastModified = System.currentTimeMillis();
		
		// TODO: accessRights
			
		entries = new HashMap<String, FSEntry>();
	}

	/**
	 * (non-Javadoc)
	 * @see org.jnode.fs.FSEntry#getName()
	 */
	public String getName() {
		return directoryName;
	}

	/**
	 * (non-Javadoc)
	 * @see org.jnode.fs.FSEntry#getParent()
	 */
	public FSDirectory getParent() {
		return parent;
	}

	/**
	 * (non-Javadoc)
	 * @see org.jnode.fs.FSEntry#getLastModified()
	 */
	public long getLastModified() throws IOException {
		return lastModified;
	}

	/**
	 * (non-Javadoc)
	 * @see org.jnode.fs.FSEntry#isFile()
	 */
	public boolean isFile() {
		return false;
	}

	/**
	 * (non-Javadoc)
	 * @see org.jnode.fs.FSEntry#isDirectory()
	 */
	public boolean isDirectory() {
		return true;
	}

	/**
	 * (non-Javadoc)
	 * @see org.jnode.fs.FSEntry#setName(java.lang.String)
	 */
	public void setName(String newName) throws IOException {
		// TODO: check for special chars / normalize name
		directoryName = newName;
	}

	/**
	 * (non-Javadoc)
	 * @see org.jnode.fs.FSEntry#setLastModified(long)
	 */
	public void setLastModified(long lastModified) throws IOException {
		this.lastModified = lastModified;
	}

	/**
	 * (non-Javadoc)
	 * @see org.jnode.fs.FSEntry#getFile()
	 */
	public FSFile getFile() throws IOException {
		throw new IOException("Not a file");
	}

	/**
	 * (non-Javadoc)
	 * @see org.jnode.fs.FSEntry#getDirectory()
	 */
	public FSDirectory getDirectory() throws IOException {
		return this;
	}

	/**
	 * (non-Javadoc)
	 * @see org.jnode.fs.FSEntry#getAccessRights()
	 */
	public FSAccessRights getAccessRights() throws IOException {
		return accessRights;
	}

	/**
	 * (non-Javadoc)
	 * @see org.jnode.fs.FSEntry#isDirty()
	 */
	public boolean isDirty() throws IOException {
		return false;
	}

	/**
	 * (non-Javadoc)
	 * @see org.jnode.fs.FSObject#isValid()
	 */
	public boolean isValid() {
		return isValid;
	}

	/**
	 * (non-Javadoc)
	 * @see org.jnode.fs.FSObject#getFileSystem()
	 */
	public FileSystem getFileSystem() {
		return filesystem;
	}

	/**
	 * (non-Javadoc)
	 * @see org.jnode.fs.FSDirectory#iterator()
	 */
	public Iterator<? extends FSEntry> iterator() throws IOException {
		return entries.values().iterator();
	}

	/**
	 * (non-Javadoc)
	 * @see org.jnode.fs.FSDirectory#getEntry(java.lang.String)
	 */
	public FSEntry getEntry(String name) throws IOException {
		return entries.get(name);
	}

	/**
	 * (non-Javadoc)
	 * @see org.jnode.fs.FSDirectory#addFile(java.lang.String)
	 */
	public FSEntry addFile(String name) throws IOException {
		RAMFile file = new RAMFile(this, name);
		entries.put(name, file);
        setLastModified(System.currentTimeMillis());
        return file;
	}

	/**
	 * (non-Javadoc)
	 * @see org.jnode.fs.FSDirectory#addDirectory(java.lang.String)
	 */
	public FSEntry addDirectory(String name) throws IOException {
		RAMDirectory dir = new RAMDirectory(filesystem, this, name);
		entries.put(name, dir);
        setLastModified(System.currentTimeMillis());
        return dir;
	}

	/**
	 * (non-Javadoc)
	 * @see org.jnode.fs.FSDirectory#remove(java.lang.String)
	 */
	public void remove(String name) throws IOException {

		FSEntry entry = entries.remove(name);

		if (entry == null)
			throw new IOException("Entry not found");
		
		if (entry instanceof RAMFile) {
			RAMFile file = (RAMFile) entry;
			file.remove();
            setLastModified(System.currentTimeMillis());
        } else {
			RAMDirectory dir = (RAMDirectory) entry;
			dir.remove();
            setLastModified(System.currentTimeMillis());
        }
	}
	
	/**
	 * removes the directory and all entries inside that directory
	 * 
	 * @throws IOException
	 */
	private void remove() throws IOException {
		Iterator<FSEntry> itr = entries.values().iterator();
		while (itr.hasNext()) {
			FSEntry entry = itr.next();
			if (entry instanceof RAMFile) {
				RAMFile file = (RAMFile) entry;
				file.remove();
			} else {
				RAMDirectory dir = (RAMDirectory) entry;
				dir.remove();
			}
		}
		parent = null;
	}

	/**
	 * (non-Javadoc)
	 * @see org.jnode.fs.FSDirectory#flush()
	 */
	public void flush() throws IOException {
		// nothing todo here
	}
}
