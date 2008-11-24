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

import java.io.PrintWriter;

import javax.naming.NameNotFoundException;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.CommandInfo;
import org.jnode.shell.CommandLine;
import org.jnode.shell.CommandShell;
import org.jnode.shell.ShellUtils;
import org.jnode.shell.alias.AliasManager;
import org.jnode.shell.alias.NoSuchAliasException;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.HelpFactory;
import org.jnode.shell.help.HelpException;
import org.jnode.shell.syntax.AliasArgument;
import org.jnode.shell.syntax.Argument;

/**
 * @author qades
 * @author Fabien DUMINY (fduminy@jnode.org)
 * @author crawley@jnode.org
 */
public class HelpCommand extends AbstractCommand {

    private final AliasArgument ARG_ALIAS = 
        new AliasArgument("alias", Argument.OPTIONAL, "The command alias name");

    public HelpCommand() {
        super("Print online help for a command alias");
        registerArguments(ARG_ALIAS);
    }

    public static void main(String[] args) throws Exception {
        new HelpCommand().execute(args);
    }

    @Override
    public void execute() throws NameNotFoundException, ClassNotFoundException, 
        HelpException, NoSuchAliasException {
        // The above exceptions are either bugs or configuration errors and should be allowed
        // to propagate so that the shell can diagnose them appropriately.
        String alias;
        CommandLine commandLine = getCommandLine();
        PrintWriter out = getOutput().getPrintWriter();
        PrintWriter err = getError().getPrintWriter();
        if (ARG_ALIAS.isSet()) {
            alias = ARG_ALIAS.getValue();
        } else if (commandLine.getCommandName() != null) {
            alias = commandLine.getCommandName();
        } else {
            alias = "help";
        }
        CommandShell shell = null;
        try {
            shell = (CommandShell) ShellUtils.getShellManager().getCurrentShell();
            CommandInfo cmdInfo =  shell.getCommandInfo(alias);
            Help cmdHelp = HelpFactory.getHelpFactory().getHelp(alias, shell.getCommandInfo(alias));
            if (cmdHelp == null) {
                err.println("No help information is available for alias / class '" + alias + "'");
                exit(1);
            }
            cmdHelp.help(out);
            otherAliases(shell.getAliasManager(), alias, cmdInfo.getCommandClass().getName(), out);
        } catch (HelpException ex) {
            err.println("Error getting help for alias / class '" + alias + "': " + ex.getMessage());
            throw ex;
        } catch (ClassNotFoundException ex) {
            try {
                String className = shell.getAliasManager().getAliasClassName(alias);
                err.println("Cannot load class '" + className + "' for alias '" + alias + "'");
            } catch (NoSuchAliasException ex2) {
                err.println("'" + alias + "' is neither an alias or a loadable class name");
            }
            throw ex;
        } catch (SecurityException ex) {
            err.println("Security exception while loading the class associated with alias '" + alias + "'");
            err.println("Reported reason: " + ex.getLocalizedMessage());
            throw ex;
        }
    }

    private void otherAliases(AliasManager aliasManager, String thisAlias, 
            String aliasClass, PrintWriter out) throws NoSuchAliasException {
        // NoSuchAliasException indicates a bug, and should be allowed to propagate.
        StringBuilder sb = new StringBuilder();

        for (String otherAlias : aliasManager.aliases()) {
            // exclude thisAlias from the output
            if (!otherAlias.equals(thisAlias)) {
                String otherAliasClass = aliasManager.getAliasClassName(otherAlias);
                // System.err.println("comparing '" + aliasClass + "' with '" + otherAliasClass + "'");
                if (aliasClass.equals(otherAliasClass)) {
                    // we have found another alias for the same command
                    if (sb.length() > 0) {
                        sb.append(", ");
                    }
                    sb.append(otherAlias);
                }
            }
        }

        if (sb.length() > 0) {
            out.println("Other aliases: " + sb);
        }
    }
}
