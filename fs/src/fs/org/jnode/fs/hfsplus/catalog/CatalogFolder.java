package org.jnode.fs.hfsplus.catalog;

import org.jnode.util.BigEndian;

public class CatalogFolder {
    private byte[] data;

    public CatalogFolder(final byte[] src) {
        data = new byte[88];
        System.arraycopy(src, 0, data, 0, 88);
    }

    public final int getRecordType() {
        return BigEndian.getInt16(data, 0);
    }

    public final int getValence() {
        return BigEndian.getInt32(data, 4);
    }

    public final CatalogNodeId getFolderId() {
        return new CatalogNodeId(data, 8);
    }

    public final String toString() {
        StringBuffer s = new StringBuffer();
        s.append("Record type: ").append(getRecordType()).append("\n");
        s.append("Valence: ").append(getValence()).append("\n");
        s.append("Folder ID: ").append(getFolderId().getId()).append("\n");
        return s.toString();
    }
}
