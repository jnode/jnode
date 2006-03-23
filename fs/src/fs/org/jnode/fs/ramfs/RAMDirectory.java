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

public class RAMDirectory implements FSEntry, FSDirectory {

	/**	Logger*/	
	private static final Logger log = Logger.getLogger(RAMDirectory.class);

	private RAMFileSystem filesystem;
	
	private RAMDirectory parent;
	private String directoryName;
	
	private long lastModified;
	private FSAccessRights accessRights;

	/* if file is deleted, it is no longer valid */
	private boolean isValid = true;
	
	private HashMap<String, FSEntry> entries;
	
	public RAMDirectory(RAMFileSystem fs, RAMDirectory parent, String name) {
		
		this.filesystem = fs;
		
		if (parent == null)
			parent = this;
		else
			this.parent = parent;
		
		this.directoryName = name;
		this.lastModified = System.currentTimeMillis();
		
		// TODO: accessRights
			
		entries = new HashMap<String, FSEntry>();
	}

	public String getName() {
		return directoryName;
	}

	public FSDirectory getParent() {
		return parent;
	}

	public long getLastModified() throws IOException {
		return lastModified;
	}

	public boolean isFile() {
		return false;
	}

	public boolean isDirectory() {
		return true;
	}

	public void setName(String newName) throws IOException {
		// TODO: check for special chars
		directoryName = newName;
	}

	public void setLastModified(long lastModified) throws IOException {
		this.lastModified = lastModified;
	}

	public FSFile getFile() throws IOException {
		throw new IOException("Not a file");
	}

	public FSDirectory getDirectory() throws IOException {
		return this;
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
		return filesystem;
	}

	public Iterator<? extends FSEntry> iterator() throws IOException {
		return entries.values().iterator();
	}

	public FSEntry getEntry(String name) throws IOException {
		//log.debug("GetEntry for: " + name);
		return entries.get(name);
	}

	public FSEntry addFile(String name) throws IOException {

		//log.debug("AddFile to directory " + directoryName + ". Filename is " + name);

		RAMFile file = new RAMFile(this, name);
		entries.put(name, file);
		return file;
	}

	public FSEntry addDirectory(String name) throws IOException {

		//log.debug("AddDirectory with name " + name);

		RAMDirectory dir = new RAMDirectory(filesystem, this, name);
		entries.put(name, dir);
		return dir;
	}

	public void remove(String name) throws IOException {

		FSEntry entry = entries.remove(name);

		if (entry == null)
			throw new IOException("Entry not found");
		
		if (entry instanceof RAMFile) {
			RAMFile file = (RAMFile) entry;
			file.remove();
		} else {
			RAMDirectory dir = (RAMDirectory) entry;
			dir.remove();
		}
	}
	
	public void remove() throws IOException {
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

	public void flush() throws IOException {
		// nothing todo here
	}
}
