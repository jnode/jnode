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
 
package org.jnode.vm;

import java.io.PrintStream;

import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingException;

import org.jnode.bootlog.BootLog;
import org.jnode.bootlog.BootLogInstance;
import org.jnode.vm.objects.BootableObject;

/**
 * Logging class used during bootstrap.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
class BootLogImpl implements BootLog, BootableObject {

    private PrintStream debugOut;

    /**
     * {@inheritDoc}
     */
    public void debug(String msg) {
        final PrintStream out = (debugOut != null) ? debugOut : System.out;
        log(DEBUG, out, msg, null);
    }

    /**
     * {@inheritDoc}
     */
    public void debug(String msg, Throwable ex) {
        final PrintStream out = (debugOut != null) ? debugOut : System.out;
        log(DEBUG, out, msg, ex);
    }

    /**
     * {@inheritDoc}
     */
    public void error(String msg) {
        log(ERROR, System.err, msg, null);
    }

    /**
     * {@inheritDoc}
     */
    public void error(String msg, Throwable ex) {
        log(ERROR, System.err, msg, ex);
        /*try {
              Thread.sleep(2500);
          } catch (InterruptedException ex2) {
              // Ignore
          }*/
    }

    /**
     * {@inheritDoc}
     */
    public void fatal(String msg) {
        log(FATAL, System.err, msg, null);
    }

    /**
     * {@inheritDoc}
     */
    public void fatal(String msg, Throwable ex) {
        log(FATAL, System.err, msg, ex);
    }

    /**
     * {@inheritDoc}
     */
    public void info(String msg) {
        log(INFO, System.out, msg, null);
    }

    /**
     * {@inheritDoc}
     */
    public void info(String msg, Throwable ex) {
        log(INFO, System.out, msg, ex);
    }

    /**
     * {@inheritDoc}
     */
    public void warn(String msg, Throwable ex) {
        log(WARN, System.out, msg, ex);
    }

    /**
     * {@inheritDoc}
     */
    public void warn(String msg) {
        log(WARN, System.out, msg, null);
    }

    /**
     * {@inheritDoc}
     */
    public void setDebugOut(PrintStream out) {
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
    private void log(int level, PrintStream ps, String msg, Throwable ex) {
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
                Unsafe.debug("\n");
            }
            if (ex != null) {
                Unsafe.debug(ex.toString());
                Unsafe.debug("\n");
            }
        }
    }

    static void initialize() {
        Unsafe.debug("Initialize BootLog\n");
        try {
            BootLogInstance.set(new BootLogImpl());
        } catch (NameAlreadyBoundException e) {
            Unsafe.debug(e.toString());
            Unsafe.debug("\n");
        } catch (NamingException e) {
            Unsafe.debug(e.toString());
            Unsafe.debug("\n");
        }
    }
}
