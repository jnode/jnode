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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class NativeZipFile implements ZipConstants {

    public static long open( String name , int mode , long lastModified ) throws IOException {
        File file = new File(name);
        
        if(!file.exists()) throw new FileNotFoundException();
        if(!file.isFile()) throw new IllegalArgumentException("java.util.zip: Attempt to open non-file for reading: "+name);
        
        long key = StructCache.getFileStructKey(file,mode,lastModified);
        if(key == 0) throw new ZipException("java.util.zip: Problem reading file: "+name);
        return key;
    }
    
    private static void close( long jzfile ) {
        int zipKey = ZipUtil.low32(jzfile);
        StructCache.releaseFileStructKey(zipKey);
    }
    
    public static int getTotal( long jzfile ) {
        int zipKey = ZipUtil.low32(jzfile);
        return StructCache.getFileStruct(zipKey).totalEntries;
    }
    
    public static long getEntry( long jzfile , String name , boolean addSlash ) {
        int zipKey = ZipUtil.low32(jzfile);
        if(!StructCache.hasFileStruct(zipKey)) return 0;
        
        int entryKey;
        int entryKeySlash;
        ZipEntryStruct entry;
        
        entryKey = name.hashCode();
        entryKeySlash = (name+"/").hashCode();
        
        if(StructCache.hasEntryStruct(zipKey,entryKey)) {
            StructCache.getEntryStruct(zipKey,entryKey).refs++;
            return ZipUtil.ints2long(zipKey,entryKey);
        }
        
        if(addSlash && StructCache.hasEntryStruct(zipKey,entryKeySlash)) {
            StructCache.getEntryStruct(zipKey,entryKey).refs++;
            return ZipUtil.ints2long(zipKey,entryKey);
        }
        
        return 0;
    }
    
    private static long getNextEntry( long jzfile , int i ) {
        int zipKey = ZipUtil.low32(jzfile);
        int entryKey = StructCache.getEntryStructKey(zipKey,i);
        
        StructCache.getEntryStruct(zipKey,entryKey).refs++;
        return ZipUtil.ints2long(zipKey,entryKey);
    }
    
    private static void freeEntry( long jzfile , long jzentry ) {
        int zipKey = ZipUtil.low32(jzfile);
        int entryKey = ZipUtil.low32(jzentry);
        
        ZipEntryStruct entry = StructCache.getEntryStruct(zipKey,entryKey);
        
        if(--entry.refs == 0) {
            //entry.buffer = null;
        }
    }
    
    private static int read( long jzfile , long jzentry , long pos , byte[] b , int off, int len ) throws IOException {
        int zipKey              = ZipUtil.low32(jzfile);
        int entryKey            = ZipUtil.low32(jzentry);
        ZipFileStruct zip       = StructCache.getFileStruct(zipKey);
        ZipEntryStruct entry    = StructCache.getEntryStruct(zipKey,entryKey);
        RandomAccessFile raf    = zip.raf;
        long start;
        long entrySize;
        
        if( zip == null || entry == null ) return -1;
        
        raf.seek(pos);
        return raf.read(b,off,len);
    }
    
    private static int getMethod( long jzentry ) {
        int zipKey      = ZipUtil.high32(jzentry);
        int entryKey    = ZipUtil.low32(jzentry);
        return StructCache.getEntryStruct(zipKey,entryKey).method;
    }
    
    private static long getCSize( long jzentry ) {
        int zipKey      = ZipUtil.high32(jzentry);
        int entryKey    = ZipUtil.low32(jzentry);
        return StructCache.getEntryStruct(zipKey,entryKey).csize;
    }
    
    private static long getSize( long jzentry ) {
        int zipKey      = ZipUtil.high32(jzentry);
        int entryKey    = ZipUtil.low32(jzentry);
        return StructCache.getEntryStruct(zipKey,entryKey).size;
    }
    
    private static String getZipMessage( long jzfile ) { return ""; }
    
    private static void initIDs() {}
}
