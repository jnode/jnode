package org.jnode.fs.jfat;

import org.jnode.util.BinaryPrefix;


public enum ClusterSize {
    _1Kb(1),
    _2Kb(2),
    _4Kb(4),
    _8Kb(8),
    _16Kb(16),
    _32Kb(32),
    _64Kb(64);

    private final int size;

    private ClusterSize(int sizeInKb)
    {
    	size = (int) (sizeInKb * BinaryPrefix.K.getMultiplier()); //Converted into KB
    }

    final public int getSize()
    {
        return size;
    }
}
