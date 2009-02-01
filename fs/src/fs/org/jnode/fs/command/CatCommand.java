/*
 * $Id$
 *
 * JNode.org
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
 
package org.jnode.fs.command;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FileArgument;
import org.jnode.shell.syntax.FlagArgument;
import org.jnode.shell.syntax.URLArgument;

/**
 * Read files or network resources and write the concatenation to standard output.  If
 * no filenames or URIs are provided, copy standard input to standard output.  Data is
 * copied byte-wise.
 * <p>
 * If any file or URL cannot be opened, it is skipped and we (eventually) set a non-zero
 * return code.  If we get an IOException reading or writing data, we allow it to propagate.
 * 
 * @author epr
 * @author Andreas H\u00e4nel
 * @author crawley@jnode.org
 * @author Fabien DUMINY (fduminy@jnode.org)
 */
public class CatCommand extends AbstractCommand {

    private final FileArgument ARG_FILE = 
        new FileArgument("file", Argument.OPTIONAL | Argument.MULTIPLE | Argument.EXISTING, 
                "the files to be concatenated");

    private final URLArgument ARG_URL = 
        new URLArgument("url", Argument.OPTIONAL | Argument.MULTIPLE | Argument.EXISTING, 
                "the urls to be concatenated");
    
    private final FlagArgument FLAG_URLS =
        new FlagArgument("urls", Argument.OPTIONAL, "If set, arguments will be urls");

    private PrintWriter err;
    
    public CatCommand() {
        super("Concatenate the contents of files, urls or standard input to standard output");
        registerArguments(ARG_FILE, ARG_URL, FLAG_URLS);
    }

    private static final int BUFFER_SIZE = 8192;


    public static void main(String[] args) throws Exception {
        new CatCommand().execute(args);
    }

    public void execute() throws IOException {
        this.err = getError().getPrintWriter();
        OutputStream out = getOutput().getOutputStream();
        File[] files = ARG_FILE.getValues();
        URL[] urls = ARG_URL.getValues();
        
        boolean ok = true;
        if (urls != null && urls.length > 0) {
            for (URL url : urls) {
                InputStream is = null;
                try {
                    is = url.openStream();
                } catch (IOException ex) {
                    err.println("Can't fetch url '" + url + "': " + ex.getLocalizedMessage());
                    ok = false;
                }
                if (is != null) {
                    try {
                        process(is, out);
                    } finally {
                        try { 
                            is.close();
                        } catch (IOException ex) { 
                            /* ignore */
                        }
                    }
                }
            }
        } else if (files != null && files.length > 0) {
            for (File file : files) {
                InputStream is = null;
                try {
                    is = openFile(file);
                    if (is == null) {
                        ok = false;
                    } else {
                        process(is, out);
                    }
                } finally {
                    if (is != null) {
                        try { 
                            is.close();
                        } catch (IOException ex) {
                            /* ignore */
                        }
                    }
                }
            }
        } else {
            process(getInput().getInputStream(), out);
        }
        out.flush();
        if (!ok) { 
            exit(1); 
        }
    }

    /**
     * Copy all of stream 'in' to stream 'out'
     * @param in
     * @param out
     * @throws IOException
     */
    private void process(InputStream in, OutputStream out) throws IOException {
        int len;
        final byte[] buf = new byte[BUFFER_SIZE];
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
    }

    /**
     * Attempt to open a file, writing an error message on failure.
     * @param fname the filename of the file to be opened
     * @return An open stream, or <code>null</code>.
     * @throws FileNotFoundException 
     */
    private InputStream openFile(File file) throws FileNotFoundException {
        try {
            return new FileInputStream(file);
        } catch (IOException ex) {
            err.println("Cannot open file '" + file + "': " + ex.getLocalizedMessage());
            return null;
        }
    }

}
