/*
 * $Id$
 *
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

/**
 * Base class used to report errors. It's only reporting errors to the logs.
 * Sub classes should override {@link #displayError(Object, String)} to actually
 * display errors in the user interface.
 * @author Fabien DUMINY (fduminy@jnode.org)
 *
 */
public class ErrorReporter {
    /**
     * Report an error from a {@link Throwable}.
     * @param log The logger to which error is reported (user interface might also display it). 
     * @param source The source object of the error.
     * @param t The {@link Throwable} that is being thrown because of the error.
     */
    public final void reportError(Logger log, Object source, Throwable t) {
        reportError(log, source, (Object) t);
    }

    /**
     * Display an error from its string representation.
     * @param log The logger to which error is reported (user interface might also display it).
     * @param source The source object of the error.
     * @param message The string representation of the error when no {@link Throwable} is thrown.
     */
    public final void reportError(Logger log, Object source, String message) {
        reportError(log, source, (Object) message);
    }

    /**
     * Display errors in the user interface. Do nothing by default.
     * User interface implementors should override it for appropriate displaying.
     * @param source The source object of the error.
     * @param message The message of the error.
     */
    protected void displayError(Object source, String message) {
        // by default display nothing
    }

    /**
     * Actual implementation of the public methods that trace the error in the logs
     * and delegate the user interface reporting to {@link #displayError(Object, String)}.  
     * @param log The logger to which error is reported (user interface might also display it).
     * @param source The source object of the error.
     * @param message The string representation of the error when no {@link Throwable} is thrown.
     */
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
