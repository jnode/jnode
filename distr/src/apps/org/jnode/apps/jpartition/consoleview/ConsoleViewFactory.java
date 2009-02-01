/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
 *
 * JNode.org
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
 
package org.jnode.apps.jpartition.consoleview;

import java.io.InputStream;
import java.io.PrintStream;

import org.jnode.apps.jpartition.ErrorReporter;
import org.jnode.apps.jpartition.ViewFactory;

/**
 * 
 * @author Fabien Duminy
 * 
 */
public class ConsoleViewFactory implements ViewFactory {
    private final InputStream in;
    private final PrintStream out;
    private final PrintStream err;

    public ConsoleViewFactory(InputStream in, PrintStream out, PrintStream err) {
        this.in = in;
        this.out = out;
        this.err = err;
    }

    public Object createCommandProcessorView() {
        return null; // nothing particular to create : work is done by
        // createDeviceView
    }

    public Object createDeviceView(ErrorReporter errorReporter, Object cmdProcessorView,
            boolean install) throws Exception {
        return new ConsoleView(in, out, errorReporter, install);
    }

    public ErrorReporter createErrorReporter() {
        return new ConsoleErrorReporter(err);
    }
}
