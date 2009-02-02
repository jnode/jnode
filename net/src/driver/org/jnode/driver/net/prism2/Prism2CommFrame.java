/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.driver.net.prism2;

import org.jnode.net.ethernet.EthernetAddress;
import org.jnode.net.wireless.WirelessConstants;
import org.jnode.util.LittleEndian;

/**
 * Access class for communication frames.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class Prism2CommFrame implements Prism2Constants {

    /**
     * Length of the header of an communication frame.
     */
    static final int HDR_LENGTH = 60;

    /**
     * Offset in this frame of the 802.11 header
     */
    static final int p80211HDR_OFF = 14;

    /**
     * Offset in this frame of the 802.3 header
     */
    static final int p8023HDR_OFF = 46;

    /**
     * Maximum lenght on an communication frame
     */
    static final int MAX_FRAME_LEN = BAP_DATALEN_MAX;

    /**
     * Maximum length of a transmit buffer
     */
    static final int MAX_TXBUF_LEN =
            HDR_LENGTH + WirelessConstants.WLAN_DATA_MAXLEN - WirelessConstants.WLAN_WEP_IV_LEN -
                    WirelessConstants.WLAN_WEP_ICV_LEN + 2;

    /**
     * Gets the status field of a comm frame.
     * 
     * @param src
     * @param srcOffset
     * @return
     */
    public static final int getStatus(byte[] src, int srcOffset) {
        return LittleEndian.getUInt16(src, srcOffset);
    }

    /**
     * Gets the datalength field of a comm frame.
     * 
     * @param src
     * @param srcOffset
     * @return
     */
    public static final int getDataLength(byte[] src, int srcOffset) {
        return LittleEndian.getUInt16(src, srcOffset + 58);
    }

    /**
     * Sets the TxControl field.
     * 
     * @param dst
     * @param dstOffset
     * @param txControl
     */
    public static final void setTxControl(byte[] dst, int dstOffset, int txControl) {
        LittleEndian.setInt16(dst, dstOffset + 12, txControl);
    }

    /**
     * Sets the Address1 field.
     * 
     * @param dst
     * @param dstOffset
     * @param addr
     */
    public static final void setAddress1(byte[] dst, int dstOffset, EthernetAddress addr) {
        addr.writeTo(dst, dstOffset + 18);
    }

    /**
     * Sets the Address2 field.
     * 
     * @param dst
     * @param dstOffset
     * @param addr
     */
    public static final void setAddress2(byte[] dst, int dstOffset, EthernetAddress addr) {
        addr.writeTo(dst, dstOffset + 24);
    }

    /**
     * Sets the Address3 field.
     * 
     * @param dst
     * @param dstOffset
     * @param addr
     */
    public static final void setAddress3(byte[] dst, int dstOffset, EthernetAddress addr) {
        addr.writeTo(dst, dstOffset + 30);
    }
}
