/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
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
 
package org.jnode.net.command;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FlagArgument;
import org.jnode.shell.syntax.URLArgument;

public class WgetCommand extends AbstractCommand {
    private static final int BUFFER_SIZE = 8192;
    
    private final FlagArgument ARG_DEBUG =
        new FlagArgument("debug", Argument.OPTIONAL, "if set, output debug information");
    private final URLArgument ARG_SOURCE = 
        new URLArgument("url", Argument.MANDATORY + Argument.MULTIPLE, "the source URL(s)");

    private PrintWriter out;
    private PrintWriter err;
    
    public WgetCommand() {
        super("Fetch the contents of one or more URLs.");
        registerArguments(ARG_SOURCE, ARG_DEBUG);
    }

    public static void main(String[] args) throws Exception {
        new WgetCommand().execute(args);
    }

    @Override
    public void execute() throws Exception {
        this.out = getOutput().getPrintWriter();
        this.err = getError().getPrintWriter();
        boolean debug = ARG_DEBUG.isSet();
        int errorCount = 0;
        for (URL url : ARG_SOURCE.getValues()) {
            String filename = getLocalFileName(url);
            if (filename == null) {
                errorCount++;
            } else {
                try {
                    out.println("Getting file " + filename + " from url " + url.toString());
                    get(url, filename);
                } catch (IOException ex) {
                    if (debug) {
                        ex.printStackTrace(err);
                    }
                    err.println("Cannot get " + filename + ": " + ex.getMessage());
                    errorCount++;
                }
            }
        }
        exit(errorCount > 0 ? 1 : 0);
    }

    /**
     * Extract file name from url to save it locally.
     * 
     * @param url the URL we need to fetch
     * @return an appropriate local filename
     * 
     * @throws Exception
     */
    protected String getLocalFileName(URL url) {
        String address = url.toString();
        int lastSlashIndex = address.lastIndexOf('/');
        if (lastSlashIndex >= 0 && lastSlashIndex < address.length() - 1) {
            return address.substring(lastSlashIndex + 1);
        } else {
            err.println("Could not figure out a local file name for URL " + url);
            return null;
        }
    }

    /**
     * Use the URL's protocol handler to fetch the URL's contents and write it
     * to the local file.
     * 
     * @param url the URL to be fetched
     * @param localFileName the name of the destination file
     * 
     * @throws IOException
     */
    protected void get(URL url, String localFileName) throws IOException {
        InputStream is = null;
        FileOutputStream os = null;
        try {
            is = url.openStream();
            os = new FileOutputStream(localFileName);
            byte[] buffer = new byte[BUFFER_SIZE];
            int numRead;
            long numWritten = 0;
            while ((numRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, numRead);
                numWritten += numRead;
            }
            is.close();
        } finally {
            if (is != null) {
                is.close();
            }
            if (os != null) {
                os.close();
            }
        }
    }
}
