/*
 * $Id$
 *
 * Copyright (C) 2003-2010 JNode.org
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
 
package org.jnode.apps.telnetd;

import org.jnode.driver.console.TextConsole;
import org.jnode.shell.CommandShell;
import org.jnode.shell.ShellException;

/**
 * 
 * @author Fabien DUMINY (fduminy at jnode.org)
 * 
 */
public class JNodeCommandShell extends CommandShell {
    private final JNodeShell shell;

    // public JNodeCommandShell(JNodeShell shell, TextConsole console,
    // InputStream in, PrintStream out, PrintStream err)
    // throws ShellException {
    // super(console, in, out, err);
    // this.shell = shell;
    // }

    /**
     * Construct a JNode command shell for the telnet daemon.
     */
    public JNodeCommandShell(TextConsole cons, JNodeShell shell) throws ShellException {
        super(cons);
        this.shell = shell;
        System.err.println("JNodeCommandShell");
    }

    /**
     * Exit the JNode command shell for the telnet daemon.
     */
    @Override
    public void exit() {
        shell.close();
        super.exit();
    }
}
