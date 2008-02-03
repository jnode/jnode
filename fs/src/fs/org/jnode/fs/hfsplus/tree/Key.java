package org.jnode.fs.hfsplus.tree;

public interface Key extends Comparable<Key> {
	public int getKeyLength();
	public int getLength();
}
