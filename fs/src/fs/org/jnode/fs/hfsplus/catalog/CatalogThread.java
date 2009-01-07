package org.jnode.fs.hfsplus.catalog;

import org.jnode.fs.hfsplus.HFSUnicodeString;
import org.jnode.util.BigEndian;

public class CatalogThread {
    private byte[] data;

    public CatalogThread(final byte[] src) {
        data = new byte[512];
        System.arraycopy(src, 0, data, 0, 512);
    }

    /**
     * Create a new catalog thread.
     * 
     * @param type
     * @param parent
     * @param name
     */
    public CatalogThread(int type, CatalogNodeId parent, HFSUnicodeString name) {
        data = new byte[512];
        BigEndian.setInt16(data, 0, type);
        BigEndian.setInt32(data, 4, parent.getId());
        System.arraycopy(parent.getBytes(), 0, data, 4, 4);
        System.arraycopy(name.getBytes(), 0, data, 8, name.getBytes().length);
    }

    public final int getRecordType() {
        return BigEndian.getInt16(data, 0);
    }

    public final CatalogNodeId getParentId() {
        return new CatalogNodeId(data, 4);
    }

    public final HFSUnicodeString getNodeName() {
        return new HFSUnicodeString(data, 8);
    }
}
