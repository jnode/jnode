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
package org.jnode.shell.io;

import java.io.IOException;

import org.jnode.shell.ShellFailureException;

/**
 * This class provides life-cycle management for CommandIO objects,
 * ensuring that 'standard' streams and readers opened by the shells
 * get closed at the appropriate time.  Unfortunately, the current
 * implementation doesn't cope with certain Bjorne shell usage patterns.
 * 
 * @author crawley@jnode.org
 */
public class CommandIOHolder {
    CommandIO io;
    private boolean isMine;

    public CommandIOHolder(CommandIO stream, boolean isMine) {
        this.io = stream;
        this.isMine = isMine;
    }

    public CommandIOHolder(CommandIOHolder other) {
        this.io = other.io;
        this.isMine = false;
    }
    
    public synchronized CommandIO getIO() {
        return io;
    }

    public synchronized void setIO(CommandIO io, boolean isMine) {
        if (this.io != io) {
            close();
            this.io = io;
        }
        this.isMine = isMine;
    }

    public synchronized void setIO(CommandIOHolder holder) throws ShellFailureException {
        if (this != holder) {
            if (this.io != holder.io) {
                close();
                this.io = holder.io;
            }
            this.isMine = holder.isMine;
        }
    }

    public synchronized void close() {
        if (isMine) {
            try {
                isMine = false; // just in case we call close twice
                io.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    public synchronized boolean isMine() {
        return isMine;
    }

    public synchronized void flush() {
        try {
            io.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        } 
    }
}
