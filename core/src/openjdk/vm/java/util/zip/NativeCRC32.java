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

package java.util.zip;

/**
 * Implementation of java.util.zip.CRC32 native methods.
 *
 * @author Chris Boertien
 * @date Mar 24, 2009
 */ 
public class NativeCRC32 {
    
    /** The fast CRC table. Computed once when the CRC32 class is loaded. */
    private static final int[] fastCRCTable = fastCRCTable();
    
    /** Make the table for a fast CRC. */
    private static int[] fastCRCTable() {
        int[] crc_table = new int[256];
        for (int n = 0; n < 256; n++) {
            int c = n;
            for (int k = 8; --k >= 0;) {
                if ((c & 1) != 0)
                    c = 0xedb88320 ^ (c >>> 1);
                else
                    c = c >>> 1;
            }
            crc_table[n] = c;
        }
        return crc_table;
    }

    private static int update( int crc , int b ) {
        int c = ~crc;
        c = fastCRCTable[(c^b) & 0xff] ^ (c >>> 8);
        return ~c;
    }
    
    private static int updateBytes( int crc , byte[] b , int off , int len ) {
        int c = ~crc;
        while (--len >= 0)
            c = fastCRCTable[(c ^ b[off++]) & 0xff] ^ (c >>> 8);
        return ~c;
    }
}
