/*
 * $Id$
 */
package org.jnode.shell.command;

import java.lang.reflect.Field;

import javax.naming.NameNotFoundException;

import org.jnode.shell.Shell;
import org.jnode.shell.ShellUtils;
import org.jnode.shell.alias.NoSuchAliasException;
import org.jnode.shell.help.*;

/**
 * @author qades
 */
public class HelpCommand {

        static final AliasArgument ARG_COMMAND = new AliasArgument("command", "command to be described");
	static final Parameter PARAM_COMMAND = new Parameter(ARG_COMMAND, Parameter.OPTIONAL);

	public static Help.Info HELP_INFO =
		new Help.Info(
			"help",
			"Provides help to commands available in the shell",
			new Parameter[] { PARAM_COMMAND });

	public static void main(String[] args)
	throws NameNotFoundException,
            HelpException {
		Help.Info info = HELP_INFO; // defaults to print own help

                ParsedArguments cmdLine = HELP_INFO.parse(args);
		if (PARAM_COMMAND.isSet(cmdLine))
			try {
				String cmd = ARG_COMMAND.getValue(cmdLine);
				Class clazz = null;
				try {
					final Shell shell = ShellUtils.getShellManager().getCurrentShell();
					clazz = shell.getAliasManager().getAliasClass(cmd);
				} catch (NoSuchAliasException ex) {
					clazz = Class.forName(cmd);
				}
				Field clInfo = clazz.getField(Help.INFO_FIELD_NAME);
				info = (Help.Info)clInfo.get(null); // static access
			} catch (ClassNotFoundException ex) {
				System.err.println("Class not found");
			} catch (NoSuchFieldException ex) {
				System.err.println("Class does not provide requested information");
			} catch (ClassCastException ex) {
				System.err.println("Embedded information is in wrong format");
			} catch (IllegalAccessException ex) {
				System.err.println("Embedded information is not public");
			} catch (SecurityException ex) {
				System.err.println("Access to class restricted");
			}
		info.help();
	}

}
