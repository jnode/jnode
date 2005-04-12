/*
 * $Id$
 */
package org.jnode.driver.net.prism2;

import org.jnode.util.LittleEndian;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class Prism2InfoFrame implements Prism2Constants {

    final static int HDR_LENGTH = 4;
    
    /** Maximum lenght on an Info frame */
    final static int MAX_FRAME_LEN = BAP_DATALEN_MAX;

    /**
     * Gets the framelength of an Info frame.
     * @param src
     * @param srcOffset
     * @return The frame length in bytes (including the length of the header)
     */
    public static final int getFrameLength(byte[] src, int srcOffset) {
        return (LittleEndian.getInt16(src, srcOffset) + 1) * 2;
    }

    /**
     * Gets the infotype of an Info frame.
     * @param src
     * @param srcOffset
     * @return
     */
    public static final int getInfoType(byte[] src, int srcOffset) {
        return LittleEndian.getInt16(src, srcOffset + 2) & 0xFFFF;
    }
    
    /**
     * Gets the link status of an IT_LINKSTATUS frame.
     * @param src
     * @param srcOffset
     * @return
     */
    public static final int getLinkStatus(byte[] src, int srcOffset) {
        return LittleEndian.getInt16(src, srcOffset + HDR_LENGTH) & 0xFFFF;
    }
}
