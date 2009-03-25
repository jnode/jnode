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
 * @author Chris Boertien
 * @date Mar 24, 2009
 */
 
class ZipUtil implements ZipConstants {
    
// Bit Manipulation

    static long ints2long( int high , int low ) {
        return ((high & 0xFFFFFFFFL) << 32) | (low & 0xFFFFFFFFL);
    }
    
    static int low32( long num ) {
        return (int)(num & 0xFFFFFFFFL);
    }
    
    static int high32( long num ) {
        return (int)((num >>> 32) & 0xFFFFFFFFL);
    }
    
    static int low31( long num ) {
        return (int)(num & 0x7FFFFFFFL);
    }
    
}
