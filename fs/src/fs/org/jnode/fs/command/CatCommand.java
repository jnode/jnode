/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.CommandLine;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FileArgument;
import org.jnode.shell.syntax.FlagArgument;
import org.jnode.shell.syntax.URLArgument;

/**
 * @author epr
 * @author Andreas H\u00e4nel
 * @author Stephen Crawley
 * @author Fabien DUMINY (fduminy@jnode.org)
 */
public class CatCommand extends AbstractCommand {

    private final FileArgument ARG_FILE = 
        new FileArgument("file", Argument.OPTIONAL | Argument.MULTIPLE, 
                "the files to be concatenated");

    private final URLArgument ARG_URL = 
        new URLArgument("url", Argument.OPTIONAL | Argument.MULTIPLE, 
                "the urls to be concatenated");
    
    private final FlagArgument FLAG_URLS =
        new FlagArgument("urls", Argument.OPTIONAL, "If set, arguments will be urls");
    
    public CatCommand() {
        super("Concatenate the contents of files, urls or standard input to standard output");
        registerArguments(ARG_FILE, ARG_URL, FLAG_URLS);
    }

    private static final int BUFFER_SIZE = 1024;


    public static void main(String[] args) throws Exception {
        new CatCommand().execute(args);
    }

    public void execute(CommandLine commandLine, InputStream in,
            PrintStream out, PrintStream err) throws IOException {
        File[] files = ARG_FILE.getValues();
        URL[] urls = ARG_URL.getValues();
        boolean ok = true;
        if (urls != null && urls.length > 0) {
            for (URL url : urls) {
                InputStream is = null;
                try {is = url.openStream();
                    if (is == null) {
                        ok = false;
                    } else {
                        process(is, out);
                    }
                } catch (IOException ex) {
                    err.println("Can't fetch url '" + url + "': " + ex.getMessage());
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
        } else if (files != null && files.length > 0) {
            for (File file : files) {
                InputStream is = null;
                try {
                    is = openFile(file, err);
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
            process(in, out);
        }
        if (out.checkError()) {
            ok = false;
        }
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
    private void process(InputStream in, PrintStream out) throws IOException {
        int len;
        final byte[] buf = new byte[BUFFER_SIZE];
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
    }

    /**
     * Attempt to open a file, writing an error message on failure.
     * @param fname the filename of the file to be opened
     * @param err where we write error messages
     * @return An open stream, or <code>null</code>.
     * @throws FileNotFoundException 
     */
    private InputStream openFile(File file, PrintStream err) throws FileNotFoundException {
        InputStream is = null;

        // FIXME we shouldn't need to these tests.  Rather, we should just open the
        // FileInputStream and print the exception message on failure.  (That assumes 
        // that the exception message is accurate and detailed!)
        if (!file.exists()) {
            err.println("File doesn't exist: '" + file + "'");
        } else if (!file.canRead()) {
            err.println("File not readable: '" + file + "'");
        } else if (file.isDirectory()) {
            err.println("Can't 'cat' a directory: '" + file + "'");
        } else {
            is = new FileInputStream(file);
        }
        return is;
    }

}
