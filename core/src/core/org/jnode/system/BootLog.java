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
 
package org.jnode.system;

import java.io.PrintStream;

import org.jnode.vm.Unsafe;

/**
 * Logging class used during bootstrap.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class BootLog {

    public static final int DEBUG = 1;
    public static final int INFO = 2;
    public static final int WARN = 3;
    public static final int ERROR = 4;
    public static final int FATAL = 5;

    private static PrintStream debugOut;

    /**
     * Log a debug message
     *
     * @param msg
     */
    public static void debug(String msg) {
        final PrintStream out = (debugOut != null) ? debugOut : System.out;
        log(DEBUG, out, msg, null);
    }

    /**
     * Log a debug message
     *
     * @param msg
     * @param ex
     */
    public static void debug(String msg, Throwable ex) {
        final PrintStream out = (debugOut != null) ? debugOut : System.out;
        log(DEBUG, out, msg, ex);
    }

    /**
     * Log an error message
     *
     * @param msg
     */
    public static void error(String msg) {
        log(ERROR, System.err, msg, null);
    }

    /**
     * Log an error message
     *
     * @param msg
     * @param ex
     */
    public static void error(String msg, Throwable ex) {
        log(ERROR, System.err, msg, ex);
        /*try {
              Thread.sleep(2500);
          } catch (InterruptedException ex2) {
              // Ignore
          }*/
    }

    /**
     * Log an fatal message
     *
     * @param msg
     */
    public static void fatal(String msg) {
        log(FATAL, System.err, msg, null);
    }

    /**
     * Log an fatal message
     *
     * @param msg
     * @param ex
     */
    public static void fatal(String msg, Throwable ex) {
        log(FATAL, System.err, msg, ex);
    }

    /**
     * Log an info message
     *
     * @param msg
     */
    public static void info(String msg) {
        log(INFO, System.out, msg, null);
    }

    /**
     * Log an info message
     *
     * @param msg
     * @param ex
     */
    public static void info(String msg, Throwable ex) {
        log(INFO, System.out, msg, ex);
    }

    /**
     * Log an warning message
     *
     * @param msg
     * @param ex
     */
    public static void warn(String msg, Throwable ex) {
        log(WARN, System.out, msg, ex);
    }

    /**
     * Log an warning message
     *
     * @param msg
     */
    public static void warn(String msg) {
        log(WARN, System.out, msg, null);
    }

    /**
     * Set the stream to use for debug logs.
     *
     * @param out
     */
    public static void setDebugOut(PrintStream out) {
        debugOut = out;
    }

    /**
     * Log an error message
     *
     * @param level
     * @param ps
     * @param msg
     * @param ex
     */
    private static void log(int level, PrintStream ps, String msg, Throwable ex) {
        if (ps != null) {
            if (msg != null) {
                ps.println(msg);
            }
            if (ex != null) {
                ex.printStackTrace(ps);
            }
        } else {
            if (msg != null) {
                Unsafe.debug(msg);
            }
            if (ex != null) {
                Unsafe.debug(ex.toString());
            }
        }
    }
}
