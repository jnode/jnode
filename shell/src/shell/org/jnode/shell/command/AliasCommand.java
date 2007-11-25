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
 
package org.jnode.shell.command;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.TreeMap;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.CommandLine;
import org.jnode.shell.Shell;
import org.jnode.shell.ShellUtils;
import org.jnode.shell.alias.AliasManager;
import org.jnode.shell.alias.NoSuchAliasException;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.Syntax;
import org.jnode.shell.help.argument.AliasArgument;
import org.jnode.shell.help.argument.ClassNameArgument;

/**
 * @author epr
 * @author qades
 * @author Martin Husted Hartvig (hagar@jnode.org)
 */
public class AliasCommand extends AbstractCommand {

    static final AliasArgument ARG_ALIAS = new AliasArgument("alias",
            "the alias");

    static final ClassNameArgument ARG_CLASS = new ClassNameArgument(
            "classname", "the classname");

    static final Parameter PARAM_REMOVE = new Parameter("r",
            "following alias will be removed", ARG_ALIAS, Parameter.MANDATORY);

    private final static String slash_t = ":\t\t";

    public static Help.Info HELP_INFO = new Help.Info(
            "alias",
            new Syntax[] {
                    new Syntax(
                            "Print all available aliases and corresponding classnames"),
                    new Syntax("Set an aliases for given classnames",
                            new Parameter[] {
                                    new Parameter(ARG_ALIAS,
                                            Parameter.MANDATORY),
                                    new Parameter(ARG_CLASS,
                                            Parameter.MANDATORY) }),
                    new Syntax("Remove an alias",
                            new Parameter[] { PARAM_REMOVE }) });

    public static void main(String[] args) throws Exception {
        new AliasCommand().execute(args);
    }

    private static void showAliases(AliasManager aliasMgr, PrintStream out)
            throws NoSuchAliasException {
        final TreeMap<String, String> map = new TreeMap<String, String>();

        for (String alias : aliasMgr.aliases()) {
            map.put(alias, aliasMgr.getAliasClassName(alias));
        }

        for (Map.Entry<String, String> entry : map.entrySet()) {
            out.println(entry.getKey() + slash_t + entry.getValue());
        }
    }

    public void execute(CommandLine commandLine, InputStream in,
            PrintStream out, PrintStream err) throws Exception {
        ParsedArguments parsedArguments = HELP_INFO.parse(commandLine);

        final Shell shell = ShellUtils.getShellManager().getCurrentShell();
        final AliasManager aliasMgr = shell.getAliasManager();

        if (parsedArguments.size() == 0) {
            showAliases(aliasMgr, out);
        } else if (PARAM_REMOVE.isSet(parsedArguments)) {
            // remove an alias
            aliasMgr.remove(ARG_ALIAS.getValue(parsedArguments));
        } else {
            // add an alias
            aliasMgr.add(ARG_ALIAS.getValue(parsedArguments), ARG_CLASS
                    .getValue(parsedArguments));
        }
    }
}
