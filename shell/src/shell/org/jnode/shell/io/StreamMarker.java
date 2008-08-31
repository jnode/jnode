/*
 * $Id: ShellManager.java 3571 2007-10-26 21:30:12Z lsantha $
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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

import java.io.Closeable;

/**
 * Instances of this class are used to denote well known streams (e.g. standard
 * input, etc) when building a CommandLine. They should be translated into real
 * streams before the command is actually invoked.
 * 
 * @author crawley@jnode.org
 */
public final class StreamMarker implements Closeable {
    private final String name;

    public StreamMarker(String name) {
        this.name = name;
    }

    public void close() {
        // Dummy operation
    }

    public String toString() {
        return name;
    }
}
