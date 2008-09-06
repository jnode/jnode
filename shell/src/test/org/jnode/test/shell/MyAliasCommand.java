/*
 * $Id: AliasCommand.java 3604 2007-11-25 21:44:48Z lsantha $
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
import org.jnode.shell.syntax.AliasArgument;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.ClassNameArgument;

/**
 * Stripped down for testing ... new syntax
 */
public class MyAliasCommand extends AbstractCommand {

    private final AliasArgument ARG_ALIAS;
    private final ClassNameArgument ARG_CLASS;
    private final AliasArgument ARG_REMOVE;

    public MyAliasCommand() {
        super("list, set or remove JNode command aliases");
        ARG_ALIAS = new AliasArgument("alias", Argument.OPTIONAL, "the alias to be added");
        ARG_CLASS = new ClassNameArgument("classname", Argument.OPTIONAL, "the classname");
        ARG_REMOVE = new AliasArgument("remove", Argument.OPTIONAL, "the alias to be removed");
        registerArguments(ARG_ALIAS, ARG_CLASS, ARG_REMOVE);
    }

    public void execute(CommandLine commandLine, InputStream in,
                        PrintStream out, PrintStream err) throws Exception {
    }
}
