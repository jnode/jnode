/*
 * $Id$
 */
package org.jnode.shell.help;

import java.util.Map;
import java.util.HashMap;

import org.jnode.shell.CommandLine;

/**
 * @author qades
 */
public class Syntax {
	private final String description;
	private final Parameter[] params;

	public Syntax(String description, Parameter[] params) {
		this.description = description;
		this.params = params;
	}

	public Syntax(String description) {
		this(description, new Parameter[0]);
	}

	/**
	 * Gets the description of this syntax
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Gets the parameters of this syntax
	 */
	public Parameter[] getParams() {
		return params;
	}

	public String complete(CommandLine partial) throws CompletionException {
		CompletionVisitor visitor = new CompletionVisitor();
		try {
			visitCommandLine(partial, visitor);
		} catch(SyntaxError ex) {
			throw new CompletionException(ex.getMessage());
		}
		return visitor.getCompletedLine();
	}

	synchronized ParsedArguments parse(String[] args) throws SyntaxError {
		if (params.length == 0) {
			if( args.length == 0 )
				return new ParsedArguments(new HashMap());
			throw new SyntaxError("Syntax takes no parameter");
		}

		CommandLine cmdLine = new CommandLine(args);
		ParseVisitor visitor = new ParseVisitor();
		visitCommandLine(cmdLine, visitor);
		ParsedArguments result = new ParsedArguments(visitor.getArgumentMap());

		// check if all mandatory parameters are set
		for( int i = 0; i < params.length; i++ )
			if( !params[i].isOptional() && !params[i].isSet(result) )
				throw new SyntaxError("Mandatory parameter " + params[i].getName() + " not set");
		return result;
	}


	private synchronized void visitCommandLine(CommandLine cmdLine, CommandLineVisitor visitor) {
		clearArguments();
		Parameter param = null;
		do {
			String s = "";
			if( cmdLine.hasNext() )
				s = cmdLine.next();

         // TODO: it didn't handle correctly the parameters starting with "-" 
            
			/*if (s.startsWith("-") && (cmdLine.getTokenType() == CommandLine.LITERAL)) { // we got a named parameter here
				if (param != null) // last param takes an argument, but it's not given
					throw new SyntaxError("Unexpected Parameter " + s);

				param = getParameter(s.substring(1));
				if (param == null)
					throw new SyntaxError("Unknown Parameter \"" + s + "\"");
                                visitor.visitParameter(param);
			} else { // we got an argument */
				if (param == null) {// must be an argument for an anonymous parameter
					param = getAnonymousParameter();
					if (param == null) {// but...there are no more of them
						throw new SyntaxError("Unexpected argument \"" + s + "\"");
					} else {
						visitor.visitParameter(param);
					}
				//}

				// no check if there is an argument, as else we would have exited before (Parameter satisfied)
				Argument arg = param.getArgument();
				String value = visitor.visitValue(s, !cmdLine.hasNext(), cmdLine.getTokenType());
				if( value != null )
					arg.setValue(value);
			}
			if (param.isSatisfied())
				param = null;
		} while (cmdLine.hasNext());
	}

	private void clearArguments() {
		for (int i = 0; i < params.length; i++)
			if (params[i].hasArgument())
				params[i].getArgument().clear();
	}

	private Parameter getParameter(String name) {
		if (Parameter.ANONYMOUS.equals(name) && (Parameter.ANONYMOUS != name)) // user just typed the "-"
			return null;
		for (int i = 0; i < params.length; i++)
			if( name.equals(params[i].getName()) && (!params[i].hasArgument() || !params[i].isSatisfied()) )
				return params[i];
		return null;
	}

	private Parameter getAnonymousParameter() {
		return getParameter(Parameter.ANONYMOUS);
	}

        private interface CommandLineVisitor {
		void visitParameter(Parameter p);
		String visitValue(String s, boolean last, int tokenType);
	}

	private class CompletionVisitor implements CommandLineVisitor {
		String line = "";
		Parameter param = null;
		public void visitParameter(Parameter p) {
			this.param = p;
			String s = p.getName();
			if( !Parameter.ANONYMOUS.equals(s) ) {
				line += "-" + s + " ";
			}
		}
		public String visitValue(String s, boolean last, int tokenType) {
			String result = "";
			if (last) { // we're not yet at the end of the command line
				// token to be completed
				result = param.complete(s);
				line += result;
			} else {
				result = ((tokenType & CommandLine.STRING) != 0
					? CommandLine.escape(s, true)
					: s);
				line += result + " ";
			}
			return result;
		}
		String getCompletedLine() {
			return line;
		}
	}

	private class ParseVisitor implements CommandLineVisitor {
		Map result = new HashMap();
		Parameter param = null;
		boolean valued = false;
		public void visitParameter(Parameter p) {
			finishParameter();
			this.param = p;
		}
		public String visitValue(String s, boolean last, int tokenType) {
			if( last && "".equals(s) )
				return null;
			valued = true;
			return s;
		}
		Map getArgumentMap() {
			finishParameter();
			return result;
		}
		void finishParameter() {
			if( param == null )
				return;

			if( valued || !param.hasArgument() )
				result.put(param, null);	// mark it as "set"
			if( param.hasArgument() ) {
				Argument arg = param.getArgument();
				result.put(arg, arg.getValues());
			}
			param = null;
			valued = false;
		}
	}

}
