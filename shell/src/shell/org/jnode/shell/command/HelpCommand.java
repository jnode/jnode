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

import javax.naming.NameNotFoundException;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.Shell;
import org.jnode.shell.ShellUtils;
import org.jnode.shell.alias.AliasManager;
import org.jnode.shell.alias.NoSuchAliasException;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.HelpException;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.argument.AliasArgument;
import org.jnode.shell.syntax.ArgumentBundle;
import org.jnode.shell.syntax.SyntaxBundle;
import org.jnode.shell.syntax.SyntaxManager;

/**
 * @author qades
 * @author Fabien DUMINY (fduminy@jnode.org)
 * @author crawley@jnode.org
 */
public class HelpCommand {

    static final AliasArgument ARG_COMMAND = new AliasArgument("command", Help.getLocalizedHelp("help.arg.command"));
	static final Parameter PARAM_COMMAND = new Parameter(ARG_COMMAND, Parameter.OPTIONAL);

	public static Help.Info HELP_INFO =
		new Help.Info(
			"help",
			Help.getLocalizedHelp("help.desc"),
			new Parameter[] { PARAM_COMMAND });

	public static void main(String[] args)
	throws HelpException {
		Help.Info info = null; 
		SyntaxBundle syntaxes = null;
        ArgumentBundle bundle = null;
		String otherAliases = null;
		
        ParsedArguments cmdLine = HELP_INFO.parse(args);
        String cmd = null;
        String arg = null;
        if (PARAM_COMMAND.isSet(cmdLine)){
			try {
				final Shell shell = ShellUtils.getShellManager().getCurrentShell();
                final AliasManager aliasManager = shell.getAliasManager(); 
                final SyntaxManager syntaxManager = shell.getSyntaxManager(); 

				arg = ARG_COMMAND.getValue(cmdLine);
				Class<?> clazz = getCommandClass(aliasManager, arg);	

                bundle = getBundle(clazz);
                if (bundle != null) {
                    syntaxes = syntaxManager.getSyntaxBundle(arg);
                    if (syntaxes == null) {
                        syntaxes = new SyntaxBundle(arg, bundle.createDefaultSyntax());
                    }
                }
                else {
    				info = Help.getInfo(clazz);
				}
                if (info != null || syntaxes != null) {
                    cmd = arg;
                    otherAliases = getOtherAliases(aliasManager, cmd, clazz);
                }
			} catch (ClassNotFoundException ex) {
				System.err.println("Command not found: " + arg);
			} catch (SecurityException ex) {
				System.err.println("Access to class prevented by security manager");
			} catch (NameNotFoundException e) {
				System.err.println("Can't find the shell manager");
			}
        }
        else {
            info = HELP_INFO; // defaults to print own help
		}
 
        if (syntaxes != null) {
            Help.getHelp().help(syntaxes, bundle, System.out);
        }
        else if (info != null) {
            Help.getHelp().help(info, cmd, System.out);
        }
        if (otherAliases != null) {
            System.out.println(otherAliases);
        }
	}

	private static ArgumentBundle getBundle(Class<?> clazz) {
        try {
            AbstractCommand command = (AbstractCommand) clazz.newInstance();
            return command.getArgumentBundle();
        } catch (ClassCastException e) {
            return null;
        } catch (InstantiationException e) {
            System.err.println("Problem during instantiation of " + clazz.getName());
        } catch (IllegalAccessException e) {
            System.err.println("Constructor for " + clazz.getName() + " is not accessible");
        }
        return null;
    }
	
	private static Class<?> getCommandClass(AliasManager aliasManager, String commandName)
	throws ClassNotFoundException
	{
	    try {
	        return aliasManager.getAliasClass(commandName);
	    }
	    catch (NoSuchAliasException ex) {
	        // Not an alias -> assuming it's a class name
	        return Class.forName(commandName);
	    }
	}

    private static String getOtherAliases(AliasManager aliasManager, String alias, Class<?> aliasClass)
	{
		boolean hasOtherAlias = false; 
		StringBuilder sb = new StringBuilder("Other aliases: ");
		boolean first = true;
		
		for(String otherAlias : aliasManager.aliases()) {
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
