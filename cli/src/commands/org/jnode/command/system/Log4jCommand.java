/*
 * $Id$
 *
 * Copyright (C) 2003-2010 JNode.org
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
 
package org.jnode.command.system;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FileArgument;
import org.jnode.shell.syntax.FlagArgument;
import org.jnode.shell.syntax.Log4jLevelArgument;
import org.jnode.shell.syntax.Log4jLoggerArgument;
import org.jnode.shell.syntax.URLArgument;

/**
 * This command class shows and manages log4j logging.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author Fabien DUMINY (fduminy@jnode.org)
 * @author crawley@jnode.org
 */
public class Log4jCommand extends AbstractCommand {
    
    private static final String help_list = "List the current loggers";
    private static final String help_set_level = "Set the level of a logger";
    private static final String help_file = "log4j configuration file";
    private static final String help_url = "URL for a log4j configuration file";
    private static final String help_level = "the logging level";
    private static final String help_logger = "the logger";
    private static final String help_super = "Manage log4j logging";
    private static final String err_config = "Cannot open configuration file '%s': %s";
    
    private final FlagArgument argList;
    private final FlagArgument argSetLevel;
    private final FileArgument argFile;
    private final URLArgument argUrl;
    private final Log4jLevelArgument argLevel;
    private final Log4jLoggerArgument argLogger;

    
    public Log4jCommand() {
        super(help_super);
        argList     = new FlagArgument("list", Argument.OPTIONAL, help_list);
        argSetLevel = new FlagArgument("setLevel", Argument.OPTIONAL, help_set_level);
        argFile     = new FileArgument("file", Argument.OPTIONAL, help_file);
        argUrl      = new URLArgument("url", Argument.OPTIONAL, help_url);
        argLevel    = new Log4jLevelArgument("level", Argument.OPTIONAL, help_level);
        argLogger   = new Log4jLoggerArgument("logger", Argument.OPTIONAL, help_logger);
        registerArguments(argSetLevel, argList, argFile, argLevel, argLogger, argUrl);
    }

    public static void main(String[] args) throws Exception {
        new Log4jCommand().execute(args);
    }
    
    @Override
    public void execute() throws IOException {
        if (argFile.isSet()) {
            // Set configuration from a file
            final File configFile = argFile.getValue();
            final Properties props = new Properties();
            FileInputStream fis = null; 
            try {
                fis = new FileInputStream(configFile);
                props.load(fis);
                PropertyConfigurator.configure(props);
            } catch (FileNotFoundException ex) {
                getError().getPrintWriter().format(err_config, configFile, ex.getLocalizedMessage());
                exit(1);
            } finally {
                if (fis != null) {
                    fis.close();
                }
            }
        } else if (argUrl.isSet()) {
            // Set configuration from a URL
            final URL configURL = argUrl.getValue();
            PropertyConfigurator.configure(configURL);
        } else if (argList.isSet()) {
            // List current loggers and their levels.  Effective levels are shown
            // in parentheses.
            Enumeration<?> en = LogManager.getCurrentLoggers();
            while (en.hasMoreElements()) {
                Logger logger = (Logger) en.nextElement();
                String level = (logger.getLevel() == null) ? 
                        ('(' + logger.getEffectiveLevel().toString() + ')') :
                            logger.getLevel().toString();
                getOutput().getPrintWriter().println(logger.getName() + ": " + level);
            }
        } else if (argSetLevel.isSet()) {
            // Change the logging level for a specified logger or the root logger
            final Level level = argLevel.getValue();
            final Logger log = argLogger.isSet() ?  argLogger.getValue() : Logger.getRootLogger();
            log.setLevel(level);
        }
    }
}
