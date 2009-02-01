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
 
package org.jnode.apps.jpartition;

import org.apache.log4j.Logger;

public class ErrorReporter {
    public final void reportError(Logger log, Object source, Throwable t) {
        reportError(log, source, (Object) t);
    }

    public final void reportError(Logger log, Object source, String message) {
        reportError(log, source, (Object) message);
    }

    protected void displayError(Object source, String message) {
        // by default display nothing
    }

    private final void reportError(Logger log, Object source, Object message) {
        Throwable t = (message instanceof Throwable) ? (Throwable) message : null;

        String msg = (t == null) ? String.valueOf(message) : t.getMessage();
        displayError(source, msg);

        if (log != null) {
            if (t != null) {
                log.error(msg, t);
            } else {
                log.error(msg);
            }
        }
    }
}
