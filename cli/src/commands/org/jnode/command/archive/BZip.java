/*
 * $Id: header.txt 5714 2010-01-03 13:33:07Z lsantha $
 *
 * Copyright (C) 2003-2012 JNode.org
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
 
package org.jnode.command.archive;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.tools.bzip2.CBZip2InputStream;
import org.apache.tools.bzip2.CBZip2OutputStream;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FileArgument;
import org.jnode.shell.syntax.FlagArgument;


/**
 * BZip is the backing class for handling compression and decompression
 * of bzip files.
 *
 * @author chris boertien
 */
public class BZip extends ArchiveCommand {
    
    @SuppressWarnings("unused")
    private static final boolean DEBUG = false;
    
    private static final String help_compress = "forces compression; regardless of invocation name";
    private static final String help_files = "space seperated list of files to compress";
    private static final String help_keep = "Keep input files";
    private static final String help_small = "(ignored)";
    private static final String help_test     = "test the file integrity";
    
    private static final String fmt_suffix_bad = "Can't guess original name for %s -- using %s.out";
    
    protected final FlagArgument Compress;
    protected final FlagArgument Decompress;
    protected final FileArgument Files;
    protected final FlagArgument Keep;
    protected final FlagArgument Small;
    protected final FlagArgument Test;

    private List<File> files;    
    private int rc = 1;
    protected int clevel;
    protected boolean keep;
    protected boolean small;
    
    public BZip(String s) {
        super(s);
        Compress   = new FlagArgument("compress", Argument.OPTIONAL, help_compress);
        Decompress = new FlagArgument("decompress", Argument.OPTIONAL, help_decompress);
        int flags = Argument.OPTIONAL | Argument.MULTIPLE | Argument.EXISTING | FileArgument.HYPHEN_IS_SPECIAL;
        Files      = new FileArgument("files", flags, help_files);
        Keep       = new FlagArgument("keep", Argument.OPTIONAL, help_keep);
        Small      = new FlagArgument("small", Argument.OPTIONAL, help_small);
        Test       = new FlagArgument("test", Argument.OPTIONAL, help_test);
        createStreamBuffer(4096);
    }
    
    public void execute(String command) {
        super.execute(command);
        parseOptions(command);
 
        try {
            if (compress) {
                compress();
            } else {
                decompress();
            }
            rc = 0;
        } catch (IOException e) {
            error(err_exception_uncaught);
            rc = 1;
        } finally {
            exit(rc);
        }
    }
    
    private void compress() throws IOException {
        InputStream in = null;
        OutputStream out = null;
        CBZip2OutputStream bzout = null;
        
        if (use_stdout) {
            bzout = new CBZip2OutputStream(stdout, clevel);
        }
        
        for (File file : files) {
            if (file.getName().equals("-")) {
                processStream(stdin, bzout);
                continue;
            }
            
            try {
                if (use_stdout) {
                    if ((in = openFileRead(file)) == null) {
                        rc = 1;
                        continue;
                    }
                    processStream(in, bzout);
                    continue;
                }
                try {
                    File bzfile = new File(file.getAbsolutePath() + ".bz2");
                    if ((out = openFileWrite(bzfile, true, force)) == null) {
                        rc = 1;
                        continue;
                    }
                    bzout = new CBZip2OutputStream(out, clevel);
                    if ((in = openFileRead(file)) == null) {
                        rc = 1;
                        continue;
                    }
                    processStream(in, bzout);
                    float sizeDiff = ((float) bzfile.length() / (float) file.length()) * 100;
                    notice(String.format(fmt_size_diff, file, sizeDiff, bzfile));
                    if (!keep) file.delete();
                } finally {
                    close(bzout);
                }
            } finally {
                close(in);
            }
        }
        // TEST need to see if this is even necessary, and if it is
        // should it be within a finally block
        if (use_stdout) {
            bzout.close();
        }
    }
    
    private void decompress() throws IOException {
        InputStream in = null;
        OutputStream out = stdout;
        
        for (File bzfile : files) {
            if (bzfile.getName().equals("-")) {
                processStream(new CBZip2InputStream(stdin), out);
                continue;
            }
            try {
                if (use_stdout) {
                    if ((in = new CBZip2InputStream(openFileRead(bzfile))) == null) {
                        rc = 1;
                        continue;
                    }
                    processStream(in, out);
                    continue;
                }
                try {
                    File file = stripSuffix(bzfile);
                    if ((out = openFileWrite(file, true, force)) == null) {
                        rc = 1;
                        continue;
                    }
                    if ((in = new CBZip2InputStream(openFileRead(bzfile))) == null) {
                        rc = 1;
                        continue;
                    }
                    processStream(in, out);
                    float sizeDiff = ((float) bzfile.length() / (float) file.length()) * 100;
                    notice(String.format(fmt_size_diff, bzfile, sizeDiff, file));
                    if (!keep) bzfile.delete();
                } finally {
                    close(out);
                }
            } finally {
                close(in);
            }
        }
    }
    
    @SuppressWarnings("unused")
    private void test(File[] files) {
        // TODO
        // requires patch to apache ant to have CBZip2InputStream fail with an
        // exception, instead it just prints to stderr and doesn't tell us if
        // it failed.
        //
        // Otherwise we would have to read and compute the crc ourself.
    }
    
    /**
     * Strips .bz and .bz2 suffixes from the file. Will also replace
     * .tbz and .tbz2 files with .tar suffix. If the suffix doesn't match
     * any of these, the suffix .out will be appended to the file name
     */
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
            notice(String.format(fmt_suffix_bad, bzfile.getPath(), bzfile.getPath()));
            return new File(bzfile.getPath() + ".out");
        }
        
        if (len > 0) {
            name = name.substring(0, name.length() - len);
        }
        
        if (newSuffix != null) {
            name = name + newSuffix;
        }
        
        return new File(name);
    }
    
    private void parseOptions(String command) {
        small = Small.isSet();
        if (!command.equals("bzcat")) {
            if (compress && Decompress.isSet()) {
                compress = false;
            }
            if (!compress && Compress.isSet()) {
                compress = true;
            }
            
            keep  = use_stdout || Keep.isSet();
        }
        
        files = new ArrayList<File>();
        
        for (File file : Files.getValues()) {
            if (file.isDirectory()) {
                for (File f : file.listFiles()) {
                    if (!f.isDirectory()) {
                        files.add(f);
                    }
                }
            } else {
                if (file.getName().equals("-")) {
                    use_stdout = true;
                }
                files.add(file);
            }
        }
        
        if (files.size() == 0) {
            files.add(new File("-"));
            use_stdout = true;
        }
    }
}
