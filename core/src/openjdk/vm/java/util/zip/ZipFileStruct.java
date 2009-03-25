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
import java.io.RandomAccessFile;
import java.io.IOException;
import java.io.EOFException;

class ZipFileStruct implements ZipConstants {

    File file;
    RandomAccessFile raf;
    String[] manifestEntries;
    long refs;
    long length;
    long modTime;
    int totalEntries;
    int ID;
    boolean doDelete;
    
    ZipFileStruct( File file , int mode , long lastModified , int ID ) throws IOException {
        this.ID =       file.getName().hashCode();
        this.file =     file;
        this.length =   file.length();
        this.refs =     1;
        this.modTime =  lastModified;
        this.doDelete = (mode & ZipFile.OPEN_DELETE) == ZipFile.OPEN_DELETE;
        this.raf =      new RandomAccessFile(file,"r");
        checkZipFile();
        readEntries(ID);
    }
    
    private void checkZipFile() throws ZipException {
        byte[] buf = new byte[4];
        int sig;
        
        try {
            raf.readFully(buf);
        } catch (IOException _) {}
        
        sig = (buf[0] & 0xFF) | ((buf[1] & 0xFF) << 8) | ((buf[2] & 0xFF) << 16) | ((buf[3] & 0xFF) << 24);
        
        if(sig != LOCSIG) {
            try {
                raf.close();
            } catch (IOException _) {}
            throw new ZipException("java.util.zip: Not a valid zip file");
        }
    }
    
    private void readEntries( int zipKey ) throws IOException {
        PartialInputStream input    = new PartialInputStream(raf,4096);
        String name                 = file.getName();
        ZipEntryStruct[] entries;
        long pos                    = raf.length() - ENDHDR;
        long top                    = Math.max(0, pos - 65536);
        int centralOffset, i;
        int namelen, extralen, commentlen;
        
        input.seek(pos);
        
        while(input.readLeInt() != ENDSIG) {
            if(pos < top) throw new ZipException("java.util.zip: central directory not found.");
            input.seek(pos--);
        }
        
        if(input.skip(ENDTOT - ENDNRD) != ENDTOT - ENDNRD) throw new EOFException(name);
        totalEntries = input.readLeShort();
        if(input.skip(ENDOFF - ENDSIZ) != ENDOFF - ENDSIZ) throw new EOFException(name);
        centralOffset = input.readLeInt();
        input.seek(centralOffset);
        
        entries = new ZipEntryStruct[totalEntries];
        
        for(i = 0; i < totalEntries; i++) {
            if(input.readLeInt() != CENSIG) throw new ZipException("java.util.zip: Invalid Central Directory Signature");
            
            ZipEntryStruct entry = new ZipEntryStruct();
            
            input.skip(6);
            entry.method    = input.readLeShort();
            entry.time      = input.readLeInt();
            entry.crc       = input.readLeInt();
            entry.csize     = input.readLeInt();
            entry.size      = input.readLeInt();
            namelen         = input.readLeShort();
            extralen        = input.readLeShort();
            commentlen      = input.readLeShort();
            input.skip(8);
            entry.offset    = input.readLeInt();
            entry.name      = input.readString(namelen);
            
            if(extralen > 0) {
                entry.extra = new byte[extralen];
                input.readFully(entry.extra);
            }
            
            if(commentlen > 0) {
                entry.comment = input.readString(commentlen);
            }
            
            entries[i] = entry;
        }
        
        StructCache.putEntries(zipKey,entries);
    }
    
    void close() {
        try {
            if(doDelete && !(file.delete())) {
                throw new IOException("java.util.zip: Failed to delete file: "+file.getName());
            }
            raf.close();
        }
        catch( IOException ioe ) {
            System.err.println("java.util.zip: IOException closing file");
        }
    }
}
