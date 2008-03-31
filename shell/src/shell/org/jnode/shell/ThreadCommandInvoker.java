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


/*
 * User: Sam Reid Date: Dec 20, 2003 Time: 1:20:33 AM Copyright (c) Dec 20, 2003
 * by Sam Reid
 */

/**
 * This invoker launches commands in regular threads. These will typically share
 * stdin,out,err etc with other commands in the current isolate. Hence
 * stream redirection should be forbidden for commands launched via the 'main' 
 * entry point, and a command launched via Command.execute will need to use
 * the execute in,out,err arguments to access the command's redirected streams.
 * 
 * @author Sam Reid
 * @author Martin Husted Hartvig (hagar@jnode.org)
 * @author crawley@jnode.org
 */
public class ThreadCommandInvoker extends AsyncCommandInvoker {

    public static final Factory FACTORY = new Factory() {
        public CommandInvoker create(CommandShell shell) {
            return new ThreadCommandInvoker(shell);
        }

        public String getName() {
            return "thread";
        }
    };

    public ThreadCommandInvoker(CommandShell commandShell) {
        super(commandShell);
    }

    public String getName() {
        return "thread";
    }

    CommandThreadImpl createThread(CommandLine cmdLine, CommandRunner cr) {
        return new CommandThreadImpl(cr, cmdLine.getCommandName());
    }
}
