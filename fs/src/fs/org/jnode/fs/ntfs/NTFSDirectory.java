package org.jnode.fs.ntfs;

import java.io.IOException;
import java.util.Iterator;

import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FileSystem;

/**
 * @author vali
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class NTFSDirectory implements FSDirectory {

	/* (non-Javadoc)
	 * @see org.jnode.fs.FSDirectory#iterator()
	 */
	public Iterator iterator() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.jnode.fs.FSDirectory#getEntry(java.lang.String)
	 */
	public FSEntry getEntry(String name) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.jnode.fs.FSDirectory#addFile(java.lang.String)
	 */
	public FSEntry addFile(String name) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.jnode.fs.FSDirectory#addDirectory(java.lang.String)
	 */
	public FSEntry addDirectory(String name) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.jnode.fs.FSDirectory#remove(java.lang.String)
	 */
	public void remove(String name) throws IOException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.jnode.fs.FSObject#isValid()
	 */
	public boolean isValid() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.jnode.fs.FSObject#getFileSystem()
	 */
	public FileSystem getFileSystem() {
		// TODO Auto-generated method stub
		return null;
	}

}
