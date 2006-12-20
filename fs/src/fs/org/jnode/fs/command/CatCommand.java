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
import java.io.InputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.jnode.shell.Command;
import org.jnode.shell.CommandLine;
import org.jnode.shell.help.Argument;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.argument.FileArgument;

/**
 * @author epr
 * @author Andreas H\u00e4nel
 */
public class CatCommand implements Command{

    static final Argument ARG_FILE = new FileArgument("file",
            "the file (or URL) to print out");

    public static Help.Info HELP_INFO = new Help.Info("cat",
            "Print the contents of the given file (or URL)",
            new Parameter[] { new Parameter(ARG_FILE, Parameter.MANDATORY)});

    public static void main(String[] args) throws Exception {
    	new CatCommand().execute(new CommandLine(args), System.in, System.out, System.err);
    }
    
    public void execute(CommandLine commandLine, InputStream in, PrintStream out, PrintStream err) throws Exception {
    	ParsedArguments cmdLine = HELP_INFO.parse(commandLine.toStringArray());
        URL url = openURL(ARG_FILE.getValue(cmdLine));
        InputStream is = url.openStream();
        if (is == null) {
            err.println("Not found " + ARG_FILE.getValue(cmdLine));
        } else {
            int len;
            final byte[] buf = new byte[ 1024];
            while ((len = is.read(buf)) > 0) {
               out.write(buf, 0, len);
            }
            out.println();
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
