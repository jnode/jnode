package org.jnode.fs.ext2;

import org.jnode.util.BinaryPrefix;


public enum BlockSize {
    _1Kb(1),
    _2Kb(2),
    _4Kb(4);

    private final int size;

    private BlockSize(int blockSizeKb)
    {
    	this.size = (int) (blockSizeKb * BinaryPrefix.K.getMultiplier()); //Converted into KB
    }

	final public int getSize() {
		return size;
	}
}
