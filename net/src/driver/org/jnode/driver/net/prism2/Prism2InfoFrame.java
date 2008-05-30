/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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

import org.jnode.util.LittleEndian;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class Prism2InfoFrame implements Prism2Constants {

    /**
     * Length of the header of an Info frame.
     */
    final static int HDR_LENGTH = 4;

    /**
     * Maximum lenght on an Info frame
     */
    final static int MAX_FRAME_LEN = BAP_DATALEN_MAX;

    /**
     * Gets the framelength of an Info frame.
     *
     * @param src
     * @param srcOffset
     * @return The frame length in bytes (including the length of the header)
     */
    public static final int getFrameLength(byte[] src, int srcOffset) {
        return (LittleEndian.getInt16(src, srcOffset) + 1) * 2;
    }

    /**
     * Gets the infotype of an Info frame.
     *
     * @param src
     * @param srcOffset
     * @return
     */
    public static final InformationType getInfoType(byte[] src, int srcOffset) {
        return InformationType.getByValue(LittleEndian.getInt16(src, srcOffset + 2) & 0xFFFF);
    }

    /**
     * Gets the link status of an IT_LINKSTATUS frame.
     *
     * @param src
     * @param srcOffset
     * @return
     */
    public static final LinkStatus getLinkStatus(byte[] src, int srcOffset) {
        return LinkStatus.getByValue(LittleEndian.getInt16(src, srcOffset + HDR_LENGTH) & 0xFFFF);
    }
}
