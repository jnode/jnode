/*
 * $Id: CommandLine.java 3580 2007-11-03 20:31:24Z lsantha $
 *
 * JNode.org
 * Copyright (C) 2007 JNode.org
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
package org.jnode.shell;

/**
 * A CommandRunner is a Runnable with a field to record a command's return code.  It also 
 * provides a convenience method for conditionally printing a command stack-trace.
 * 
 * @author crawley@jnode.org
 *
 */
public abstract class CommandRunner implements Runnable {
    private final CommandShell shell;

    /**
     * @param shell
     */
    CommandRunner(CommandShell shell) {
        this.shell = shell;
    }

    private int rc;

    public int getRC() {
        return rc;
    }

    void setRC(int rc) {
        this.rc = rc;
    }

    boolean isDebugEnabled() {
        return this.shell.isDebugEnabled();
    }

    void stackTrace(Throwable ex) {
        if (ex != null && isDebugEnabled()) {
            ex.printStackTrace(this.shell.getConsole().getErr());
        }
    }

}