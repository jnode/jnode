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

import org.jnode.apps.jpartition.consoleview.ConsoleViewFactory;
import org.jnode.apps.jpartition.swingview.SwingViewFactory;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FlagArgument;

/**
 * Command to launch JPartition from JNode's shell.
 * @author Fabien DUMINY (fduminy@jnode.org)
 *
 */
public class JPartitionCommand extends AbstractCommand {
    private final FlagArgument FLAG_SWING =
            new FlagArgument("swing", Argument.OPTIONAL, "if set, use the Swing (graphic) UI");

    private final FlagArgument FLAG_CONSOLE =
            new FlagArgument("console", Argument.OPTIONAL, "if set, use the console (text) UI");

    private final FlagArgument ARG_INSTALL =
            new FlagArgument("install", Argument.OPTIONAL, "if set, format the partition(s)");

    /**
     * Constructor.
     */
    public JPartitionCommand() {
        super("interactive disk partitioning tool");
        registerArguments(FLAG_CONSOLE, FLAG_SWING, ARG_INSTALL);
    }

    /**
     * Main method to run JPartition outside of JNode.
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        new JPartitionCommand().execute(args);
    }

    /**
     * {@inheritDoc}
     */
    public void execute() throws Exception {
        boolean install = ARG_INSTALL.isSet();

        InputStream in = getInput().getInputStream();
        PrintStream out = getOutput().getPrintStream();
        PrintStream err = getError().getPrintStream();
        
        boolean consoleView = FLAG_CONSOLE.isSet();
        boolean swingView = FLAG_SWING.isSet();
        doExecute(install, in, out, err, consoleView, swingView);
    }

    /**
     * {@inheritDoc}
     */
    public void doExecute(boolean install, InputStream in, PrintStream out, PrintStream err, boolean consoleView,
                          boolean swingView) throws Exception { 
        ViewFactory viewFactory =
                consoleView ? new ConsoleViewFactory()
                        : swingView ? new SwingViewFactory() : null;
        if (viewFactory == null) {
            err.println("No UI selected");
            exit(1);
        }
 
        JPartition jpartition = new JPartition(viewFactory, in, out, err, install);
        jpartition.launch();
    } 
}
