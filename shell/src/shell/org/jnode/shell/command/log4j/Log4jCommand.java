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
 
package org.jnode.shell.command.log4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.Syntax;
import org.jnode.shell.help.SyntaxErrorException;
import org.jnode.shell.help.argument.FileArgument;
import org.jnode.shell.help.argument.OptionArgument;
import org.jnode.shell.help.argument.StringArgument;
import org.jnode.shell.help.argument.URLArgument;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author Fabien DUMINY (fduminy@jnode.org)
 */
public class Log4jCommand {
    static final OptionArgument ARG_ACTION = new OptionArgument("action",
            "action to do on log4j", 
            new OptionArgument.Option("level", "Change the level of a logger"), 
            new OptionArgument.Option("url", "Configure log4j from an url"),
            new OptionArgument.Option("file", "Configure log4j from a file"));
    
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
    
    static final FileArgument ARG_CFG_FILE = new FileArgument("configFile", "the configuration file");
    static final URLArgument ARG_CFG_URL = new URLArgument("configURL", "the configuration URL");
    
    static final Parameter PARAM_ACTION = new Parameter(ARG_ACTION);    
	static final Parameter PARAM_LOGGER = new Parameter(ARG_LOGGER, Parameter.MANDATORY);
	static final Parameter PARAM_LEVEL = new Parameter(ARG_LEVEL, Parameter.MANDATORY);        
    
    static final Parameter PARAM_CFG_FILE = new Parameter(ARG_CFG_FILE, Parameter.MANDATORY);
    static final Parameter PARAM_CFG_URL = new Parameter(ARG_CFG_URL, Parameter.MANDATORY);

	public static Help.Info HELP_INFO = new Help.Info("log4j", new Syntax[] 
                { 
                    new Syntax("Set the level for a logger", new Parameter[] { PARAM_LOGGER, PARAM_LEVEL }),
                    new Syntax("Set the configuration from a file", new Parameter[] { PARAM_CFG_FILE }),
                    new Syntax("Set the configuration from an url", new Parameter[] { PARAM_CFG_URL })
                });

	public static void main(String[] args) throws SyntaxErrorException, IOException {
		final ParsedArguments cmdLine = HELP_INFO.parse(args);
		
        if(PARAM_CFG_FILE.isSet(cmdLine))
        {
            final File configFile = ARG_CFG_FILE.getFile(cmdLine);
            final Properties props = new Properties();
            final FileInputStream fis = new FileInputStream(configFile);
            props.load(fis);
            PropertyConfigurator.configure(props);
            fis.close();
        }
        else if(PARAM_CFG_URL.isSet(cmdLine))
        {
            final URL configURL = ARG_CFG_URL.getURL(cmdLine);
            PropertyConfigurator.configure(configURL);
        }
        else
        {
            final String loggerName = ARG_LOGGER.getValue(cmdLine);
            final String levelName = ARG_LEVEL.getValue(cmdLine);
    		final Logger log = Logger.getLogger(loggerName);
    		final Level level = Level.toLevel(levelName.toUpperCase());
    		
    		log.setLevel(level);
        }
	}
}
