/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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
 
package org.jnode.command.net;

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
    
    private static final String help_debug = "if set, output debug information";
    private static final String help_url = "the source URL(s)";
    private static final String help_super = "Fetch the contents of one or more URLs.";
    private static final String fmt_get_file = "Getting file %s from url %s%n";
    private static final String err_get_file = "Cannot get %s: %s%n";
    private static final String err_no_file = "Could not figure out a local file name for URL %s%n";
    
    private final FlagArgument argDebug;
    private final URLArgument argUrl;

    private PrintWriter out;
    private PrintWriter err;
    
    public WgetCommand() {
        super(help_super);
        argDebug = new FlagArgument("debug", Argument.OPTIONAL, help_debug);
        argUrl   = new URLArgument("url", Argument.MANDATORY + Argument.MULTIPLE, help_url);
        registerArguments(argUrl, argDebug);
    }

    public static void main(String[] args) throws Exception {
        new WgetCommand().execute(args);
    }
    
    @Override
    public void execute() throws Exception {
        this.out = getOutput().getPrintWriter();
        this.err = getError().getPrintWriter();
        boolean debug = argDebug.isSet();
        int errorCount = 0;
        for (URL url : argUrl.getValues()) {
            String filename = getLocalFileName(url);
            if (filename == null) {
                errorCount++;
            } else {
                try {
                    out.format(fmt_get_file, filename, url.toString());
                    get(url, filename);
                } catch (IOException ex) {
                    if (debug) {
                        ex.printStackTrace(err);
                    }
                    err.format(err_get_file, filename, ex.getLocalizedMessage());
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
            err.format(err_no_file, url.toString());
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
