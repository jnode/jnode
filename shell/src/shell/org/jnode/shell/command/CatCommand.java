/*
 * $Id: CatCommand.java 4975 2009-02-02 08:30:52Z lsantha $
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.MalformedURLException;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.StringArgument;

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

    private static final String help_sources = "A list of data sources, either files or urls to remote files";
    
    private final StringArgument Sources = new StringArgument("sources", Argument.MULTIPLE, help_sources);
                
    private PrintWriter err;
    
    public CatCommand() {
        super("Concatenate the contents of files, urls or standard input to standard output");
        registerArguments(Sources);
    }

    private static final int BUFFER_SIZE = 8192;


    public static void main(String[] args) throws Exception {
        new CatCommand().execute(args);
    }

    public void execute() throws IOException {
        this.err = getError().getPrintWriter();
        OutputStream out = getOutput().getOutputStream();
        
        if (!Sources.isSet()) {
            process(getInput().getInputStream(), out);
            return;
        }
        
        boolean ok = true;
        URL url;
        File file;
        InputStream in;
        
        for (String source : Sources.getValues()) {
            in = null;
            if (source.equals("-")) {
                process(getInput().getInputStream(), out);
                continue;
            }
            
            try {
                url = new URL(source);
                in = openURL(url);
            } catch (MalformedURLException e) {
                file = new File(source);
                if (file.exists()) {
                    in = openFile(file);
                } else {
                    err.println("Malformed url, or non-existant file: " + source);
                }
            }
            
            if (in != null) {
                process(in, out);
                try {
                    in.close();
                } catch (IOException e) {
                    /* ignore */
                }
            } else {
                ok = false;
            }
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
     * Attempt to open a url, writing an error message on failure.
     * @param url the url of the remote source to be opened.
     * @return An open stream, or <code>null</code>
     */
    private InputStream openURL(URL url) {
        try {
            return url.openStream();
        } catch (IOException ex) {
            err.println("Cannot open url '" + url + "': " + ex.getLocalizedMessage());
            return null;
        }
    }
    
    /**
     * Attempt to open a file, writing an error message on failure.
     * @param fname the filename of the file to be opened
     * @return An open stream, or <code>null</code>. 
     */
    private InputStream openFile(File file) {
        try {
            return new FileInputStream(file);
        } catch (IOException ex) {
            err.println("Cannot open file '" + file + "': " + ex.getLocalizedMessage());
            return null;
        }
    }

}
