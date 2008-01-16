package org.jnode.apps.jpartition.consoleview;

import java.io.PrintStream;

import org.jnode.apps.jpartition.ErrorReporter;

/**
 * 
 * @author Fabien Duminy
 *
 */
class ConsoleErrorReporter extends ErrorReporter {
	private final PrintStream err;
	
	ConsoleErrorReporter(PrintStream err)
	{
		this.err = err;
	}
	
	protected void displayError(Object source, String message)
	{
		StringBuilder sb = new StringBuilder();
		if(source != null)
		{
			sb.append('[').append(String.valueOf(source)).append("] ");
		}
		sb.append(message);
		err.println(sb.toString());
	}
}
