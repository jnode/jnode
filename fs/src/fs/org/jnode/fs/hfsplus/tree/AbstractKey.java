package org.jnode.fs.hfsplus.tree;

public abstract class AbstractKey implements Key {
    public abstract int getKeyLength();

    public abstract int getLength();

    public abstract int compareTo(Key key);
}
