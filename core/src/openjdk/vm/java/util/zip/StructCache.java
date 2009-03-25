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
 
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;

class StructCache {

    private static final Map<Integer, ZipFileEntry>     fileCache	
        = new HashMap<Integer, ZipFileEntry>();
/*
    private static final Map<Integer, InflaterStruct>   inflaters	
        = new HashMap<Integer, InflaterStruct>();

    private static final Map<Integer, DeflaterStruct>   deflaters	
        = new HashMap<Integer, DeflaterStruct>();
*/
    private StructCache() {}

    // File Cache access methods
    
    private static ZipFileEntry getEntry( int zipKey ) {
        return fileCache.get(zipKey);
    }

    private static void putNewEntry( ZipFileStruct zip ) {
        int key             = zip.file.getName().hashCode();
        ZipFileEntry entry  = new ZipFileEntry(zip);
        fileCache.put(key, entry);
    }

    private static ZipFileEntry removeEntry( int zipKey ) {
        return fileCache.remove(zipKey);
    }

    private static boolean containsEntry( int zipKey ) {
        return fileCache.containsKey(zipKey);
    }

    // ZipFileStruct

    static final long getFileStructKey( File file , int mode , long lastModified ) {

        synchronized (fileCache) {
            int zipKey = file.getName().hashCode();
            if(!containsEntry(zipKey)) {
                try {
					ZipFileStruct zip = new ZipFileStruct(file,mode,lastModified,zipKey);
                    putNewEntry(zip);
                }
                catch(ZipException ze) {
                    ze.printStackTrace(System.err);
                    return 0;
                }
                catch(IOException e) {
                    e.printStackTrace(System.err);
                    return 0;
                }
            }
            return zipKey;
        }
    }

    static final void releaseFileStructKey( int zipKey ) {
        synchronized (fileCache) {
            if (--getEntry(zipKey).fileStruct.refs == 0) {
                removeEntry(zipKey);
            }
        }
    }

    static final ZipFileStruct getFileStruct( int zipKey ) {
        return getEntry(zipKey).fileStruct;
    }

    static final boolean hasFileStruct( int zipKey ) {
        return containsEntry(zipKey);
    }

    // ZipEntryStruct

    static final ZipEntryStruct getEntryStruct( int zipKey , int entryKey ) {
        return getEntry(zipKey).entries.get(entryKey);
    }

    static final void putEntries( int zipKey , ZipEntryStruct[] entries ) {
        for (int i = 0; i < entries.length; i++) {
            int entryKey = entries[i].name.hashCode();
            getEntry(zipKey).entries.put(entryKey, entries[i]);
            getEntry(zipKey).entriesIndex.add(entryKey);
        }
    }

    static final int getEntryStructKey( int zipKey , int index ) {
        return getEntry(zipKey).entriesIndex.get(index);
    }

    static final boolean hasEntryStruct( int zipKey , int entryKey ) {
        return getEntry(zipKey).entries.containsKey(entryKey);
    }

    // InflaterStruct
/*
    static final int getInflaterStructKey( boolean nowrap ) {
        InflaterStruct inflater = new InflaterStruct(nowrap);
        int key = System.identityHashCode(inflater);
        inflaters.put(key, inflater);
        return key;
    }

    static final InflaterStruct getInflaterStruct( int key ) {
        return inflaters.get(key);
    }

    static final boolean releaseInflaterStruct( int key ) {
        return (inflaters.remove(key) != null);
    }

	// DeflaterStruct

    static final int getDeflaterStructKey( int strategy , int level , boolean nowrap ) {
        DeflaterStruct deflater = new DeflaterStruct(strategy,level,nowrap);
        int key = System.identityHashCode(deflater);
        deflaters.put(key, deflater);
        return key;
    }

    static final DeflaterStruct getDeflaterStruct( int key ) {
        return deflaters.get(key);
    }

    static final boolean releaseDeflaterStruct( int key ) {
        return (deflaters.remove(key) != null);
    }
*/
    private static class ZipFileEntry {

        private ZipFileStruct					fileStruct;
        private Map<Integer, ZipEntryStruct>	entries;
        private List<Integer>					entriesIndex;

        private ZipFileEntry( ZipFileStruct zip ) {
            fileStruct 		= zip;
            entries 		= new HashMap<Integer, ZipEntryStruct>();
            entriesIndex 	= new ArrayList<Integer>();
        }
    }
}
