/*
 * $Id: header.txt 5714 2010-01-03 13:33:07Z lsantha $
 *
 * Copyright (C) 2003-2012 JNode.org
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
 
package org.jnode.shell.bjorne;

import org.jnode.shell.CommandRunnable;
import org.jnode.shell.ShellException;

public abstract class BjorneSubshellRunner implements CommandRunnable {
    private int rc;
    private Throwable terminatingException;
    private final BjorneContext context;
    
    public BjorneSubshellRunner(BjorneContext context) {
        super();
        this.context = context;
    }

    @Override
    public void flushStreams() {
        context.flushIOs();
    }

    @Override
    public int getRC() {
        return rc;
    }
    
    @Override
    public Throwable getTerminatingException() {
        return terminatingException;
    }

    public final void run() {
        try {
            rc = doRun();
        } catch (Throwable ex) {
            terminatingException = ex;
        }
    }

    protected abstract int doRun() throws ShellException;
}
