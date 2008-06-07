/*
 *
 */
package org.jnode.fs.jfat;

import java.io.IOException;


/**
 * @author gvt
 */
public class FatDotDirEntry extends FatShortDirEntry {

    private static final byte dot = '.';

    public FatDotDirEntry(FatFileSystem fs, FatMarshal entry, int index) {
        super(fs, entry, index);
        if (!isDirectory())
            throw new UnsupportedOperationException();
    }

    public FatDotDirEntry(FatFileSystem fs, boolean dotDot, FatShortDirEntry parent,
            int startCluster) throws IOException {
        super(fs);
        init(parent, startCluster);
        if (!dotDot) {
            setIndex(0);
        } else {
            setIndex(1);
            lName[1] = dot;
        }
        encodeName();
    }

    private void init(FatShortDirEntry parent, int startCluster) throws IOException {
        setNameCase(new FatCase());
        setAttr(new FatAttr());
        setDirectory();
        setCreated(parent.getCreated());
        clearName();
        lName[0] = dot;
        setLastAccessed(parent.getLastAccessed());
        setLastModified(parent.getLastModified());
        setStartCluster(startCluster);
        setLength(0);
    }
}
