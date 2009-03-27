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

package org.jnode.shell.command;

import org.jnode.shell.io.CommandInput;
import org.jnode.shell.io.CommandOutput;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

class GZIP {
    
    private static final String msg_exists_prompt = " already exists; do you wish to overwrite (y or n)? ";
    private static final String msg_exists_skip   = "gzip: skipping file: ";
    private static final String msg_err_open = "gzip: Cannot open file: ";
    private static final String msg_err_create = "gzip: Cannot create file: ";
    private static final String msg_err_bad_suffix = "gzip: Invalid suffix, expecting ";

    private static final int BUFFER_SIZE = 4096;
    
    private File[] files;
    private PrintWriter outw;
    private PrintWriter errw;
    private Reader inr;
    private InputStream stdinStream;
    private OutputStream stdoutStream;
    private String suffix = "gz";
    private int verbosity;
    private boolean checkSuffix;
    
    GZIP( File[] files , String suffix , CommandInput stdin , CommandOutput stdout , CommandOutput stderr ) {
        outw = stdout.getPrintWriter();
        errw = stderr.getPrintWriter();
        inr  = stdin.getReader();
        stdinStream  = stdin.getInputStream();
        stdoutStream = stdout.getOutputStream();
        if(files != null && files.length > 0)
            this.files = files;
        if(suffix != null && !suffix.equals("")) {
            this.suffix = suffix;
        }
    }
    
    void checkSuffix( boolean b ) {
        checkSuffix = b;
    }
    
    void verbosity( int v ) {
        verbosity = v;
    }
    
    void compress( boolean forced , boolean use_stdout , boolean recurse ) throws IOException {
        
        InputStream in;
        OutputStream out = null;
        GZIPOutputStream gzout = null;
        File gzFile;
        
        if(files == null) {
            processStream(stdinStream , new GZIPOutputStream(stdoutStream,BUFFER_SIZE));
        } else {
            if(use_stdout) gzout = new GZIPOutputStream(stdoutStream,BUFFER_SIZE);
        
            for(File file : files) {
                if(!file.exists()) {
                    error("File does not exist: "+file.getName());
                    continue;
                }
                if(!use_stdout) {
                    gzFile = new File(file.getName()+"."+suffix);
                    out = openFileWrite(gzFile,true,forced);
                    if(out == null) continue;
                    gzout = new GZIPOutputStream(out,BUFFER_SIZE);
                }
                in = openFileRead(file);
                processStream(in,gzout);
                if(!use_stdout) {
                    gzout.finish();
                    gzout.close();
                    file.delete();
                }
                in.close();
            }
            if(use_stdout) gzout.finish();
        }
    }
    
    void decompress( boolean forced , boolean use_stdout , boolean recurse ) throws IOException {
        
        InputStream in;
        OutputStream out = null;
        String name;
        File file;
        
        if(use_stdout) out = stdoutStream;
        
        if(files == null) {
            processStream( new GZIPInputStream(stdinStream,BUFFER_SIZE) , stdoutStream);
        } else {
            for(File gzFile : files) {
                if(!gzFile.exists()) {
                    error("File not found: " + gzFile.getName());
                    continue;
                }
                if(!gzFile.getName().endsWith(suffix)) {
                    error(msg_err_bad_suffix + suffix + " : " + gzFile);
                    continue;
                }
                if(!use_stdout) {
                    name = gzFile.getName().substring(0,gzFile.getName().length() - (suffix.length()+1));
                    file = new File(name);
                    out = openFileWrite(file,true,forced);
                }
                if(out == null) continue;
                
                in = new GZIPInputStream(openFileRead(gzFile),BUFFER_SIZE);
                processStream(in,out);
                in.close();
                if(!use_stdout) {
                    gzFile.delete();
                    out.close();
                }
            }
        }
    }
    
    void test() {}
    
    void list() {}
    
    private void processStream( InputStream in , OutputStream out ) throws IOException {
        int len;
        final byte[] buf = new byte[BUFFER_SIZE];
        while((len = in.read(buf)) > 0) {
            out.write(buf,0,len);
        }
    }
    
    private void debug(String s) {
        if(verbosity == 2) outw.println(s);
    }
    
    private void verbose(String s) {
        if(verbosity > 0) outw.println(s);
    }
    
    private void error(String s) {
        if(verbosity >= 0) errw.println(s);
    }
    
    private char prompt(String s) throws IOException {
        outw.print(s);
        int n = inr.read();
        outw.println();
        return (char)n;
    }
    
    private InputStream openFileRead(File file) {
        try {
            return new FileInputStream(file);
        }
        catch(IOException ioe) {
            return null;
        }
    }
    
    private OutputStream openFileWrite(File file , boolean delete , boolean forced ) {
        int choice;
        
        try {
            if(file.exists()) {
                if(forced) {
                    file.delete();
                }
                else {
                    for(;;) {
                        choice = prompt(msg_exists_prompt);
                        if(choice == 'y' || choice == 'n') break;
                    }
                    switch(choice) {
                        case 'y' : file.delete(); break;
                        case 'n' : verbose(msg_exists_skip + file.getName()); break;
                    }
                }
                if(!file.createNewFile()) {
                    return null;
                }
            }
            return new FileOutputStream(file);
        }
        catch(IOException ioe) {
            return null;
        }
    }
}
