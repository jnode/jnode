/*
 * $Id$
 */
package org.jnode.shell.command.log4j;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.OptionArgument;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.StringArgument;
import org.jnode.shell.help.Syntax;
import org.jnode.shell.help.SyntaxErrorException;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class Log4jCommand {

	static final OptionArgument ARG_LEVEL =
		new OptionArgument(
			"level",
			"minimum level",
			new OptionArgument.Option[] {
				new OptionArgument.Option("debug", "Debug"),
				new OptionArgument.Option("info", "Info"),
				new OptionArgument.Option("warn", "Warning"),
				new OptionArgument.Option("error", "Error"),
				new OptionArgument.Option("fatal", "Fatal")});

	static final StringArgument ARG_LOGGER = new StringArgument("logger", "the name of the logger");

	static final Parameter PARAM_LOGGER = new Parameter(ARG_LOGGER, Parameter.MANDATORY);
	static final Parameter PARAM_LEVEL = new Parameter(ARG_LEVEL, Parameter.MANDATORY);

	public static Help.Info HELP_INFO = new Help.Info("log4j", new Syntax[] { new Syntax("Set the level for a logger", new Parameter[] { PARAM_LOGGER, PARAM_LEVEL })
	});

	public static void main(String[] args) throws SyntaxErrorException {
		final ParsedArguments cmdLine = HELP_INFO.parse(args);
		final String loggerName = ARG_LOGGER.getValue(cmdLine);
		final String levelName = ARG_LEVEL.getValue(cmdLine);
		
		final Logger log = Logger.getLogger(loggerName);
		final Level level = Level.toLevel(levelName.toUpperCase());
		
		log.setLevel(level);
	}
	
	
}
