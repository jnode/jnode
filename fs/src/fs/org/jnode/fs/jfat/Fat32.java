/*
 *
 */
package org.jnode.fs.jfat;

import java.io.IOException;

import org.jnode.driver.block.BlockDeviceAPI;


/**
 * @author gvt
 */
public class Fat32 extends Fat {
    protected Fat32(BootSector bs, BlockDeviceAPI api) {
        super(bs, api);
    }

    protected long offset(int index) {
        return (long) (4 * index);
    }

    public int get(int index) throws IOException {
        return (int) (getUInt32(index) & 0x0FFFFFFF);
    }

    public int set(int index, int element) throws IOException {
        long old = getUInt32(index);

        setInt32(index, (int) ((element & 0x0FFFFFFF) | (old & 0xF0000000)));

        return (int) (old & 0x0FFFFFFF);
    }

    public boolean isEofChain(int entry) {
        return (entry >= 0x0FFFFFF8);
    }

    public int eofChain() {
        return 0x0FFFFFFF;
    }
}
