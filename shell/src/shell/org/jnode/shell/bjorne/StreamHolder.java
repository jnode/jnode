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
 
package org.jnode.shell.bjorne;

import java.io.IOException;

import org.jnode.shell.io.CommandIO;

public class StreamHolder {
    CommandIO stream;
    private boolean isMine;

    public StreamHolder(CommandIO stream, boolean isMine) {
        this.stream = stream;
        this.isMine = isMine;
    }

    public StreamHolder(StreamHolder other) {
        this.stream = other.stream;
        this.isMine = false;
    }
    
    public CommandIO getStream() {
        return stream;
    }

    public void setStream(CommandIO stream, boolean isMine) {
        close();
        this.stream = stream;
        this.isMine = isMine;
    }

    public void close() {
        if (isMine) {
            try {
                isMine = false; // just in case we call close twice
                stream.close();
            } catch (IOException ex) {
                // FIXME - should we squash or report this?
            }
        }
    }
    
    public boolean isMine() {
        return isMine;
    }
}