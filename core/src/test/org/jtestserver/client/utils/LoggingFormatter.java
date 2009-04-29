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

import java.util.logging.Formatter;
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
        StringBuilder sb = new StringBuilder(record.getLevel().getName()).append(": ");
        sb.append(record.getSourceClassName()).append('#').append(record.getSourceMethodName());
        sb.append(": ").append(record.getMessage()).append('\n');
        return sb.toString();
    }
}
