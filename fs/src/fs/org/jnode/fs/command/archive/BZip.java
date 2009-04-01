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

import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FileArgument;
import org.jnode.shell.syntax.FlagArgument;

import org.apache.tools.bzip2.CBZip2InputStream;
import org.apache.tools.bzip2.CBZip2OutputStream;

/**
 *
 * @author chris boertien
 */
public class BZip extends ArchiveCommand {

    private static final String help_compress = "forces compression; regardless of invocation name";
    private static final String help_files = "space seperated list of files to compress";
    private static final String help_keep = "Keep input files";
    private static final String help_small = "(ignored)";
    private static final String help_test     = "test the file integrity";
    
    private static final String fmt_suffix_bad = "Can't guess original name for %s -- using %s.out";
    
    protected final FlagArgument Compress   = new FlagArgument("compress", Argument.OPTIONAL, help_compress);
    protected final FlagArgument Decompress = new FlagArgument("decompress", Argument.OPTIONAL, help_decompress);
    protected final FileArgument Files   = new FileArgument("files", Argument.OPTIONAL | Argument.MULTIPLE, help_files);
    protected final FlagArgument Keep       = new FlagArgument("keep", Argument.OPTIONAL, help_keep);
    protected final FlagArgument Small      = new FlagArgument("small", Argument.OPTIONAL, help_small);
    protected final FlagArgument Test       = new FlagArgument("test", Argument.OPTIONAL, help_test);
    
    protected int clevel = 9;
    protected boolean keep;
    
    public BZip(String s) {
        super(s);
        registerArguments(Compress, Decompress, Files, Keep, Small, Test);
        createStreamBuffer(4096);
    }
    
    public void execute() {
        super.execute();
        
        if (compress && Decompress.isSet()) {
            compress = false;
        }
        if (!compress && Compress.isSet()) {
            compress = true;
        }
        
        keep  = Keep.isSet();
        
        if (!keep && use_stdout) {
            keep = true;
        }
        
        try {
            if (compress) {
                compress(processFileList(Files.getValues(), true));
            } else {
                decompress(processFileList(Files.getValues(), true));
            }
        } catch (IOException e) {
            e.printStackTrace();
            fatal(err_exception_uncaught, 1);
        }
    }
    
    private void compress(File[] files) throws IOException {
        InputStream in;
        OutputStream out = null;
        CBZip2OutputStream bzout = null;
        File bzfile = null;
        float sizeDiff;
        
        if (files == null) {
            processStream(stdin, new CBZip2OutputStream(stdout, clevel));
            return;
        }
        
        if (use_stdout) {
            bzout = new CBZip2OutputStream(stdout, clevel);
        }
        
        for (File file : files) {
            if (!use_stdout) {
                bzfile = new File(file.getAbsolutePath() + ".bz2");
                if ((out = openFileWrite(bzfile, true, force)) == null) continue;
                bzout = new CBZip2OutputStream(out, clevel);
            }
            
            if (file.getName().equals("-")) {
                processStream(stdin, bzout);
                continue;
            }
            
            if ((in = openFileRead(file)) == null) {
                if (!use_stdout) bzout.close();
                continue;
            }
            processStream(in, bzout);
            in.close();
            
            if (!use_stdout) {
                bzout.close();
                sizeDiff = ((float) bzfile.length() / (float) file.length()) * 100;
                notice(String.format(fmt_size_diff, file, sizeDiff, bzfile));
                if (!keep) file.delete();
            }
        }
        
        if (use_stdout) {
            bzout.close();
        }
    }
    
    private void decompress(File[] files) throws IOException {
        InputStream in;
        OutputStream out = null;
        File file = null;
        float sizeDiff;
        
        if (files == null) {
            processStream(new CBZip2InputStream(stdin), stdout);
            return;
        }
        
        if (use_stdout) {
            out = stdout;
        }
        
        for (File bzfile : files) {
            if (!use_stdout) {
                file = stripSuffix(bzfile);
                if ((out = openFileWrite(file, true, force)) == null) continue;
            }
            
            if (bzfile.getName().equals("-")) {
                processStream(new CBZip2InputStream(stdin), out);
                continue;
            }
            
            if ((in = new CBZip2InputStream(openFileRead(bzfile))) == null) {
                if (!use_stdout) out.close();
                continue;
            }
            processStream(in, out);
            in.close();
            
            if (!use_stdout) {
                out.close();
                sizeDiff = ((float) bzfile.length() / (float) file.length()) * 100;
                notice(String.format(fmt_size_diff, bzfile, sizeDiff, file));
                if (!keep) bzfile.delete();
            }
        }
    }
    
    private void test(File[] files) {
        // requires patch to apache ant to have CBZip2InputStream fail with an
        // exception, instead it just prints to stderr and doesn't tell us if
        // it failed.
        //
        // Otherwise we would have to read and compute the crc ourself.
    }
    
    private File stripSuffix(File bzfile) {
        String name = bzfile.getName();
        int len = 0;
        String newSuffix = null;
        
        if (name.endsWith(".bz")) {
            len = 3;
        } else if (name.endsWith(".bz2")) {
            len = 4;
        } else if (name.endsWith(".tbz")) {
            len = 4;
            newSuffix = ".tar";
        } else if (name.endsWith(".tbz2")) {
            len = 5;
            newSuffix = ".tar";
        } else {
            notice(String.format(fmt_suffix_bad, bzfile, bzfile));
            notice("Can't guess original name for " + bzfile + " -- using " + bzfile + ".out");
            return new File(name + ".out");
        }
        
        if (len > 0) {
            name = name.substring(0, name.length() - len);
        }
        
        if (newSuffix != null) {
            name = name + newSuffix;
        }
        
        return new File(name);
    }
}
