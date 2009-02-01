/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2009 JNode.org
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

    private final FlagArgument FLAG_LIST =
        new FlagArgument("list", Argument.OPTIONAL, "List current loggers");

    private final FlagArgument FLAG_SET_LEVEL =
        new FlagArgument("setLevel", Argument.OPTIONAL, "Set the Level of a logger");

    private final FileArgument ARG_FILE =
        new FileArgument("file", Argument.OPTIONAL, "log4j configuration file");

    private final URLArgument ARG_URL =
        new URLArgument("url", Argument.OPTIONAL, "URL for log4j configuration file");

    private final Log4jLevelArgument ARG_LEVEL = 
        new Log4jLevelArgument("level", Argument.OPTIONAL, "the logging level");

    private final Log4jLoggerArgument ARG_LOGGER = 
        new Log4jLoggerArgument("logger", Argument.OPTIONAL, "the logger");

    
    public Log4jCommand() {
        super("manage log4j logging");
        registerArguments(FLAG_SET_LEVEL, FLAG_LIST, ARG_FILE, ARG_LEVEL, ARG_LOGGER, ARG_URL);
    }

    public static void main(String[] args) throws Exception {
        new Log4jCommand().execute(args);
    }

    @Override
    public void execute() throws IOException {
        if (ARG_FILE.isSet()) {
            // Set configuration from a file
            final File configFile = ARG_FILE.getValue();
            final Properties props = new Properties();
            FileInputStream fis = null; 
            try {
                fis = new FileInputStream(configFile);
                props.load(fis);
                PropertyConfigurator.configure(props);
            } catch (FileNotFoundException ex) {
                getError().getPrintWriter().println("Cannot open configuration file '" + 
                        configFile + "': " + ex.getMessage());
                exit(1);
            } finally {
                if (fis != null) {
                    fis.close();
                }
            }
        } else if (ARG_URL.isSet()) {
            // Set configuration from a URL
            final URL configURL = ARG_URL.getValue();
            PropertyConfigurator.configure(configURL);
        } else if (FLAG_LIST.isSet()) {
            // List current loggers and their levels.  Effective levels are shown
            // in parentheses.
            Enumeration<?> en = LogManager.getCurrentLoggers();
            while (en.hasMoreElements()) {
                Logger logger = (Logger) en.nextElement();
                String level = (logger.getLevel() == null) ? 
                        ("(" + logger.getEffectiveLevel().toString() + ")") :
                            logger.getLevel().toString();
                getOutput().getPrintWriter().println(logger.getName() + ": " + level);
            }
        } else if (FLAG_SET_LEVEL.isSet()) {
            // Change the logging level for a specified logger or the root logger
            final Level level = ARG_LEVEL.getValue();
            final Logger log = ARG_LOGGER.isSet() ?  ARG_LOGGER.getValue() : Logger.getRootLogger();
            log.setLevel(level);
        }
    }
}
