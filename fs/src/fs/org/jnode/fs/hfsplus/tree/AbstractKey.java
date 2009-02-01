package org.jnode.fs.hfsplus.tree;

import org.jnode.fs.hfsplus.catalog.CatalogNodeId;

public abstract class AbstractKey implements Key {
    
    protected int keyLength;
    protected CatalogNodeId parentID;
    
    public abstract int getKeyLength();
    
    public abstract byte[] getBytes();

    public abstract int compareTo(Key key);
}
