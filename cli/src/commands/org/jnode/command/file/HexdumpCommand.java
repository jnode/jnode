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
 
package org.jnode.command.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FileArgument;
import org.jnode.shell.syntax.URLArgument;
import org.jnode.util.NumberUtils;

/**
 * @author gvt
 * @author crawley@jnode.org
 */
public class HexdumpCommand extends AbstractCommand {
    
    private static final String help_file = "the file to print out";
    private static final String help_url = "the url to print out";
    private static final String help_super = "Print a hexadecimal dump of a given file (or URL)";
    private static final String fmt_cant_open = "Cannot open %s: %s%n";
    private static final String fmt_cant_open_url = "Cannot access URL %s: %s%n";
    
    private final FileArgument argFile;
    private final URLArgument argURL;

    public HexdumpCommand() {
        super(help_super);
        argFile = new FileArgument("file", Argument.OPTIONAL | Argument.EXISTING, help_file);
        argURL  = new URLArgument("url", Argument.OPTIONAL | Argument.EXISTING, help_url);
        registerArguments(argFile, argURL);
    }

    public static void main(String[] args) throws Exception {
        new HexdumpCommand().execute(args);
    }
    
    public void execute() throws IOException {
        boolean myInput = false;
        InputStream is = null;
        PrintWriter out = getOutput().getPrintWriter(false);
        PrintWriter err = getError().getPrintWriter();
        try {
            // Set up the stream to be dumped.
            File file = argFile.getValue();
            if (argFile.isSet()) {
                try {
                    is = new FileInputStream(file);
                } catch (FileNotFoundException ex) {
                    err.format(fmt_cant_open, file, ex.getLocalizedMessage());
                    exit(1);
                }
            } else if (argURL.isSet()) {
                URL url = argURL.getValue();
                try {
                    is = url.openStream();
                } catch (IOException ex) {
                    err.format(fmt_cant_open_url, url, ex.getLocalizedMessage());
                    exit(1);
                }
            } else {
                is = getInput().getInputStream();
                myInput = true;
            }

            // Now do the work
            final int rowlen = 16;
            int prt = 0;
            int len;

            final byte[] buf = new byte[1024];
            StringBuilder sb = new StringBuilder();
            while ((len = is.read(buf)) > 0) {
                int left = len;
                int ofs = 0;
                //
                while (left > 0) {
                    sb.setLength(0);
                    int sz = Math.min(rowlen, left);

                    sb.append(NumberUtils.hex(prt, 8)).append("  ");

                    for (int i = 0; i < rowlen; i++) {
                        if (ofs + i < len) {
                            sb.append(NumberUtils.hex(buf[ofs + i], 2));
                        } else {
                            sb.append("  ");
                        }
                        if ((i + 1) < rowlen) {
                            sb.append(' ');
                        }
                        if ((i + 1) == rowlen / 2) {
                            sb.append(' ');
                        }
                    }

                    sb.append("  |");

                    for (int i = 0; i < rowlen; i++) {
                        if (ofs + i < len) {
                            char c = (char) buf[ofs + i];
                            if ((c >= ' ') && (c < (char) 0x7f)) {
                                sb.append(c);
                            } else {
                                sb.append('.');
                            }
                        } else {
                            sb.append(' ');
                        }
                    }

                    sb.append('|');

                    left -= sz;
                    ofs += sz;
                    prt += sz;

                    out.println(sb.toString());
                    out.flush();
                }
            }
            out.flush();
        } finally {
            if (is != null && myInput) {
                try {
                    is.close();
                } catch (IOException ex) {
                    /* ignore */
                }
            }
        }
    }
}
