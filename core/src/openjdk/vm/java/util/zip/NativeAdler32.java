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
 * Implementation of java.util.zip.Adler32 native methods.
 *
 * @author Chris Boertien
 * @date Mar 24, 2009
 */

public class NativeAdler32 {
    
    private static int update( int adler , int b ) {
        int s1 = adler & 0xffff;
        int s2 = adler >>> 16;
        
        s1 = (s1 + (b & 0xFF)) % 65521;
        s2 = (s1 + s2) % 65521;
        
        return (s2 << 16) + s1;
    }
    
    private static int updateBytes( int adler , byte[] b , int off , int len ) {
        int n;
        int s1 = adler & 0xffff;
        int s2 = adler >>> 16;

        while (len > 0) {
	        n = 3800;
	        if (n > len) n = len;
	        len -= n;
            while (--n >= 0) {
                s1 = s1 + (b[off++] & 0xFF);
                s2 = s2 + s1;
            }
            s1 %= 65521;
            s2 %= 65521;
        }

        return (s2 << 16) | s1;
    }
}
