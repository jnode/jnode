package org.jnode.fs.hfsplus.catalog;

import org.jnode.util.BigEndian;

public class CatalogNodeId {
    private byte[] cnid;

    public CatalogNodeId(final byte[] src, final int offset) {
        cnid = new byte[4];
        System.arraycopy(src, offset, cnid, 0, 4);
    }

    public CatalogNodeId(final int nodeId) {
        cnid = new byte[4];
        BigEndian.setInt32(cnid, 0, nodeId);
    }

    /* Parent Of the Root */
    public static final CatalogNodeId HFSPLUS_POR_CNID = new CatalogNodeId(1);
    /* ROOT directory */
    public static final CatalogNodeId HFSPLUS_ROOT_CNID = new CatalogNodeId(2);
    /* EXTents B-tree */
    public static final CatalogNodeId HFSPLUS_EXT_CNID = new CatalogNodeId(3);
    /* CATalog B-tree */
    public static final CatalogNodeId HFSPLUS_CAT_CNID = new CatalogNodeId(4);
    /* BAD blocks file */
    public static final CatalogNodeId HFSPLUS_BAD_CNID = new CatalogNodeId(5);
    /* ALLOCation file */
    public static final CatalogNodeId HFSPLUS_ALLOC_CNID = new CatalogNodeId(6);
    /* STARTup file */
    public static final CatalogNodeId HFSPLUS_START_CNID = new CatalogNodeId(7);
    /* ATTRibutes file */
    public static final CatalogNodeId HFSPLUS_ATTR_CNID = new CatalogNodeId(8);
    /* ExchangeFiles temp id */
    public static final CatalogNodeId HFSPLUS_EXCH_CNID = new CatalogNodeId(15);
    /* first available user id */
    public static final CatalogNodeId HFSPLUS_FIRSTUSER_CNID = new CatalogNodeId(16);

    public final int getId() {
        return BigEndian.getInt32(cnid, 0);
    }

}
