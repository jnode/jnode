/*
 * $Id: header.txt 5714 2010-01-03 13:33:07Z lsantha $
 *
 * Copyright (C) 2003-2012 JNode.org
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
 * @see sun.font.NullFontScaler
 */
class NativeNullFontScaler {
    /**
     * @see sun.font.NullFontScaler#getNullScalerContext()
     */
    private static long getNullScalerContext() {
        //todo implement it
        return 0;
    }
    /**
     * @see sun.font.NullFontScaler#getGlyphImage(long, int)
     */
    private static long getGlyphImage(NullFontScaler instance, long arg1, int arg2) {
        //todo implement it
        return 0;
    }
}
