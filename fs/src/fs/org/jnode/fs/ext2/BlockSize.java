package org.jnode.fs.ext2;

import org.jnode.util.BinaryScaleFactor;

public enum BlockSize {
    _1Kb(1), _2Kb(2), _4Kb(4);

    private final int size;

    private BlockSize(int blockSizeKb) {
        /* The size is converted to kilobytes */
        this.size = (int) (blockSizeKb * BinaryScaleFactor.K.getMultiplier()); 
    }

    public final int getSize() {
        return size;
    }
}
