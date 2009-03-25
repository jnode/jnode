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
 
public class NativeZipEntry implements ZipConstants {
    
    private static void initFields( ZipEntry _this , long jzentry ) {
        int zipKey           = ZipUtil.high32(jzentry);
        int entryKey         = ZipUtil.low32(jzentry);
        ZipEntryStruct entry = StructCache.getEntryStruct(zipKey,entryKey);
        
        _this.name      = entry.name;
        _this.time      = entry.time;
        _this.crc       = entry.crc;
        _this.csize     = entry.csize;
        _this.size      = entry.size;
        _this.method    = entry.method;
        _this.extra     = entry.extra;
        _this.comment   = entry.comment;
    }
    
    public static void initIDs() {}
}
