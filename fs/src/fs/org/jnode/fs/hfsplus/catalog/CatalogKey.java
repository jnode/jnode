package org.jnode.fs.hfsplus.catalog;

import org.jnode.fs.hfsplus.HFSUnicodeString;
import org.jnode.fs.hfsplus.tree.AbstractKey;
import org.jnode.fs.hfsplus.tree.Key;
import org.jnode.util.BigEndian;

public class CatalogKey extends AbstractKey {

    public static final int MAXIMUM_KEY_LENGTH = 516;

    private int keyLength;
    private CatalogNodeId parentID;
    private HFSUnicodeString nodeName;

    /**
     * 
     * @param src
     * @param offset
     */
    public CatalogKey(final byte[] src, final int offset) {
        byte[] ck = new byte[2];
        System.arraycopy(src, offset, ck, 0, 2);
        keyLength = BigEndian.getInt16(ck, 0);
        ck = new byte[4];
        System.arraycopy(src, offset + 2, ck, 0, 4);
        parentID = new CatalogNodeId(ck, 0);
        if (keyLength > 6) {
            nodeName = new HFSUnicodeString(src, offset + 6);
        }
    }

    /**
     * 
     * @param parentID
     * @param name
     */
    public CatalogKey(final CatalogNodeId parentID, final HFSUnicodeString name) {
        this.parentID = parentID;
        this.nodeName = name;
        this.keyLength = 6 + name.getLength();
    }

    public final int getKeyLength() {
        return keyLength;
    }

    public final int getLength() {
        return keyLength;
    }

    public final CatalogNodeId getParentId() {
        return parentID;
    }

    public final HFSUnicodeString getNodeName() {
        return nodeName;
    }

    public final int compareTo(final Key o) {
        if (o instanceof CatalogKey) {
            CatalogKey ck = (CatalogKey) o;
            if (getParentId().getId() == ck.getParentId().getId()) {
                return nodeName.getUnicodeString().compareTo(
                        ck.getNodeName().getUnicodeString());
            } else if (getParentId().getId() < ck.getParentId().getId()) {
                return -1;
            } else {
                return 1;
            }
        } else {
            return -1;
        }
    }

    public final String toString() {
        StringBuffer s = new StringBuffer();
        s.append("Key length: ").append(getKeyLength()).append(" ");
        s.append("Parent ID: ").append(getParentId().getId()).append(" ");
        s.append("Node name: ")
                .append(
                        (getNodeName() != null) ? getNodeName()
                                .getUnicodeString() : "");
        return s.toString();
    }
}
