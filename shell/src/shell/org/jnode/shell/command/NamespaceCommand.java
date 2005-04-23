/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */

package org.jnode.shell.command;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Set;

import org.jnode.naming.InitialNaming;
import org.jnode.shell.CommandLine;
import org.jnode.shell.help.Help;

/**
 * @author epr
 */
public class NamespaceCommand {

    public static Help.Info HELP_INFO = new Help.Info("namespace",
            "Print the contents of the system namespace");

    public static void main(String[] args) throws Exception {
        new NamespaceCommand().execute(new CommandLine(args), System.in,
                System.out, System.err);
    }

    /**
     * Execute this command
     */
    public void execute(CommandLine cmdLine, InputStream in, PrintStream out,
            PrintStream err) throws Exception {

        Set<Class< ? >> names = InitialNaming.nameSet();
        for (Class< ? > name : names) {
            out.println(name);
        }
    }

}
