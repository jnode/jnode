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
import org.jnode.shell.syntax.*;

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
        super("Read the argument files or urls, copying their contents to standard output.  " +
                "If there are no arguments, standard input is read until EOF is reached; " +
                "e.g. ^D when reading keyboard input.");
        registerArguments(ARG_FILE, ARG_URL, FLAG_URLS);
    }

    private static final int BUFFER_SIZE = 1024;


    public static void main(String[] args) throws Exception {
        new CatCommand().execute(args);
    }

    public void execute(CommandLine commandLine, InputStream in, PrintStream out, PrintStream err) 
    throws Exception {
        File[] files = ARG_FILE.getValues();
        String[] urls = ARG_URL.getValues();
        boolean ok = true;
        try {
            if (urls != null && urls.length > 0) {
                for (String urlString : urls) {
                    InputStream is = null;
                    try {
                        URL url = new URL(urlString);
                        is = url.openStream();
                        if (is == null) {
                            ok = false;
                        }
                        else {
                            process(is, out);
                        }
                    } catch (MalformedURLException ex) {
                        err.println("Malformed url '" + urlString + "': " + ex.getMessage());
                    } catch (IOException ex) {
                        err.println("Can't fetch url '" + urlString + "': " + ex.getMessage());                                        
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
            }
            else if (files != null && files.length > 0) {
                for (File file : files) {
                    InputStream is = null;
                    try {
                        is = openFile(file, err);
                        if (is == null) {
                            ok = false;
                        }
                        else {
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
            }
            else {
                process(in, out);
            }
            if (out.checkError()) {
                ok = false;
            }
        } catch (IOException ex) {
            // Deal with i/o errors reading from in/is or writing to out.
            err.println("Problem concatenating file(s): " + ex.getMessage());
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
     */
    private InputStream openFile(File file, PrintStream err) {
        InputStream is = null;

        try {
            // FIXME we shouldn't be doing these tests.  Rather, we should be
            // just trying to create the FileInputStream and printing the 
            // exception message on failure.  (That assumes that the exception
            // message is accurate!)
            if (!file.exists()) {
                err.println("File doesn't exist: '" + file + "'");
            }
            else if (file.isDirectory()) {
                err.println("Can't 'cat' a directory: '" + file + "'");
            }
            else {
                is = new FileInputStream(file);
            }
        } catch (FileNotFoundException ex) {
            // should never happen since we check for existence before
        }

        return is;
    }

}
