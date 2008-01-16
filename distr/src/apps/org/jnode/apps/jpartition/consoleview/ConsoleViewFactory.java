package org.jnode.apps.jpartition.consoleview;

import java.io.InputStream;
import java.io.PrintStream;

import org.jnode.apps.jpartition.ErrorReporter;
import org.jnode.apps.jpartition.ViewFactory;
import org.jnode.shell.help.ParsedArguments;

/**
 * 
 * @author Fabien Duminy
 *
 */
public class ConsoleViewFactory implements ViewFactory {
	private final InputStream in;
	private final PrintStream out;
	private final PrintStream err;
	
	public ConsoleViewFactory(InputStream in, PrintStream out, PrintStream err)
	{
		this.in = in;
		this.out = out;
		this.err = err;
	}
	
	public Object createCommandProcessorView() {
		return null; // nothing particular to create : work is done by createDeviceView
	}

	public Object createDeviceView(ErrorReporter errorReporter,
			Object cmdProcessorView, boolean install) throws Exception {
		return new ConsoleView(in, out, errorReporter, install);
	}

	public ErrorReporter createErrorReporter() {
		return new ConsoleErrorReporter(err);
	}
}
