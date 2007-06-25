/*
 * Copyright 1996-1998 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package sun.io;

/**
 * @author Limin Shi
 * @author Mark Son-Bell
 */

public class CharToByteSJIS extends CharToByteJIS0208 {
    CharToByteJIS0201 cbJIS0201 = new CharToByteJIS0201();

    public String getCharacterEncoding() {
        return "SJIS";
    }

    protected int convSingleByte(char inputChar, byte[] outputByte) {
	byte b;

	// \u0000 - \u007F map straight through
	if ((inputChar & 0xFF80) == 0) {
	    outputByte[0] = (byte)inputChar;
	    return 1;
	}

	if ((b = cbJIS0201.getNative(inputChar)) == 0)
	    return 0;

	outputByte[0] = b;
	return 1;
    }

    protected int getNative(char ch) {
	int offset = index1[ch >> 8] << 8;
	int pos = index2[offset >> 12].charAt((offset & 0xfff) + (ch & 0xff));
        if (pos == 0) {
            /* Zero value indicates this Unicode has no mapping to JIS0208.
             * We bail here because the JIS -> SJIS algorithm produces
             * bogus SJIS values for invalid JIS input.  Zero should be the
             * only invalid JIS value in our table.
             */
            return 0;
        }
        /*
         * This algorithm for converting from JIS to SJIS comes from Ken Lunde's
         * "Understanding Japanese Information Processing", pg 163.
         */
        int c1 = (pos >> 8) & 0xff;
        int c2 = pos & 0xff;
        int rowOffset = c1 < 0x5F ? 0x70 : 0xB0;
        int cellOffset = (c1 % 2 == 1) ? (c2 > 0x5F ? 0x20 : 0x1F) : 0x7E;
        return ((((c1 + 1 ) >> 1) + rowOffset) << 8) | (c2 + cellOffset);
    }
}
