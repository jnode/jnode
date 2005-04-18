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
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.jnode.shell.Command;
import org.jnode.shell.CommandLine;
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

/**
 * @author epr
 * @author qades
 * @author Martin Husted Hartvig (hagar@jnode.org)
 */
public class AliasCommand implements Command
{

  static final AliasArgument ARG_ALIAS = new AliasArgument("alias", "the alias");
	static final ClassNameArgument ARG_CLASS = new ClassNameArgument("classname", "the classname");
	static final Parameter PARAM_REMOVE = new Parameter("r", "following alias will be removed", ARG_ALIAS, Parameter.MANDATORY);

  private final static String slash_t = ":\t";

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

	public static void main(String[] args) throws Exception {
    new AliasCommand().execute(new CommandLine(args), System.in, System.out, System.err);
	}
	

	private static void showAliases(AliasManager aliasMgr, PrintStream out) throws NoSuchAliasException {
	  final TreeMap map = new TreeMap();

		for (Iterator i = aliasMgr.aliasIterator(); i.hasNext();) {
			final String alias = (String) i.next();
			map.put(alias, aliasMgr.getAliasClassName(alias));
		}
		
    StringBuffer stringBuffer;

		for (Iterator i = map.entrySet().iterator(); i.hasNext(); ) {
		  final Map.Entry entry = (Map.Entry)i.next();
      stringBuffer = new StringBuffer();
      stringBuffer.append(entry.getKey());
      stringBuffer.append(slash_t);
      stringBuffer.append(entry.getValue());

			out.println(stringBuffer.toString());

      stringBuffer = null;
		}
	}

  public void execute(CommandLine commandLine, InputStream in, PrintStream out, PrintStream err) throws Exception
  {
    ParsedArguments parsedArguments = HELP_INFO.parse(commandLine.toStringArray());

    final Shell shell = ShellUtils.getShellManager().getCurrentShell();
    final AliasManager aliasMgr = shell.getAliasManager();

    if (parsedArguments.size() == 0) {
        showAliases(aliasMgr, out);
    } else if (PARAM_REMOVE.isSet(parsedArguments)) {
      // remove an alias
      aliasMgr.remove(ARG_ALIAS.getValue(parsedArguments));
    } else {
      // add an alias
      aliasMgr.add(ARG_ALIAS.getValue(parsedArguments), ARG_CLASS.getValue(parsedArguments));
    }
  }
}
