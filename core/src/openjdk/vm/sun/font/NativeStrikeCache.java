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
 
package sun.font;

/**
 * @see sun.font.StrikeCache
 * @author Levente S\u00e1ntha
 */
class NativeStrikeCache {
    /**
     * @see sun.font.StrikeCache#getGlyphCacheDescription(long[])
     */
    private static void getGlyphCacheDescription(long[] arg1) {
        arg1[0] = 4; // nativeAddressSize

		//TODO find right values
        arg1[1] = 0; // glyphInfoSize
        arg1[2] = 0; // xAdvanceOffset
        arg1[3] = 0; // yAdvanceOffset
        arg1[4] = 10; // widthOffset
        arg1[5] = 10; // heightOffset
        arg1[6] = 0; // rowBytesOffset
        arg1[7] = 0; // topLeftXOffset
        arg1[8] = 0; // topLeftYOffset
        arg1[9] = 0; // pixelDataOffset
        arg1[10] = 0; // invisibleGlyphPtr
    }
    /**
     * @see sun.font.StrikeCache#freeIntPointer(int)
     */
    private static void freeIntPointer(int arg1) {
        //todo implement it
    }
    /**
     * @see sun.font.StrikeCache#freeLongPointer(long)
     */
    private static void freeLongPointer(long arg1) {
        //todo implement it
    }
    /**
     * @see sun.font.StrikeCache#freeIntMemory(int[], long)
     */
    private static void freeIntMemory(int[] arg1, long arg2) {
        //todo implement it
    }
    /**
     * @see sun.font.StrikeCache#freeLongMemory(long[], long)
     */
    private static void freeLongMemory(long[] arg1, long arg2) {
        //todo implement it
    }
}
