package org.jnode.fs.hfsplus;

import org.jnode.fs.FSObject;

public class HFSPlusObject implements FSObject {
    protected HfsPlusFileSystem fs;

    public HFSPlusObject(final HfsPlusFileSystem fileSystem) {
        this.fs = fileSystem;
    }

    public final HfsPlusFileSystem getFileSystem() {
        return fs;
    }

    public final boolean isValid() {
        return false;
    }
}
