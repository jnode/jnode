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
import  java.io.PrintStream;
import  javax.naming.NameNotFoundException;

import org.jnode.driver.console.InputHistory;
import  org.jnode.shell.help.*;
import org.jnode.shell.help.argument.OptionArgument;
import  org.jnode.shell.Shell;
import  org.jnode.shell.ShellUtils;

/** A shell command to access the data in the CommandHistory class.
 *  @author Matt Paine
 */
public class HistoryCommand {

//**********  HELP_INFO  **********//


	public static final OptionArgument ARG_HISTORYARG = new OptionArgument("history arg", "The argument for the history",
				new OptionArgument.Option[]
				{
					new OptionArgument.Option("string", "The command to search for"),
					new OptionArgument.Option("int", "The index of the command to execute")
				});


	public static final Help.Info HELP_INFO = new Help.Info("history", 
				new Syntax[]
					{
						//  history
						new Syntax("List the current archive of commands", new Parameter[]
						{
							new Parameter("h", "Displays the usage message", Parameter.OPTIONAL)
						}),
						new Syntax("Executes a historical command", new Parameter[]
						{
							new Parameter("t", "Test the history argument (does not execute)", Parameter.OPTIONAL),
							new Parameter(ARG_HISTORYARG, Parameter.MANDATORY)
						})
					});


//**********  Private Variables  **********//

	/** Reference to the current shell. **/
	private Shell shell;

	/** Sends all output to this PrintStream. **/
	private PrintStream out;

	/** Reference to the CommandHistory to work with. **/
	private InputHistory history;


//**********  Constructor  **********//

	/** Sets up any instance variables and processes the command arguments. 
	 *  @param args The arguments to work with.
	 **/
	public HistoryCommand (String[] args) 
	throws NameNotFoundException {
		shell = ShellUtils.getShellManager().getCurrentShell();
		history = shell.getCommandHistory();
		out = System.out;

		// no arguments prints all history
		if (args.length == 0)
			listCommands();

		// -h displays usage
		else if ("-h".equals(args[0]))
			printUsage();

		// -t tests the next argument for a match
		else if ("-t".equals(args[0])) {
			if (args.length != 2)
				printUsage();
			else
				printTest(args[1]);
		}

		// If there are more than 1 argument then display the useage
		else if (args.length != 1)
			printUsage();

		// Try executing the historical command based on the argument
		else
			executeHistoryCommand(args[0]);
	}


//**********  Public Methods  **********//

	/** List out every command from the history, with each commands index. **/
	public void listCommands() {
		for (int x = 0; x < history.size(); x++)
			out.println("" + x + ": " + history.getLineAt(x));
		out.println();
	}

	/** Tests the given argument to see if it is found in the history.
	 *  The found command is displayed.
	 *  @param arg The argument to test for.
	 **/
	public void printTest(String arg) {
		String line = parseCommandArg(arg);
		if (line == null)
			out.println("No command found!");
		else
			out.println(line);
		out.println();
	}

	/** Executes the command based on the given argument.
	 *  TODO: This method needs implementing.
	 *  @param arg The argument to parse and execute.
	 **/
	public void executeHistoryCommand (String arg) {
		//TODO
		out.println("Not Implemented Yet (try -t switch to test the command)");
	}


	/** Write the usage information of this command into the given stream. **/
	public void printUsage() {
		out.println("Usage:");
		out.println("\thistory    : List all the current archived commands, showing the index");
		out.println("\t             number of each command.");
		out.println();
		out.println("\thistory -h : Displays this usage message.");
		out.println();
		out.println("\thistory 4  : If an index number is used, that number is used to execute the");
		out.println("\t             historical command with that index.");
		out.println();
		out.println("\thistory di : If a string is used, the last command used that starts with");
		out.println("\t             that string is executed.E.g. This command may execute the last");
		out.println("\t             \"dir\" command if no other command starting with \"di\" has been");
		out.println("\t             executed since the last dir.");
		out.println();
		out.println("\thistory -t [index | string]</b> ");
		out.println("\t           : Instead of executing the archived command");
		out.println("\t             (at the given index, or starting with the search string), test");
		out.println("\t             the command by printing the command line to the output.");
		out.println();
	}


//**********  Private Methods  **********//

	/** Parses the argument to get the historical command.
	 *  This method works out if the argument is an index number or a search
	 *  string and returns the command based on that deduction.
	 *  @param arg The argument to parse.
	 *  @return The CommandLine object returned from the CommandHistory based
	 *          on the parsed argument.
	 **/
	private String parseCommandArg(String arg) {
		try {
			int i = Integer.parseInt(arg);
			return history.getLineAt(i);
		}
		catch (NumberFormatException nfex) {
			return history.getLineWithPrefix(arg);
		}
	}


//**********  Main  **********//

	/** Main method. This method creates the HistoryCommand based on the arguments.
	 *  <p>Usage:</p>
	 *  <ul><li><b>history</b> : List all the current archived commands,
	 *          showing the index number of each command.</li>
	 *      <li><b>history -h</b> : Displays a short help message.</li>
	 *      <li><b>history 4</b> : If an index number is used, that number is
	 *          used to execute the historical command with that index.</li>
	 *      <li><b>history di</b> : If a string is used, the last command used
	 *          that starts with that string is executed. E.g. This command
	 *          may execute the last "dir" command if no other command
	 *          starting with "di" has been executed since the last dir.</li>
	 *      <li><b>history -t [index | string]</b> : Instead of executing the
	 *          archived command (at the given index, or starting with the
	 *          search string), test the command by printing the command line
	 *          to the output.
	 *  </ul>
	 *  @param args The arguments for this command as outlined in the description.
	 **/
	public static void main(String[] args)
	throws NameNotFoundException {
		new HistoryCommand(args);
	}

}
