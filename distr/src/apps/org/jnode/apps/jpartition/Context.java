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
 
package org.jnode.apps.jpartition;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

public class Context {
    private final BufferedReader in;
    private final PrintStream out;
    private final ErrorReporter errorReporter;

    public Context(InputStream in, PrintStream out, ErrorReporter errorReporter) {
        InputStreamReader r = new InputStreamReader(in);
        this.in = new BufferedReader(r);

        this.out = out;
        this.errorReporter = errorReporter;
    }

    public final PrintStream getOut() {
        return out;
    }

    public final BufferedReader getIn() {
        return in;
    }

    public final ErrorReporter getErrorReporter() {
        return errorReporter;
    }
}
