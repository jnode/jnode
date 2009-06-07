/*
JTestServer is a client/server framework for testing any JVM implementation.
 
Copyright (C) 2009  Fabien DUMINY (fduminy@jnode.org)

JTestServer is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

JTestServer is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package org.jtestserver.client.utils;

import java.io.PrintWriter;

import java.io.StringWriter;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * This is the formatter we will use for logging.
 * 
 * @author Fabien DUMINY (fduminy@jnode.org)
 *
 */
public class LoggingFormatter extends Formatter {
    /**
     * {@inheritDoc}
     */
    @Override
    public String format(LogRecord record) {
        String loggerName = record.getLoggerName();
        String level = record.getLevel().getName();
        String message = record.getMessage();
/*        
        System.err.println("message0=" + message.replace(':', '#'));
        String serverLogger = ProcessRunner.SERVER_LOGGER.getName(); 
        if (serverLogger.equals(record.getLoggerName())) {
            int idx = message.indexOf(serverLogger);            
            if (idx >= 0) {
                message = message.substring(idx + serverLogger.length() + 1);
                message = message.trim();
                
                System.err.println("message1=" + message.replace(':', '#'));
                idx = message.indexOf(':');                
                if (idx >= 0) {
                    try {
                        level = Level.parse(message.substring(0, idx)).getName();
                        
                        message = message.substring(idx + 1);
                        message = message.trim();
                        
                        System.err.println("message2=" + message.replace(':', '#'));
                        idx = message.indexOf(':');                        
                        if (idx >= 0) {
                            loggerName = message.substring(0, idx);
                            
                            message = message.substring(idx + 1);
                            message = message.trim();
                        }
                    } catch (IllegalArgumentException iae) {
                        // ignore errors from Level.parse(...) method
                    }
                }
            }
        }
*/        
        StringBuilder sb = new StringBuilder(level);
        sb.append(": ").append(loggerName);            
        
        //sb.append(record.getSourceMethodName());        
        sb.append(": ").append(message);
        if (record.getThrown() != null) {
            StringWriter sw = new StringWriter();
            record.getThrown().printStackTrace(new PrintWriter(sw));
            sb.append('\n').append(sw.getBuffer());
        }
        sb.append('\n');        
        return sb.toString();
    }
}
