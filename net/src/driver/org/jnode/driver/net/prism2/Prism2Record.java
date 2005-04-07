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
     * Gets the RID from a given record.
     * @param src
     * @param srcOfs
     * @return
     */
    final static int getRecordRID(byte[] src, int srcOfs) {
        return LittleEndian.getInt16(src, srcOfs + 2);
    }

}
