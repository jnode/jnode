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
 
package org.jnode.apps.jpartition;

import java.io.PrintStream;

/**
 * Factory used to build a user interface (text mode, graphical mode ...) for JPartition.
 *   
 * @author Fabien DUMINY (fduminy@jnode.org)
 *
 */
public interface ViewFactory {
    /**
     * Creates a view for the devices which might or might not (implementor's choice)
     * display available devices, their partitions... 
     * @param context The {@link Context} to use.
     * @param cmdProcessorView The command processor view to use (normally created by this factory).
     * @param install True if we are trying to install jnode, false in other cases.
     * @return The device view.
     * @throws Exception
     */
    Object createDeviceView(Context context, Object cmdProcessorView, boolean install)
        throws Exception;

    /**
     * Creates a command processor view.
     * @param context The {@link Context} to use.
     * @return A new command processor view.
     */
    Object createCommandProcessorView(Context context);

    /**
     * Creates an error reporter.
     * @param err The error stream to use.
     * @return A new error reporter.
     */
    ErrorReporter createErrorReporter(PrintStream err);
    
}
