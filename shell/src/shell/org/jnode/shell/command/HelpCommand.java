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
import org.jnode.shell.CommandLine;
import org.jnode.shell.Shell;
import org.jnode.shell.ShellUtils;
import org.jnode.shell.alias.AliasManager;
import org.jnode.shell.alias.NoSuchAliasException;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.HelpException;
import org.jnode.shell.syntax.AliasArgument;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.ArgumentBundle;
import org.jnode.shell.syntax.SyntaxBundle;
import org.jnode.shell.syntax.SyntaxManager;

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
    public void execute() throws Exception {
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

        Help.Info info = null; 
        SyntaxBundle syntaxes = null;
        ArgumentBundle bundle = null;
        String otherAliases = null;
        try {
            final Shell shell = ShellUtils.getShellManager().getCurrentShell();
            final AliasManager aliasManager = shell.getAliasManager(); 
            final SyntaxManager syntaxManager = shell.getSyntaxManager();
            Class<?> clazz = getCommandClass(aliasManager, alias);
            bundle = getBundle(clazz, err);
            if (bundle != null) {
                syntaxes = syntaxManager.getSyntaxBundle(alias);
                if (syntaxes == null) {
                    syntaxes = new SyntaxBundle(alias, bundle.createDefaultSyntax());
                }
            } else {
                info = Help.getInfo(clazz);
            }
            if (info != null || syntaxes != null) {
                otherAliases = getOtherAliases(aliasManager, alias, clazz);
            }
        } catch (ClassNotFoundException ex) {
            err.println("Alias not found: " + alias);
            exit(1);
        } catch (SecurityException ex) {
            err.println("Access to class prevented by security manager");
            exit(2);
        } catch (NameNotFoundException ex) {
            err.println("Can't find the shell manager");
            exit(2);
        } catch (HelpException ex) {
            err.println("No help information available for alias " + alias);
            exit(1);
        }

        if (syntaxes != null) {
            Help.getHelp().help(syntaxes, bundle, out);
        } else if (info != null) {
            Help.getHelp().help(info, alias, out);
        } else {
            out.println("No help information available: " + alias);
        }
        if (otherAliases != null) {
            out.println(otherAliases);
        }
    }

    private ArgumentBundle getBundle(Class<?> clazz, PrintWriter err) {
        try {
            AbstractCommand command = (AbstractCommand) clazz.newInstance();
            return command.getArgumentBundle();
        } catch (ClassCastException e) {
            // The target class cannot 
        } catch (InstantiationException e) {
            err.println("Problem during instantiation of " + clazz.getName());
            exit(2);
        } catch (IllegalAccessException e) {
            err.println("Constructor for " + clazz.getName() + " is not accessible");
            exit(2);
        }
        return null;
    }

    private Class<?> getCommandClass(AliasManager aliasManager, String commandName)
        throws ClassNotFoundException {
        try {
            return aliasManager.getAliasClass(commandName);
        } catch (NoSuchAliasException ex) {
            // Not an alias -> assuming it's a class name
            return Class.forName(commandName);
        }
    }

    private String getOtherAliases(AliasManager aliasManager, String alias, Class<?> aliasClass) {
        boolean hasOtherAlias = false; 
        StringBuilder sb = new StringBuilder("Other aliases: ");
        boolean first = true;

        for (String otherAlias : aliasManager.aliases()) {
            // exclude alias from the returned list
            if (!otherAlias.equals(alias)) {
                try {
                    Class<?> otherAliasClass = aliasManager.getAliasClass(otherAlias);

                    if (aliasClass.equals(otherAliasClass)) {
                        // we have found another alias for the same command
                        hasOtherAlias = true;
                        if (!first) {
                            sb.append(",");
                        }
                        sb.append(otherAlias);
                        first = false;
                    }
                } catch (NoSuchAliasException nsae) {
                    // should never happen since we iterate on known aliases
                } catch (ClassNotFoundException e) {
                    // should never happen since we iterate on known aliases
                }
            }
        }

        return hasOtherAlias ? sb.toString() : null;
    }
}
