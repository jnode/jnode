/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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
 
package org.jnode.command.common;

import java.io.PrintWriter;

import javax.naming.NameNotFoundException;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.CommandInfo;
import org.jnode.shell.CommandLine;
import org.jnode.shell.CommandShell;
import org.jnode.shell.ShellException;
import org.jnode.shell.ShellUtils;
import org.jnode.shell.alias.AliasManager;
import org.jnode.shell.alias.NoSuchAliasException;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.HelpException;
import org.jnode.shell.help.HelpFactory;
import org.jnode.shell.syntax.AliasArgument;
import org.jnode.shell.syntax.Argument;

/**
 * @author qades
 * @author Fabien DUMINY (fduminy@jnode.org)
 * @author crawley@jnode.org
 */
public class HelpCommand extends AbstractCommand {

    private static final String help_alias = "The command alias name";
    private static final String help_super = "Print online help for a command alias";
    private static final String err_no_help = "No help information is available for '%s'%n";
    private static final String err_help_ex = "Error getting help for '%s': %s%n";
    private static final String err_sec_ex = "Security exception while loading the class for '%s'%nReason: %s%n";
    private static final String fmt_other_alias = "Other aliases %s";
    
    private final AliasArgument argAlias;

    public HelpCommand() {
        super(help_super);
        argAlias = new AliasArgument("alias", Argument.OPTIONAL, help_alias);
        registerArguments(argAlias);
    }

    public static void main(String[] args) throws Exception {
        new HelpCommand().execute(args);
    }

    @Override
    public void execute() throws NameNotFoundException, ShellException, HelpException {
        // The above exceptions are either bugs or configuration errors and should be allowed
        // to propagate so that the shell can diagnose them appropriately.
        String alias;
        CommandLine commandLine = getCommandLine();
        PrintWriter out = getOutput().getPrintWriter();
        PrintWriter err = getError().getPrintWriter();
        if (argAlias.isSet()) {
            alias = argAlias.getValue();
        } else if (commandLine.getCommandName() != null) {
            alias = commandLine.getCommandName();
        } else {
            alias = "help";
        }
        CommandShell shell = null;
        try {
            shell = (CommandShell) ShellUtils.getShellManager().getCurrentShell();
            CommandInfo cmdInfo =  shell.getCommandInfo(alias);
            Help cmdHelp = HelpFactory.getHelpFactory().getHelp(alias, cmdInfo);
            if (cmdHelp == null) {
                err.format(err_no_help, alias);
                exit(1);
            }
            cmdHelp.help(out);
            otherAliases(shell.getAliasManager(), alias, cmdInfo.getCommandClass().getName(), out);
        } catch (HelpException ex) {
            err.format(err_help_ex, alias, ex.getLocalizedMessage());
            throw ex;
        } catch (ShellException ex) {
            err.println(ex.getMessage());
            throw ex;
        } catch (SecurityException ex) {
            err.format(err_sec_ex, alias, ex.getLocalizedMessage());
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
            out.format(fmt_other_alias, sb);
        }
    }
}
