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
 
package org.jnode.shell.io;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;

/**
 * Instances of this class are used to denote well known streams (e.g. standard
 * input, etc) when building a CommandLine. They should be translated into real
 * streams before the command is actually invoked.
 * 
 * @author crawley@jnode.org
 */
public final class CommandIOMarker extends BaseCommandIO {
    private final String name;
    private final int direction;

    public CommandIOMarker(String name) {
        this(name, DIRECTION_UNSPECIFIED);
    }

    public CommandIOMarker(String name, int direction) {
        super(null);
        this.name = name;
        this.direction = direction;
    }

    public String toString() {
        return name + "(" + direction + ")";
    }

    @Override
    public int getDirection() {
        return direction;
    }
    
    void doClose() {
        // do nothing.
    }

    void doFlush() {
        // do nothing.
    }

    public String getName() {
        return name;
    }

    @Override
    public InputStream getInputStream() {
        throw new CommandIOException("I/O not supported on CommandIOMarker");
    }

    @Override
    public OutputStream getOutputStream() {
        throw new CommandIOException("I/O not supported on CommandIOMarker");
    }

    @Override
    public PrintStream getPrintStream(boolean autoflush) {
        throw new CommandIOException("I/O not supported on CommandIOMarker");
    }

    @Override
    public PrintWriter getPrintWriter(boolean autoflush) {
        throw new CommandIOException("I/O not supported on CommandIOMarker");
    }

    @Override
    public Reader getReader() throws CommandIOException {
        throw new CommandIOException("I/O not supported on CommandIOMarker");
    }

    @Override
    public Writer getWriter() throws CommandIOException {
        throw new CommandIOException("I/O not supported on CommandIOMarker");
    }
}
