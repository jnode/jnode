/*
 * $Id$
 */
package org.jnode.fs.ntfs;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class IndexRoot extends NTFSStructure {

    public static final int SIZE = 0x10;
    
    /**
     * Initialize this instance.
     * @param attr
     */
    public IndexRoot(IndexRootAttribute attr) {
        super(attr, attr.getAttributeOffset());
    }
    
    /**
     * Gets the attribute type.
     * @return
     */
    public int getAttributeType() {
        return getUInt32AsInt(0x00);
    }
    
    /**
     * Gets the collation rule.
     * @return
     */
    public int getCollationRule() {
        return getUInt32AsInt(0x04);
    }
    
    /**
     * Size of each index block in bytes (in the index allocation attribute).
     * @return
     */
    public int getIndexBlockSize() {
        return getUInt32AsInt(0x08);
    }
    
    /**
     * Gets the number of clusters per index record.
     * @return
     */
    public int getClustersPerIndexBlock() {
        final int v = getInt8(0x0C);
        if (v < 0) {
            return 1;
        } else {
            return v;
        }
    }   
}
