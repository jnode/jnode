/*
 *
 */

package org.jnode.fs.command;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.CommandLine;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FileArgument;
import org.jnode.shell.syntax.URLArgument;
import org.jnode.util.NumberUtils;

/**
 * @author gvt
 * @author crawley@jnode.org
 */
public class HexdumpCommand extends AbstractCommand {
    private final FileArgument ARG_FILE = new FileArgument(
            "file", Argument.OPTIONAL, "the file to print out");

    private final URLArgument ARG_URL = new URLArgument(
            "url", Argument.OPTIONAL, "the url to print out");

    public HexdumpCommand() {
        super("Print a hexadecimal dump of a given file (or URL)");
        registerArguments(ARG_FILE, ARG_URL);
    }

    public static void main(String[] args) throws Exception {
        new HexdumpCommand().execute(args);
    }

    public void execute(CommandLine commandLine, InputStream in, PrintStream out, PrintStream err) 
        throws IOException {
        InputStream is = null;
        try {
            // Set up the stream to be dumped.
            File file = ARG_FILE.getValue();
            if (ARG_FILE.isSet()) {
                try {
                    is = new FileInputStream(file);
                } catch (FileNotFoundException ex) {
                    err.println("Cannot open " + file + ": " + ex.getMessage());
                    exit(1);
                }
            } else if (ARG_URL.isSet()) {
                URL url = ARG_URL.getValue();
                try {
                    is = url.openStream();
                } catch (IOException ex) {
                    err.println("Cannot access URL '" + url + "': " + ex.getMessage());
                    exit(1);
                }
            } else {
                is = in;
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
                            sb.append(" ");
                        }
                        if ((i + 1) == rowlen / 2) {
                            sb.append(" ");
                        }
                    }

                    sb.append("  |");

                    for (int i = 0; i < rowlen; i++) {
                        if (ofs + i < len) {
                            char c = (char) buf[ofs + i];
                            if ((c >= ' ') && (c < (char) 0x7f)) {
                                sb.append(c);
                            } else {
                                sb.append(".");
                            }
                        } else {
                            sb.append(" ");
                        }
                    }

                    sb.append("|");

                    left -= sz;
                    ofs += sz;
                    prt += sz;

                    out.println(sb.toString());
                    out.flush();
                }
            }
            out.flush();
        } finally {
            if (is != null && is != in) {
                try {
                    is.close();
                } catch (IOException ex) {
                    /* ignore */
                }
            }
        }
    }
}
