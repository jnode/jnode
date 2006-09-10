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

import org.jnode.shell.help.*;
import org.jnode.shell.Command;
import org.jnode.shell.CommandLine;

import java.io.InputStream;
import java.io.PrintStream;


/**
 * Shell command to set property values.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author Martin Husted Hartvig (hagar@jnode.org)
 * @author Levente S\u00e1ntha
 */

public class SetCommand implements Command{

	static final PropertyNameArgument ARG_KEY = new PropertyNameArgument("key", "the property name");
	static final StringArgument ARG_VALUE = new StringArgument("value", "the value to set the property to, if missing the property is removed");

  public static final Help.Info HELP_INFO = new Help.Info(
		"set", "Sets or removes a system property.",
		new Parameter[]{
			new Parameter(ARG_KEY, Parameter.MANDATORY),
			new Parameter(ARG_VALUE, Parameter.OPTIONAL),
		}
	);

	public static void main(String[] args) throws Exception {
    new SetCommand().execute(new CommandLine(args), System.in, System.out, System.err);
	}

  public void execute(CommandLine commandLine, InputStream in, PrintStream out, PrintStream err) throws Exception
  {
    ParsedArguments parsedArguments = HELP_INFO.parse(commandLine.toStringArray());

      String value = ARG_VALUE.getValue(parsedArguments);
      if(value == null)
        System.getProperties().remove(ARG_KEY.getValue(parsedArguments));
      else
        System.getProperties().setProperty(ARG_KEY.getValue(parsedArguments), value);
  }
}
