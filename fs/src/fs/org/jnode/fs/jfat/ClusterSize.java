package org.jnode.fs.jfat;

import org.jnode.util.BinaryScaleFactor;


public enum ClusterSize {
    _1Kb(1), _2Kb(2), _4Kb(4), _8Kb(8), _16Kb(16), _32Kb(32), _64Kb(64);

    private final int size;

    private ClusterSize(int sizeInKb) {
        size = (int) (sizeInKb * BinaryScaleFactor.K.getMultiplier()); //Converted into KB
    }

    public final int getSize() {
        return size;
    }
}
