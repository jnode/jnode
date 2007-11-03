package org.jnode.shell.command;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.CommandLine;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.Syntax;
import org.jnode.shell.help.argument.OptionArgument;

/**
 * 
 * @author peda
 */
public class GrepCommand extends AbstractCommand {

	public static final int MODE_CONTAINS = 0;
	public static final int MODE_REGEXP = 1;

	
	static final OptionArgument ARG_ACTION = 
		new OptionArgument("options", "options that can be passed to grep", 
						   new OptionArgument.Option("-v", "Only output non matching lines"),
						   new OptionArgument.Option("-r", "Use java regexp syntax instead of include syntax"));

	static final Parameter PARAM_ACTION = new Parameter(ARG_ACTION);
	static final Parameter PARAM_EXPRESSION = new Parameter("expression", "the expression that should be greped for", false);
	
    public static Help.Info HELP_INFO = new Help.Info("grep", 
    		new Syntax[] { new Syntax("grep for regular expressions", PARAM_ACTION, PARAM_EXPRESSION) });
    	
    /**
     * main method, normaly not used, use execute instead!!
     * @param args
     * @throws Exception
     */
	public static void main(String[] args)
    	throws Exception {
    		new GrepCommand().execute(args);
    	}

	/**
	 * main entry point
	 * called from Shell
	 */
	public void execute(CommandLine commandLine, InputStream in,
			PrintStream out, PrintStream err) throws Exception {

		Options options = new Options();
		options.err = err;
		options.args = commandLine.getArguments();

		out.println("Start grep...");
		// parse commandline arguments...
		final boolean parseOK = parseCommandLine(options);
		if (!parseOK) {
			displayHelp(out);
			exit(2);
		}
		
		final BufferedReader r = new BufferedReader(new InputStreamReader(in));
		String line; 

		// loop over inputstream and grep for expression...
		boolean found = false;
		while ((line = r.readLine()) != null) {
			if (options.mode == MODE_CONTAINS) {
				if (line.contains(options.expression)) {
					if (!options.inverse) {
						out.println(line);
						found = true;
					}
				} else if (options.inverse) {
					out.println(line);
					found = true;
				}
			
			} else {
				if (line.matches(options.expression)) {
					if (!options.inverse) {
						out.println(line);
						found = true;
					}
				} else if (options.inverse) {
					out.println(line);
					found = true;
				}
				}
			}
		if (!found) {
			exit(1);
		}
	}
	
	private boolean parseCommandLine(Options options) {
		
		for (int i = 0; i < options.args.length; i++) {
			if (options.args[i].startsWith("-")) {
				setOptions(options.args[i], options);
				options.args[i] = null;
			}
		}

		for (int i = 0; i < options.args.length; i++) {
			if (options.args[i] != null) {
				options.expression = options.args[i];
				return true;
			}
		}
		
		return false;
	}
	
	private void setOptions(String opts, Options options) {
		if (opts.startsWith("--")) {
			// none yet
			options.err.println("unknown longopt '" + opts + "'");
		} else {
			for (int i = 1; i < opts.length(); i++) {
				switch (opts.charAt(i)) {
				case 'r':
					options.mode = MODE_REGEXP;
					break;
				case 'v':
					options.inverse = true;
					break;
				default:
					options.err.println("unknown opt '" + opts.charAt(i) + "'");
					break;
				}
			}
		}
	}
	
	private void displayHelp(PrintStream out) {
		out.println("JNode grep <options> expression");
		out.println("options:");
		out.println("  -v  inverse search, only print non matching lines");
		out.println("  -r  java regexp mode instead of contains mode");
		out.println();
	}

	private class Options {

		private int mode = 0;
		
		private boolean inverse = false;

		private String expression;
	    
		private PrintStream err;
		
		private String[] args;
	}
}
