/*
 * $Id$
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

package org.jnode.shell;

import java.io.InputStream;
import java.io.PrintStream;

/**
 * @author Martin Husted Hartvig (hagar@jnode.org)
 */

public interface Command {
    /**
     * This will be execute'ed when the command is called.
     * 
     * @param commandLine what comes in from the user
     * @param in input stream, most often this is System.in, but it can be a
     *        file or piped.
     * @param out output stream, mostly this is System.out, but it can be a file
     *        or piped.
     * @param err err stream, mostly this is System.err, but it can be a file or
     *        piped.
     * @throws Exception
     */
    public void execute(CommandLine commandLine, InputStream in,
            PrintStream out, PrintStream err) throws Exception;
}
