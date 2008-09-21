/*
 * $Id: TftpCommand.java 4213 2008-06-08 02:02:10Z crawley $
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
package org.jnode.net.command;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.CommandLine;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FlagArgument;
import org.jnode.shell.syntax.URLArgument;

public class WgetCommand extends AbstractCommand {
    private final FlagArgument ARG_DEBUG =
        new FlagArgument("debug", Argument.OPTIONAL, "if set, output debug information");
    private final URLArgument ARG_SOURCE = 
        new URLArgument("url", Argument.MANDATORY + Argument.MULTIPLE, "the source URL(s)");

    private PrintStream out;
    private PrintStream err;
    
    public WgetCommand() {
        super("Fetch the contents of one or more URLs.");
        registerArguments(ARG_SOURCE, ARG_DEBUG);
    }

    public static void main(String[] args) throws Exception {
        new WgetCommand().execute(args);
    }

    @Override
    public void execute(CommandLine commandLine, InputStream in,
            PrintStream out, PrintStream err) throws Exception {
        this.out = out;
        this.err = err;
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
        BufferedOutputStream bos = null;
        try {
            is = url.openStream();
            bos = new BufferedOutputStream(new FileOutputStream(localFileName));
            byte[] buffer = new byte[8192];
            int numRead;
            long numWritten = 0;
            while ((numRead = is.read(buffer)) != -1) {
                bos.write(buffer, 0, numRead);
                numWritten += numRead;
            }
            is.close();
        } finally {
            if (is != null) {
                is.close();
            }
            if (bos != null) {
                bos.close();
            }
        }
    }
}
