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

package	sun.io;

/**
 * The SJIS converters follow the Shift JIS definition in the JIS
 * X0208:1997 standard as much as possible. The MS932 converters must
 * be used where the Microsoft Code Page 932 character repertoire and
 * Unicode mappings are required.
 *
 * @author Limin Shi
 * @author Mark Son-Bell
 */

public class ByteToCharSJIS extends ByteToCharJIS0208 {
    ByteToCharJIS0201 bcJIS0201 = new ByteToCharJIS0201();

    public String getCharacterEncoding() {
        return "SJIS";
    }

    protected char convSingleByte(int b) {
        // If the high bits are all off, it's ASCII == Unicode
        if ((b & 0xFF80) == 0) {
	    return (char)b;
        }
	return bcJIS0201.getUnicode(b);
    }

    protected char getUnicode(int c1, int c2) {
	int adjust = c2 < 0x9F ? 1 : 0;
	int rowOffset = c1 < 0xA0 ? 0x70 : 0xB0;
	int cellOffset = (adjust == 1) ? (c2 > 0x7F ? 0x20 : 0x1F) : 0x7E;
	int b1 = ((c1 - rowOffset) << 1) - adjust;
	int b2 = c2 - cellOffset;
	return super.getUnicode(b1, b2);
    }

    String prt(int i) {
	return Integer.toString(i,16);
    }
}
