/*
 * $Id$
 */
package org.jnode.shell.help;

import java.lang.reflect.Field;

import javax.naming.NamingException;

import org.jnode.naming.InitialNaming;
import org.jnode.shell.CommandLine;

/**
 * @author qades
 */
public abstract class Help {

	public static final Class NAME = Help.class;//"system/help";
	public static final String INFO_FIELD_NAME = "HELP_INFO";

	public static Help getHelp() throws HelpException {
		try {
			return (Help) InitialNaming.lookup(NAME);
		} catch (NamingException ex) {
			throw new HelpException("Help application not found");
		}
	}

	public static Info getInfo(Class clazz) throws HelpException {
		try {
			Field helpInfo = clazz.getField(INFO_FIELD_NAME);
			return (Help.Info) helpInfo.get(null); // static access
		} catch (NoSuchFieldException ex) {
			throw new HelpException("Command information not found");
		} catch (IllegalAccessException ex) {
			throw new HelpException("Command information not accessible");
		}
	}
	/**
	 * Shows the help page for a command
	 * 
	 * @param info
	 *            the command information
	 */
	public abstract void help(Info info);

	/**
	 * Shows the usage line for a command
	 * 
	 * @param info
	 *            the command information
	 */
	public abstract void usage(Info info);

	/**
	 * Shows the description of a single argument. Used as a callback in {@link Argument#describe(Help)}.
	 */
	public abstract void describeArgument(Argument arg);

	/**
	 * Shows the description of a single parameter. Used as a callback in
	 * {@link Parameter#describe(Help)}.
	 */
	public abstract void describeParameter(Parameter param);

	/**
	 * Here is where all the command description goes. Example code: <br>
	 * 
	 * <pre>
	 *  public class DdCommand { public static CommandShell.Info commandShellInfo = new CommandShell.Info( "dd", // Internal command name "Copies blocks of data between block and character devices", // short description of the command new Parameter[]{ new Parameter("if", "input file", new FileArgument("infilename", "location of the input file / device"), Parameter.MANDATORY), new Parameter("of", "output file", new FileArgument("outfilename", "location of the output file / device"), Parameter.MANDATORY), new Parameter("count", "count of blocks to transfer", new Argument("blockCount", "count of blocks to transfer"), Parameter.MANDATORY), new Parameter("bs", "size of the blocks to transfer", new Argument("blocksize", "example: 512, 42K, 300M, 2G. default: 1"), Parameter.OPTIONAL) } );
	 * 
	 *  ... }
	 * </pre>
	 */
	public static class Info {
		private final String name;
		private final Syntax[] syntaxes;

		public Info(String name, Syntax[] syntaxes) {
			this.name = name;
			this.syntaxes = syntaxes;
		}
		public Info(String name, String description, Parameter[] params) {
			this(name, new Syntax[] { new Syntax(description, params)});
		}

		public Info(String name, String description) {
			this(name, new Syntax[] { new Syntax(description)});
		}

		/**
		 * Gets the name of this command
		 */
		public String getName() {
			return name;
		}

		/**
		 * Gets the syntaxes allowed for this command
		 */
		public Syntax[] getSyntaxes() {
			return syntaxes;
		}

		public void usage() {
			Help.getHelp().usage(this);
		}

		public void help() {
			Help.getHelp().help(this);
		}

		public String complete(CommandLine partial) throws CompletionException {
			//System.out.println("completing \"" + partial + "\"");
			for (int i = 0; i < syntaxes.length; i++) {
				try {
					return syntaxes[i].complete(partial.getRemainder());
				} catch (CompletionException ex) {
					// this syntax is not fitting
					// following debug output is for testing the "intelligent" delegation mechanism
					// System.err.println("Syntax \"" + syntaxes[i].getDescription() + "\" threw "
					// + ex.toString());
				}
			}
			System.out.println();
			usage();
			throw new CompletionException("Invalid command syntax");
		}

		public ParsedArguments parse(String[] args) throws SyntaxError {
			for (int i = 0; i < syntaxes.length; i++) {
				try {
					return syntaxes[i].parse(args);
				} catch (SyntaxError ex) {
					// debug output to debug syntax finding mechanism
					//System.err.println(ex.toString());
					//ex.printStackTrace(System.out);
				}
			}

			// no fitting Syntax found? trow an error
			throw new SyntaxError("No matching syntax found");
		}
	}

}
