package org.jnode.fs.hfsplus.catalog;

import org.jnode.fs.hfsplus.HFSPlusForkData;
import org.jnode.fs.hfsplus.HFSUtils;
import org.jnode.util.BigEndian;

public class CatalogFile {
    private byte[] data;

    public CatalogFile(final byte[] src) {
        data = new byte[248];
        System.arraycopy(src, 0, data, 0, 248);
    }

    public final int getRecordType() {
        return BigEndian.getInt16(data, 0);
    }

    public final int getFlags() {
        return BigEndian.getInt16(data, 2);
    }

    public final CatalogNodeId getFileId() {
        return new CatalogNodeId(data, 8);
    }

    public final int getCreateDate() {
        return BigEndian.getInt32(data, 12);
    }

    public final int getContentModDate() {
        return BigEndian.getInt32(data, 16);
    }

    public final int getAttrModDate() {
        return BigEndian.getInt32(data, 20);
    }

    public final HFSPlusForkData getDataFork() {
        return new HFSPlusForkData(data, 88);
    }

    public final HFSPlusForkData getResourceFork() {
        return new HFSPlusForkData(data, 168);
    }

    public final String toString() {
        StringBuffer s = new StringBuffer();
        s.append("Record type:").append(getRecordType()).append("\t");
        s.append("File ID  :").append(getFileId().getId()).append("\n");
        s.append("Creation Date :").append(HFSUtils.printDate(getCreateDate(), "EEE MMM d HH:mm:ss yyyy")).append("\n");
        s.append("Content Mod Date  :").append(HFSUtils.printDate(getContentModDate(), "EEE MMM d HH:mm:ss yyyy"))
                .append("\n");
        s.append("Attr Mod Date  :").append(HFSUtils.printDate(getAttrModDate(), "EEE MMM d HH:mm:ss yyyy")).append(
                "\n");
        return s.toString();
    }
}
