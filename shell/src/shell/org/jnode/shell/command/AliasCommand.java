/*
 * $Id$
 */
package org.jnode.shell.command;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.naming.NameNotFoundException;

import org.jnode.shell.Shell;
import org.jnode.shell.ShellUtils;
import org.jnode.shell.alias.AliasManager;
import org.jnode.shell.alias.NoSuchAliasException;
import org.jnode.shell.help.AliasArgument;
import org.jnode.shell.help.ClassNameArgument;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.Syntax;
import org.jnode.shell.help.SyntaxErrorException;

/**
 * @author epr
 * @author qades
 */
public class AliasCommand {

        static final AliasArgument ARG_ALIAS = new AliasArgument("alias", "the alias");
	static final ClassNameArgument ARG_CLASS = new ClassNameArgument("classname", "the classname");
	static final Parameter PARAM_REMOVE = new Parameter("r", "following alias will be removed", ARG_ALIAS, Parameter.MANDATORY);

	public static Help.Info HELP_INFO = new Help.Info(
		"alias", new Syntax[] {
			new Syntax("Print all available aliases and corresponding classnames"),
			new Syntax("Set an aliases for given classnames",
				new Parameter[] {
					new Parameter(ARG_ALIAS, Parameter.MANDATORY),
					new Parameter(ARG_CLASS, Parameter.MANDATORY)
				}
			), new Syntax("Remove an alias",
				new Parameter[] {
					PARAM_REMOVE
				}
			)
		});

	public static void main(String[] args) throws NoSuchAliasException, NameNotFoundException, SyntaxErrorException {
		ParsedArguments cmdLine = HELP_INFO.parse(args);

		final Shell shell = ShellUtils.getShellManager().getCurrentShell();
		final AliasManager aliasMgr = shell.getAliasManager();

		if (cmdLine.size() == 0) {
		    showAliases(aliasMgr);
		} else if (PARAM_REMOVE.isSet(cmdLine)) {
			// remove an alias
			aliasMgr.remove(ARG_ALIAS.getValue(cmdLine));
		} else {
			// add an alias
			aliasMgr.add(ARG_ALIAS.getValue(cmdLine), ARG_CLASS.getValue(cmdLine));
		}
	}
	
	private static void showAliases(AliasManager aliasMgr) throws NoSuchAliasException {
	    final TreeMap map = new TreeMap();
		for (Iterator i = aliasMgr.aliasIterator(); i.hasNext();) {
			final String alias = (String) i.next();
			final String clsName = aliasMgr.getAliasClassName(alias);
			map.put(alias, clsName);
		}
		
		for (Iterator i = map.entrySet().iterator(); i.hasNext(); ) {
		    final Map.Entry entry = (Map.Entry)i.next();
			System.out.println(entry.getKey() + ":\t" + entry.getValue());
		}	    
	}
}
