/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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
 
package org.jnode.bootlog;

import java.io.PrintStream;

/**
 * Logging class used during bootstrap.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface BootLog {
    public static final int DEBUG = 1;
    public static final int INFO = 2;
    public static final int WARN = 3;
    public static final int ERROR = 4;
    public static final int FATAL = 5;

    /**
     * Log a debug message
     *
     * @param msg
     */
    void debug(String msg);

    /**
     * Log a debug message
     *
     * @param msg
     * @param ex
     */
    void debug(String msg, Throwable ex);

    /**
     * Log an error message
     *
     * @param msg
     */
    void error(String msg);

    /**
     * Log an error message
     *
     * @param msg
     * @param ex
     */
    void error(String msg, Throwable ex);

    /**
     * Log an fatal message
     *
     * @param msg
     */
    void fatal(String msg);

    /**
     * Log an fatal message
     *
     * @param msg
     * @param ex
     */
    void fatal(String msg, Throwable ex);

    /**
     * Log an info message
     *
     * @param msg
     */
    void info(String msg);

    /**
     * Log an info message
     *
     * @param msg
     * @param ex
     */
    void info(String msg, Throwable ex);

    /**
     * Log an warning message
     *
     * @param msg
     * @param ex
     */
    void warn(String msg, Throwable ex);

    /**
     * Log an warning message
     *
     * @param msg
     */
    void warn(String msg);

    /**
     * Set the stream to use for debug logs.
     *
     * @param out
     */
    void setDebugOut(PrintStream out);
}
