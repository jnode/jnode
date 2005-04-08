/*
 * $Id$
 */
package org.jnode.driver.net.prism2;

import org.jnode.util.LittleEndian;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class Prism2Record {
    
    final static int HDR_LENGTH = 4;
    
    /**
     * Gets the record length from a given record.
     * @param src
     * @param srcOfs
     * @return
     */
    final static int getRecordLength(byte[] src, int srcOfs) {
        return LittleEndian.getInt16(src, srcOfs + 0);
    }
    
    /**
     * Sets the record length from a given record.
     * @param dst
     * @param dstOfs
     * @return
     */
    final static void setRecordLength(byte[] dst, int dstOfs, int recordLength) {
        LittleEndian.setInt16(dst, dstOfs + 0, recordLength);
    }
    
    /**
     * Gets the RID from a given record.
     * @param src
     * @param srcOfs
     * @return
     */
    final static int getRecordRID(byte[] src, int srcOfs) {
        return LittleEndian.getInt16(src, srcOfs + 2);
    }

    /**
     * Sets the RID from a given record.
     * @param dst
     * @param dstOfs
     * @return
     */
    final static void setRecordRID(byte[] dst, int dstOfs, int rid) {
        LittleEndian.setInt16(dst, dstOfs + 2, rid);
    }

}
