package org.jnode.fs.hfsplus.tree;

public interface Key extends Comparable<Key> {
    int getKeyLength();

    int getLength();
}
