/*
 * $Id: CatCommand.java 3603 2007-11-25 21:43:50Z lsantha $
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

package org.jnode.test.shell;

import java.io.InputStream;
import java.io.PrintStream;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.CommandLine;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.Syntax;
import org.jnode.shell.help.argument.FileArgument;
import org.jnode.shell.help.argument.URLArgument;

/**
 * Cut down test class
 */
public class MyCatCommand extends AbstractCommand {

    static final FileArgument ARG_FILE = new FileArgument("file",
        "the files to be concatenated", true);

    static final URLArgument ARG_URL = new URLArgument("url",
        "the files to be concatenated", true);

    public static Help.Info HELP_INFO = new Help.Info("cat",
        new Syntax[]{
            new Syntax(
                "Fetch the argument urls and copy their contents to standard output.",
                new Parameter[]{
                    new Parameter("u",
                        "selects urls rather than pathnames",
                        ARG_URL, Parameter.MANDATORY)}),
            new Syntax(
                "Read the argument files, copying their contents to standard output.  " +
                    "If there are no arguments, standard input is read until EOF is reached; " +
                    "e.g. ^D when reading keyboard input.",
                new Parameter[]{
                    new Parameter(ARG_FILE, Parameter.OPTIONAL)})

        });

    public void execute(CommandLine commandLine, InputStream in, PrintStream out, PrintStream err)
        throws Exception {
    }

}
