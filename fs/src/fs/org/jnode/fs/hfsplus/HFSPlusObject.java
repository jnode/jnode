package org.jnode.fs.hfsplus;

import org.jnode.fs.FSObject;

public class HFSPlusObject implements FSObject {

	private HfsPlusFileSystem fs;
	
	public HFSPlusObject(HfsPlusFileSystem fileSystem) {
		this.fs = fileSystem;
	}

	public HfsPlusFileSystem getFileSystem() {
		return fs;
	}

	public boolean isValid() {
		return false;
	}
}
