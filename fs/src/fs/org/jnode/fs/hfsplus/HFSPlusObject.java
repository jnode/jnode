package org.jnode.fs.hfsplus;

import org.jnode.fs.FSObject;
import org.jnode.fs.FileSystem;
import org.jnode.fs.nfs.nfs2.NFS2FileSystem;

public class HFSPlusObject implements FSObject {

	private HfsPlusFileSystem fs;
	
	public HFSPlusObject(HfsPlusFileSystem fileSystem) {
		this.fs = fileSystem;
	}

	public FileSystem getFileSystem() {
		return fs;
	}

	public boolean isValid() {
		return false;
	}
}
