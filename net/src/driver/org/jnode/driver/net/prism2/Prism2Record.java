/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
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
