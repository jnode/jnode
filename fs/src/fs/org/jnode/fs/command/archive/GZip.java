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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 *
 * TODO implement test/list
 * TODO use GZIP env variable
 * TODO implement name/noname flag usage
 * @author chris boertien
 */
public class GZip extends ArchiveCommand {
    
    private static final String msg_exists_prompt = " already exists; do you wish to overwrite (y or n)? ";
    private static final String msg_exists_skip   = "gzip: skipping file: ";
    private static final String msg_err_open = "gzip: Cannot open file: ";
    private static final String msg_err_create = "gzip: Cannot create file: ";
    private static final String msg_err_bad_suffix = "gzip: Invalid suffix, expecting ";

    private static final int BUFFER_SIZE = 4096;
    
    private File[] files;
    private InputStream stdinStream;
    private OutputStream stdoutStream;
    
    protected static final int GZIP_LIST = 1;
    protected static final int GZIP_TEST = 2;
    protected static final int GZIP_DECOMPRESS = 3;
    protected static final int GZIP_COMPRESS = 4;
    
    protected String suffix = ".gz";
    protected int mode;
    
    protected GZip(String s) {
        super(s);
        createStreamBuffer(BUFFER_SIZE);
    }
    
    public void execute(File[] files , boolean forced , boolean use_stdout , boolean recurse) throws IOException {
        setup();
        stdinStream = getInput().getInputStream();
        stdoutStream = getOutput().getOutputStream();
        
        switch(mode) {
            case GZIP_LIST : 
                list(processFiles(files, recurse)); return;
            case GZIP_TEST : 
                test(processFiles(files, recurse)); return;
            case GZIP_COMPRESS : 
                compress(processFiles(files, recurse), forced, use_stdout); return;
            case GZIP_DECOMPRESS : 
                decompress(processFiles(files, recurse), forced, use_stdout); return;
        }
    }
    
    protected void compress(File[] files , boolean forced , boolean use_stdout) throws IOException {
        InputStream in;
        OutputStream out = null;
        GZIPOutputStream gzout = null;
        File gzFile = null;
        float sizeDiff;
        
        debug("Compress");
        debug("forced=" + forced);
        debug("use_stdout=" + use_stdout);
        
        if (files == null) {
            debug("stdin > stdout");
            processStream(stdinStream, new GZIPOutputStream(stdoutStream, BUFFER_SIZE));
        } else {
            if (use_stdout) gzout = new GZIPOutputStream(stdoutStream, BUFFER_SIZE);
            if (use_stdout) debug("files > stdout");
            else debug("files > files");
        
            for (File file : files) {
                debug(file.getName());
                if (!file.exists()) {
                    error("File does not exist: " + file.getName());
                    continue;
                }
                if (!use_stdout) {
                    gzFile = new File(file.getAbsolutePath() + suffix);
                    if ((out = openFileWrite(gzFile, true, forced)) == null) continue;
                    gzout = new GZIPOutputStream(out, BUFFER_SIZE);
                }
                if ((in = openFileRead(file)) == null) continue;
                processStream(in, gzout);
                if (!use_stdout) {
                    gzout.finish();
                    gzout.close();
                    sizeDiff = ((float) gzFile.length() / (float) file.length()) * 100;
                    notice(file + ":\t" + sizeDiff + "% -- replaced with " + gzFile);
                    file.delete();
                }
                in.close();
            }
            if (use_stdout) gzout.finish();
        }
    }
    
    protected void decompress(File[] files , boolean forced , boolean use_stdout) throws IOException {
        InputStream in;
        OutputStream out = null;
        String name;
        File file = null;
        float sizeDiff;
        
        debug("Decompress");
        debug("forced=" + forced);
        debug("use_stdout=" + use_stdout);
        
        if (use_stdout) out = stdoutStream;
        
        if (files == null) {
            debug("stdin > stdout");
            processStream(new GZIPInputStream(stdinStream, BUFFER_SIZE), stdoutStream);
        } else {
            if (use_stdout) debug("files > stdout");
            else debug("files > files");
            
            for (File gzFile : files) {
                debug(gzFile.getName());
                if (!gzFile.exists()) {
                    error("File not found: " + gzFile);
                    continue;
                }
                if (!gzFile.getName().endsWith(suffix)) {
                    notice("gzip: " + file + ": unknown suffix -- ignored");
                    continue;
                }
                if (!use_stdout) {
                    name = gzFile.getAbsolutePath();
                    name = name.substring(0, name.length() - suffix.length());
                    file = new File(name);
                    if ((out = openFileWrite(file, true, forced)) == null) continue;
                }
                
                if ((in = new GZIPInputStream(openFileRead(gzFile), BUFFER_SIZE)) == null) continue;
                processStream(in, out);
                in.close();
                if (!use_stdout) {
                    sizeDiff = ((float) gzFile.length() / (float) file.length()) * 100;
                    notice(gzFile + ":\t" + sizeDiff + "% -- replaced with " + file);
                    gzFile.delete();
                    out.close();
                }
            }
        }
    }
    
    protected void test(File[] files) {}
    
    protected void list(File[] files) {}
    
    private File[] processFiles(File[] files , boolean recurse) {
        if (files == null || files.length == 0) return null;
        
        debug("processFiles(files(" + files.length + ")," + recurse + ")");
        
        ArrayList<File> _files = new ArrayList<File>();
        
        for (File file : files) {
            debug(file.getName());
            if (file.getName().equals("-")) {
                debug("found stdin");
                return null;
            }
            if (!file.exists()) {
                error("Cannot find file: " + file);
                continue;
            }
            if (file.isDirectory() && recurse) {
                debug("searching directory: " + file);
                File[] dirList = file.listFiles();
                for (File subFile : dirList) {
                    debug(subFile.getName());
                    if (subFile.isFile()) {
                        debug(subFile + " added");
                        _files.add(subFile);
                    }
                }
            } else {
                if (file.isFile()) {
                    debug(file + " added");
                    _files.add(file);
                }
            }
        }
        
        if (_files.size() == 0) return null;
        debug("Found " + _files.size() + " files");
        return _files.toArray(files);
    }
}
