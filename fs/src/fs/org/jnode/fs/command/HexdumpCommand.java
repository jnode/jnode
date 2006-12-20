/*
 *
 */

package org.jnode.fs.command;

import org.jnode.shell.Command;
import org.jnode.shell.CommandLine;
import org.jnode.shell.help.Argument;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.argument.FileArgument;
import org.jnode.util.NumberUtils;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author gvt
 */
public class HexdumpCommand implements Command {
    static final Argument ARG_FILE = new FileArgument("file",
            "the file (or URL) to print out");

    public static Help.Info HELP_INFO = new Help.Info("hexdump",
            "hexadecimal dump of the given file (or URL)",
            new Parameter[]{new Parameter(ARG_FILE, Parameter.MANDATORY)});

    public static void main(String[] args) throws Exception {
        new HexdumpCommand().execute(new CommandLine(args), System.in, System.out, System.err);
    }


    public void execute(CommandLine commandLine, InputStream in, PrintStream out, PrintStream err) throws Exception {
        ParsedArguments cmdLine = HELP_INFO.parse(commandLine.toStringArray());
        URL url = openURL(ARG_FILE.getValue(cmdLine));
        InputStream is = url.openStream();

        if (is == null) {
            err.println("Not found " + ARG_FILE.getValue(cmdLine));
        } else {
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
                        if (ofs + i < len)
                            sb.append(NumberUtils.hex(buf[ofs + i], 2));
                        else
                            sb.append("  ");
                        if ((i + 1) < rowlen)
                            sb.append(" ");
                        if ((i + 1) == rowlen / 2)
                            sb.append(" ");
                    }

                    sb.append("  |");

                    for (int i = 0; i < rowlen; i++) {
                        if (ofs + i < len) {
                            char c = (char) buf[ofs + i];
                            if ((c >= ' ') && (c < (char) 0x7f))
                                sb.append(c);
                            else
                                sb.append(".");
                        } else
                            sb.append(" ");
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
            is.close();
        }
    }

    private URL openURL(String fname) throws MalformedURLException {
        try {
            return new URL(fname);
        } catch (MalformedURLException ex) {
            return new File(fname).toURL();
        }
    }

}
