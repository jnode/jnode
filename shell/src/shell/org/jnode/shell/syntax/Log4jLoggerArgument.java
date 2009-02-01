/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
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
 
package org.jnode.shell.syntax;

import java.util.Enumeration;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jnode.driver.console.CompletionInfo;
import org.jnode.shell.CommandLine.Token;

/**
 * This Argument class captures log4j logger names, and completes them against 
 * the set of logger names that are already defined.
 * 
 * @author crawley@jnode.org
 */
public class Log4jLoggerArgument extends Argument<Logger> {

    public Log4jLoggerArgument(String label, int flags, String description) {
        super(label, flags, new Logger[0], description);
    }

    @Override
    protected String argumentKind() {
        return "logger";
    }

    /**
     * Any token is an acceptable Logger name.
     */
    @Override
    protected Logger doAccept(Token value) throws CommandSyntaxException {
        return Logger.getLogger(value.text);
    }

    /**
     * Complete against existing logger names.
     */
    @Override
    public void complete(CompletionInfo completion, String partial) {
        Enumeration<?> en = LogManager.getCurrentLoggers();
        while (en.hasMoreElements()) {
            String loggerName = ((Logger) en.nextElement()).getName();
            if (loggerName.startsWith(partial)) {
                completion.addCompletion(loggerName);
            }
        }
    }
}
