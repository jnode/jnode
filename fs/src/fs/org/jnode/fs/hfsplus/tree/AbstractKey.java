package org.jnode.fs.hfsplus.tree;

public abstract class AbstractKey implements Key {
    
    public abstract int getKeyLength();
    
    public abstract byte[] getBytes();

    public abstract int compareTo(Key key);
}
