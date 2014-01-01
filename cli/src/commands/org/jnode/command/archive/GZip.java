/*
 * $Id$
 *
 * Copyright (C) 2003-2014 JNode.org
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
import java.io.OutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
    
    private List<File> files;
    protected String suffix = ".gz";
    protected boolean recurse;
    
    protected GZip(String s) {
        super(s);
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
        GZIPOutputStream gzout = null;
        
        if (use_stdout) {
            gzout = new GZIPOutputStream(stdout, BUFFER_SIZE);
        }
        
        for (File file : files) {
            if (file.getName().equals("-")) {
                processStream(stdin, gzout);
                continue;
            }
            try {
                if (use_stdout) {
                    if ((in = openFileRead(file)) == null) {
                        rc = 1;
                        continue;
                    }
                    processStream(in, gzout);
                    continue;
                }
                try {
                    File gzfile = new File(file.getAbsolutePath() + suffix);
                    if ((out = openFileWrite(gzfile, true, force)) == null) {
                        rc = 1;
                        continue;
                    }
                    gzout = new GZIPOutputStream(out, BUFFER_SIZE);
                    if ((in = openFileRead(file)) == null) {
                        rc = 1;
                        continue;
                    }
                    processStream(in, gzout);
                    gzout.finish();
                    float sizeDiff = ((float) gzfile.length() / (float) file.length()) * 100;
                    notice(String.format(fmt_size_diff, file, sizeDiff, gzfile));
                    file.delete();
                } finally {
                    close(gzout);
                }
            } finally {
                close(in);
            }
        }
        
        if (use_stdout) {
            gzout.finish();
            // TEST need to see if this is even necessary, and if it is
            // should it be within a finally block
            gzout.close();
        }
    }
    
    private void decompress() throws IOException {
        InputStream in = null;
        OutputStream out = stdout;
        
        for (File gzfile : files) {
            if (gzfile.getName().equals("-")) {
                processStream(new GZIPInputStream(stdin, BUFFER_SIZE), out);
                continue;
            }
            
            try {
                if (use_stdout) {
                    if ((in = new GZIPInputStream(openFileRead(gzfile), BUFFER_SIZE)) == null) {
                        continue;
                    }
                    processStream(in, out);
                    continue;
                }
                try {
                    File file = stripSuffix(gzfile);
                    if (file == null) {
                        continue;
                    }
                    if ((out = openFileWrite(file, true, force)) == null) {
                        rc = 1;
                        continue;
                    }
                    if ((in = new GZIPInputStream(openFileRead(gzfile), BUFFER_SIZE)) == null) {
                        continue;
                    }
                    processStream(in, out);
                    float sizeDiff = ((float) gzfile.length() / (float) file.length()) * 100;
                    notice(String.format(fmt_size_diff, gzfile, sizeDiff, file));
                    gzfile.delete();
                } finally {
                    close(out);
                }
            } finally {
                close(in);
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
    
    private void parseOptions(String command) {
        if (!command.equals("zcat")) {
            if (Suffix.isSet()) suffix = Suffix.getValue();
        
            recurse = Recurse.isSet();
        }
        
        files = new ArrayList<File>();
        
        for (File file : Files.getValues()) {
            if (file.isDirectory()) {
                if (recurse) {
                    for (File f : file.listFiles()) {
                        if (!f.isDirectory()) {
                            files.add(f);
                        }
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
        }
        
        if (files.size() == 1 && files.get(0).getName().equals("-")) {
            use_stdout = true;
        }
    }
}
