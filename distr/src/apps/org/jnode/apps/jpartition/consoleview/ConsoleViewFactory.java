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
 
package org.jnode.apps.jpartition.consoleview;

import java.io.PrintStream;

import org.jnode.apps.jpartition.Context;
import org.jnode.apps.jpartition.ErrorReporter;
import org.jnode.apps.jpartition.ViewFactory;

/**
 * 
 * @author Fabien Duminy
 * 
 */
public class ConsoleViewFactory implements ViewFactory {
    public ConsoleViewFactory() {
    }

    public Object createCommandProcessorView(Context context) {
        // nothing particular to create : work is done by createDeviceView
        return null; 
    }

    /**
     * {@inheritDoc}
     */
    public Object createDeviceView(Context context, Object cmdProcessorView,
            boolean install) throws Exception {
        return new ConsoleView(context, install);
    }

    /**
     * {@inheritDoc}
     */
    public ErrorReporter createErrorReporter(PrintStream err) {
        return new ConsoleErrorReporter(err);
    }
}
