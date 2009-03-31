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

package org.jnode.fs.command.archive;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipException;

public class Zip extends ArchiveCommand {

    private static final int BUFFER_SIZE = 4096;
    
    private static final int V_DEBUG = 10;
    private static final int V_NOTICE = 1;
    private static final int V_WARN   = 0;
    private static final int V_ERROR  = -1;
    
    /* ZIP_DELETE searches the archive for matching entries and removes them
     * ZIP_EXTRACT dumps the contents of the zip archive to the file system
     * ZIP_ADD searches the filesystem for matching filenames and adds them to the archive
     * ZIP_MOVE searches the filesystem for matching filesnames and adds them to the archive, deleting the original
     *          files when the archive is created.
     * ZIP_REFRESH searches the archive for matching entries, then searches the filesystem for files that match
     *             the entries that have a more recent mod time. If there are any they replace that entry in the archive.
     * ZIP_UPDATE searches the filesystem for matching filesnames, adds them if they dont already exist, if they do
     *            exist, replace only if the filesystem mod time is more recent
     */
    private static final int ZIP_DELETE = 1;
    private static final int ZIP_REFRESH = 2;
    private static final int ZIP_MOVE = 3;
    private static final int ZIP_ADD = 4;
    private static final int ZIP_UPDATE = 5;
    private static final int ZIP_EXTRACT = 6;
    
    private File zfile;
    private File tmpFile;
    private ZipOutputStream zout;
    
    private int mode;
    private int verbosity = 0;
    
    public Zip(String s) {
        super(s);
    }
    /*
    Zip( File zfile , CommandInput stdin , CommandOutput stdout , CommandOutput stderr ) {
        inr = stdin.getReader();
        outw = stdout.getPrintWriter();
        errw = stderr.getPrintWriter();
        this.zfile = zfile;
    }
    
    void add( String[] files ) throws IOException {
        setupOutputStream();
        // if the zip file already exists we have to copy the entries into the out stream, unless
        // one of the given files matches an entry. If so, then either ask the user to overwrite
        // or if this is an update operation, then check the mod times.
        
        File file;
        ZipEntry entry;
        InputStream in;
        
        for(String name : files) {
            file = new File(name);
            if(!file.exists()) {
                error(file+" does not exist.");
                continue;
            }
            if((in = new FileInputStream(file)) == null) {
                error("Could not open stream");
                continue;
            }
            
            entry = new ZipEntry(name);
            zout.putNextEntry(entry);
            processStream(in,zout);
            zout.closeEntry();
        }
        zout.finish();
        zout.close();
    }
    
    void extract() throws IOException {
        if(!zfile.exists()) {
            error("Cannot find file: "+zfile);
            return;
        }
        
        ZipFile archive = new ZipFile(zfile);
        Enumeration<? extends ZipEntry> entries = archive.entries();
        ZipEntry entry;
        File file;
        InputStream in;
        OutputStream out;
        
        while(entries.hasMoreElements()) {
            entry = entries.nextElement();
            file = new File(entry.getName());
            if(file.exists()) {
                if(prompt_yn(file+" exists. Overwrite? ")) {
                    file.delete();
                } else {
                    continue;
                }
            }
            file.createNewFile();
            out = new FileOutputStream(file);
            in = archive.getInputStream(entry);
            if(in == null || out == null) {
                error("Could not open streams");
                continue;
            }
            processStream(in,out);
            in.close();
            out.close();
        }
    }
    
    void close() throws IOException {
        zout.close();
    }
    
    private void setupOutputStream() throws IOException {
        tmpFile = new File(zfile.getName()+".tmp");
        tmpFile.createNewFile();
        zout = new ZipOutputStream( new FileOutputStream(tmpFile) );
    }
    */
}
