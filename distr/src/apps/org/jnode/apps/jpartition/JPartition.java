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

import java.io.InputStream;
import java.io.PrintStream;

import org.jnode.apps.jpartition.model.UserFacade;

/**
 * Main class of JPartition application.
 * @author Fabien DUMINY (fduminy@jnode.org)
 *
 */
public class JPartition {
    /**
     * The view factory used to create the user interface.
     */
    private final ViewFactory viewFactory;
    
    /**
     * True if we are trying to install jnode, false in other cases.
     */
    private final boolean install;

    /**
     * The input stream to use.
     */
    private final InputStream in;
    
    /**
     * The output stream to use.
     */
    private final PrintStream out;

    /**
     * The error stream to use.
     */
    private final PrintStream err;
    
    /**
     * Constructor for a new instance of JPartition application.
     * @param viewFactory The view factory used to create the user interface.
     * @param in Input stream.
     * @param out Output stream.
     * @param install True if we are trying to install jnode, false in other cases.
     */
    public JPartition(ViewFactory viewFactory, InputStream in, PrintStream out, PrintStream err, boolean install) {
        this.viewFactory = viewFactory;
        this.install = install;
        this.in = in;
        this.out = out;
        this.err = err;        
    }

    /**
     * Actually show the user interface from parameters given at construction time.
     * @throws Exception
     */
    public final void launch() throws Exception {
        ErrorReporter errorReporter = viewFactory.createErrorReporter(err);
        Context context = new Context(in, out, errorReporter);        
        UserFacade.getInstance().setContext(context);

        
        // CommandProcessor
        Object cmdProcessorView = viewFactory.createCommandProcessorView(context);

        // Device
        viewFactory.createDeviceView(context, cmdProcessorView, install);
    }
}
