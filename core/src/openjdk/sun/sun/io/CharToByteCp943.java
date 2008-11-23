/*
 * Copyright 1997-2003 Sun Microsystems, Inc.  All Rights Reserved.
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
// Table from Unicode to Cp943
package sun.io;

import sun.nio.cs.ext.IBM943;

/**
 * Tables and data to convert Unicode to Cp943
 *
 * @author  BuildTables tool
 */

public class CharToByteCp943 extends CharToByteDBCS_ASCII {

    private static IBM943 nioCoder = new IBM943();

    public String getCharacterEncoding() {
        return "Cp943";
    }

    public CharToByteCp943() {
        super();
        super.mask1 = 0xFFC0;
        super.mask2 = 0x003F;
        super.shift = 6;
        super.index1 = nioCoder.getEncoderIndex1();
        super.index2 = nioCoder.getEncoderIndex2();
        super.index2a = nioCoder.getEncoderIndex2a();
        subBytes = new byte[1];
        subBytes[0] = 0x6f;
    }
}
