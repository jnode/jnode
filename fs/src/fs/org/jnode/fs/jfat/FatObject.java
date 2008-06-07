/*
 *
 */
package org.jnode.fs.jfat;

import org.jnode.fs.spi.AbstractFSObject;


/**
 * @author gvt
 */
public abstract class FatObject extends AbstractFSObject {
    public FatObject(FatFileSystem fs) {
        super(fs);
    }

    public final FatFileSystem getFatFileSystem() {
        return (FatFileSystem) getFileSystem();
    }
}
