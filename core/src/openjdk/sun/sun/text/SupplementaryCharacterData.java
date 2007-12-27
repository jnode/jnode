/*
 * Copyright 2003 Sun Microsystems, Inc.  All Rights Reserved.
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

package sun.text;

/**
 * SupplementaryCharacterData is an SMI-private class which was written for
 * RuleBasedBreakIterator and BreakDictionary.
 */
public final class SupplementaryCharacterData implements Cloneable {

    /**
     * An array for supplementary characters and values.
     * Lower one byte is used to keep a byte-value.
     * Upper three bytes are used to keep the first supplementary character
     * which has the value. The value is also valid for the following
     * supplementary characters until the next supplementary character in
     * the array <code>dataTable</code>.
     * For example, if the value of <code>dataTable[2]</code> is
     * <code>0x01000123</code> and the value of <code>dataTable[3]</code> is
     * <code>0x01000567</code>, supplementary characters from
     * <code>0x10001</code> to <code>0x10004</code> has the value
     * <code>0x23</code>. And, <code>getValue(0x10003)</code> returns the value.
     */
    private int[] dataTable;


    /**
     * Creates a new SupplementaryCharacterData object with the given table.
     */
    public SupplementaryCharacterData(int[] table) {
        dataTable = table;
    }

    /**
     * Returns a corresponding value for the given supplementary code-point.
     */
    public int getValue(int index) {
        // Index should be a valid supplementary character.
        assert index >= Character.MIN_SUPPLEMENTARY_CODE_POINT &&
               index <= Character.MAX_CODE_POINT :
               "Invalid code point:" + Integer.toHexString(index);

        int i = 0;
        int j = dataTable.length - 1;
        int k;

        for (;;) {
            k = (i + j) / 2;

            int start = dataTable[k] >> 8;
            int end   = dataTable[k+1] >> 8;

            if (index < start) {
                j = k;
            } else if (index > (end-1)) {
                i = k;
            } else {
                return dataTable[k] & 0xFF;
            }
        }
    }
 
    /**
     * Returns the data array.
     */
    public int[] getArray() {
        return dataTable;
    }

}
