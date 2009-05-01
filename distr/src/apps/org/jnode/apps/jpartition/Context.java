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

/**
 * Represents the context with the shell : input and output streams
 * and the error reporter (error stream might eventually be used by some
 * {@link ErrorReporter} implementations).
 *  
 * @author Fabien DUMINY (fduminy@jnode.org)
 *
 */
public class Context {
    /**
     * Reader for the input stream.
     */
    private final BufferedReader in;
    
    /**
     * Output stream.
     */
    private final PrintStream out;
    
    /**
     * Error reporter.
     */
    private final ErrorReporter errorReporter;

    /**
     * Constructor.
     * @param in Input stream.
     * @param out Output stream.
     * @param errorReporter Error reporter.
     */
    public Context(InputStream in, PrintStream out, ErrorReporter errorReporter) {
        InputStreamReader r = new InputStreamReader(in);
        this.in = new BufferedReader(r);

        this.out = out;
        this.errorReporter = errorReporter;
    }

    /**
     * Get the reader for the input stream. 
     * @return Reader for the input stream.
     */
    public final PrintStream getOut() {
        return out;
    }

    /**
     * Get the output stream.
     * @return Output stream.
     */
    public final BufferedReader getIn() {
        return in;
    }

    /**
     * Get the error reporter.
     * @return Error reporter.
     */
    public final ErrorReporter getErrorReporter() {
        return errorReporter;
    }
}
