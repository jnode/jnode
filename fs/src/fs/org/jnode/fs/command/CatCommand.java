/*
 * $Id$
 */
package org.jnode.fs.command;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.jnode.shell.help.Argument;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.FileArgument;

/**
 * @author epr
 */
public class CatCommand {

    static final Argument ARG_FILE = new FileArgument("file",
            "the file (or URL) to print out");

    public static Help.Info HELP_INFO = new Help.Info("cat",
            "Print the contents of the given file (or URL)",
            new Parameter[] { new Parameter(ARG_FILE, Parameter.MANDATORY)});

    public static void main(String[] args) throws Exception {
        final ParsedArguments cmdLine = HELP_INFO.parse(args);
        final URL url = openURL(ARG_FILE.getValue(cmdLine));
        final InputStream is = url.openStream();
        if (is == null) {
            System.err.println("Not found " + ARG_FILE.getValue(cmdLine));
        } else {
            int len;
            final byte[] buf = new byte[ 1024];
            while ((len = is.read(buf)) > 0) {
                System.out.write(buf, 0, len);
            }
            System.out.flush();
            is.close();
        }
    }

    private static URL openURL(String fname) throws MalformedURLException {
        try {
            return new URL(fname);
        } catch (MalformedURLException ex) {
            return new File(fname).toURL();
        }
    }

}
