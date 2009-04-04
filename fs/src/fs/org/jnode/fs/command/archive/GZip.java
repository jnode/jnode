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
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FlagArgument;
import org.jnode.shell.syntax.FileArgument;
import org.jnode.shell.syntax.StringArgument;

/**
 *
 * TODO implement test/list
 * TODO use GZIP env variable
 * TODO implement name/noname flag usage
 * @author chris boertien
 */
public class GZip extends ArchiveCommand {
    
    private static final String help_suffix  = "append <suffix> on compressed files";
    private static final String help_list    = "list compressed file contents";
    private static final String help_noname  = "do not save or restore the original name and time stamp";
    private static final String help_name    = "save or restore the original name and time stamp";
    private static final String help_recurse = "operate recursively on directories";
    private static final String help_test    = "test compressed file integrity";
    private static final String help_file    = "the files to compress, use stdin if FILE is '-' or no files are listed";
    
    protected final FileArgument Files    = new FileArgument("files", Argument.OPTIONAL | Argument.MULTIPLE, help_file);
    protected final FlagArgument List     = new FlagArgument("list", Argument.OPTIONAL, help_list);
    protected final FlagArgument NoName   = new FlagArgument("noname", Argument.OPTIONAL, help_noname);
    protected final FlagArgument Name     = new FlagArgument("name", Argument.OPTIONAL, help_name);
    protected final FlagArgument Recurse  = new FlagArgument("recurse", Argument.OPTIONAL, help_recurse);
    protected final FlagArgument Test     = new FlagArgument("test", Argument.OPTIONAL, help_test);
    protected final StringArgument Suffix = new StringArgument("suffix", Argument.OPTIONAL, help_suffix);
    
    protected String suffix = ".gz";
    protected boolean recurse;
    
    protected GZip(String s) {
        super(s);
        createStreamBuffer(4096);
    }
    
    public void execute(String command) {
        super.execute(command);
        
        if (!command.equals("zcat")) {
            if (Suffix.isSet()) suffix = Suffix.getValue();
        
            recurse = Recurse.isSet();
        }
        
        try {
            if (compress) {
                compress(processFileList(Files.getValues(), recurse));
            } else {
                decompress(processFileList(Files.getValues(), recurse));
            }
        } catch (IOException e) {
            e.printStackTrace();
            fatal(err_exception_uncaught, 1);
        }
    }
    
    private void compress(File[] files) throws IOException {
        InputStream in;
        OutputStream out = null;
        GZIPOutputStream gzout = null;
        File gzfile = null;
        float sizeDiff;
        
        if (files == null) {
            processStream(stdin, new GZIPOutputStream(stdout, BUFFER_SIZE));
            return;
        }
        
        if (use_stdout) {
            gzout = new GZIPOutputStream(stdout, BUFFER_SIZE);
        }
    
        for (File file : files) {
            if (!use_stdout) {
                gzfile = new File(file.getAbsolutePath() + suffix);
                if ((out = openFileWrite(gzfile, true, force)) == null) continue;
                gzout = new GZIPOutputStream(out, BUFFER_SIZE);
            }
            
            if (file.getName().equals("-")) {
                processStream(stdin, gzout);
                continue;
            }
            
            if ((in = openFileRead(file)) == null) {
                if (!use_stdout) gzout.close();
                continue;
            }
            processStream(in, gzout);
            
            if (!use_stdout) {
                gzout.finish();
                gzout.close();
                sizeDiff = ((float) gzfile.length() / (float) file.length()) * 100;
                notice(String.format(fmt_size_diff, file, sizeDiff, gzfile));
                file.delete();
            }
            in.close();
        }
        
        if (use_stdout) {
            gzout.finish();
            gzout.close();
        }
    }
    
    private void decompress(File[] files) throws IOException {
        InputStream in;
        OutputStream out = null;
        File file = null;
        float sizeDiff;
        
        if (files == null) {
            processStream(new GZIPInputStream(stdin, BUFFER_SIZE), stdout);
            return;
        }
        
        if (use_stdout) {
            out = stdout;
        }
        
        for (File gzfile : files) {
            if (!use_stdout) {
                file = stripSuffix(gzfile);
                if ((out = openFileWrite(file, true, force)) == null) continue;
            }
            
            if (gzfile.getName().equals("-")) {
                processStream(new GZIPInputStream(stdin, BUFFER_SIZE), out);
                continue;
            }
            
            if ((in = new GZIPInputStream(openFileRead(gzfile), BUFFER_SIZE)) == null) {
                if (!use_stdout) out.close();
                continue;
            }
            processStream(in, out);
            in.close();
            
            if (!use_stdout) {
                out.close();
                sizeDiff = ((float) gzfile.length() / (float) file.length()) * 100;
                notice(String.format(fmt_size_diff, gzfile, sizeDiff, file));
                gzfile.delete();
            }
        }
    }
    
    protected void test(File[] files) {}
    
    protected void list(File[] files) {}
    
    private File stripSuffix(File file) {
        String name = file.getAbsolutePath();
        
        if (!name.endsWith(suffix)) {
            notice(name + " unknown suffix -- ignore");
            return null;
        }
        
        name = name.substring(0, name.length() - suffix.length());
        
        return new File(name);
    }
}
